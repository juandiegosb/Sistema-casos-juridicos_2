package co.edu.ufps.legal_cases.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.ufps.legal_cases.security.model.Permiso;
import co.edu.ufps.legal_cases.security.model.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.UsuarioSistemaRepository;
import co.edu.ufps.legal_cases.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Este filtro revisa cada petición que llega al backend
// y mira si esa petición trae una cookie con un JWT válido.
@Component
//Aqui se especifica que se ejecuta una vez por peticion
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "access_token";   //nombre que le dimos a la cookie en el AuthController

    private final JwtService jwtService;
    private final UsuarioSistemaRepository usuarioSistemaRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioSistemaRepository usuarioSistemaRepository) {
        this.jwtService = jwtService;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) /**Para que la peticion siga despues de la revision */ throws ServletException, IOException {

        String token = obtenerTokenDesdeCookie(request); // Busca la cookie con el JWT

        if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.obtenerUsername(token);

            UsuarioSistema usuario = usuarioSistemaRepository
                    .findWithRolPermisosAndPerfilByUsername(username)
                    .orElse(null);

            if (usuario != null && usuarioPuedeAutenticarse(usuario)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuario.getUsername(),
                                null, // Como la contraseña se valido en el login aqui ya no es necesario
                                obtenerAuthorities(usuario) // Trae los permisos en formato de Spring Security
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                ); // Agrega detalles de la peticion como la ip del cliente, etc

                SecurityContextHolder.getContext().setAuthentication(authentication);   // Aqui spring ya sabe quien es el usuario y sus permisos
            }

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response); //Deja que la peticion continue su camino
    }

    private String obtenerTokenDesdeCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies(); // Obtiene todas las cookies que mando el navegador en la peticion

        if (cookies == null) {
            return null;
        }

        //Busca la que necesitamos para autenticar
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private boolean usuarioPuedeAutenticarse(UsuarioSistema usuario) {
        return usuarioActivo(usuario)
                && rolActivo(usuario)
                && perfilActivo(usuario);
    }

    //Metodos para ver que el usuario, su rol y su perfil esten activos sino no se puede autenticar
    private boolean usuarioActivo(UsuarioSistema usuario) {
        return Boolean.TRUE.equals(usuario.getActivo());
    }

    private boolean rolActivo(UsuarioSistema usuario) {
        return usuario.getRol() != null
                && Boolean.TRUE.equals(usuario.getRol().getActivo());
    }

    private boolean perfilActivo(UsuarioSistema usuario) {
        if (usuario.getAsesor() != null) {
            return Boolean.TRUE.equals(usuario.getAsesor().getActivo());
        }

        if (usuario.getEstudiante() != null) {
            return Boolean.TRUE.equals(usuario.getEstudiante().getActivo());
        }

        if (usuario.getMonitor() != null) {
            return Boolean.TRUE.equals(usuario.getMonitor().getActivo());
        }

        if (usuario.getAdministrativo() != null) {
            return Boolean.TRUE.equals(usuario.getAdministrativo().getActivo());
        }

        if (usuario.getConciliador() != null) {
            return Boolean.TRUE.equals(usuario.getConciliador().getActivo());
        }

        return false;
    }

    // Convierte los permisos activos del rol en permisos de Spring Security
    // Para luego usarlos con anotaciones @PreAutorize en los controllers
    private List<SimpleGrantedAuthority> obtenerAuthorities(UsuarioSistema usuario) {
        return usuario.getRol().getPermisos()
                .stream()
                .filter(permiso -> Boolean.TRUE.equals(permiso.getActivo()))
                .map(Permiso::getNombre)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}