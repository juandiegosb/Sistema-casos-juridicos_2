package co.edu.ufps.legal_cases.security.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.model.Administrativo;
import co.edu.ufps.legal_cases.business.model.Asesor;
import co.edu.ufps.legal_cases.business.model.Conciliador;
import co.edu.ufps.legal_cases.business.model.Estudiante;
import co.edu.ufps.legal_cases.business.model.Monitor;
import co.edu.ufps.legal_cases.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.Rol;
import co.edu.ufps.legal_cases.security.model.TipoPerfilUsuario;
import co.edu.ufps.legal_cases.security.model.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.RolRepository;
import co.edu.ufps.legal_cases.security.repository.UsuarioSistemaRepository;

import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarNumeroDocumento;

// Service interno para crear usuarios del sistema a partir de perfiles reales del negocio.
//Por esto no uso dto, sino directamente las entidades de negocio. 
//Lo uso en los servicios de cada perfil (AsesorService, EstudianteService, etc) para crear el usuario del sistema justo después de crear el perfil real.
@Service
@Transactional
public class UsuarioSistemaRegistroService {

    private static final String ROL_ASESOR = "Asesor";
    private static final String ROL_ESTUDIANTE = "Estudiante";
    private static final String ROL_MONITOR = "Monitor";
    private static final String ROL_ADMINISTRADOR = "Administrador";
    private static final String ROL_CONCILIADOR = "Conciliador";

    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioSistemaRegistroService(
            UsuarioSistemaRepository usuarioSistemaRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioSistema crearParaAsesor(Asesor asesor) {
        validarPerfilAsesor(asesor);

        UsuarioSistema usuario = crearUsuarioBase(
                asesor.getEmail(),
                asesor.getDocumento(),
                ROL_ASESOR,
                TipoPerfilUsuario.ASESOR
        );

        // Relación vieja temporal.
        // Se mantiene para no romper login, /me ni validaciones actuales mientras se migra.
        usuario.setAsesor(asesor);
        validarUnSoloPerfil(usuario);

        return usuarioSistemaRepository.save(usuario);
    }

    public UsuarioSistema crearParaEstudiante(Estudiante estudiante) {
        validarPerfilEstudiante(estudiante);

        UsuarioSistema usuario = crearUsuarioBase(
                estudiante.getEmail(),
                estudiante.getDocumento(),
                ROL_ESTUDIANTE,
                TipoPerfilUsuario.ESTUDIANTE
        );

        // Relación vieja temporal.
        // Se mantiene para no romper login, /me ni validaciones actuales mientras se migra.
        usuario.setEstudiante(estudiante);
        validarUnSoloPerfil(usuario);

        return usuarioSistemaRepository.save(usuario);
    }

    public UsuarioSistema crearParaMonitor(Monitor monitor) {
        validarPerfilMonitor(monitor);

        UsuarioSistema usuario = crearUsuarioBase(
                monitor.getEmail(),
                monitor.getDocumento(),
                ROL_MONITOR,
                TipoPerfilUsuario.MONITOR
        );

        // Relación vieja temporal.
        // Se mantiene para no romper login, /me ni validaciones actuales mientras se migra.
        usuario.setMonitor(monitor);
        validarUnSoloPerfil(usuario);

        return usuarioSistemaRepository.save(usuario);
    }

    public UsuarioSistema crearParaAdministrativo(Administrativo administrativo) {
        validarPerfilAdministrativo(administrativo);

        UsuarioSistema usuario = crearUsuarioBase(
                administrativo.getEmail(),
                administrativo.getDocumento(),
                ROL_ADMINISTRADOR,
                TipoPerfilUsuario.ADMINISTRATIVO
        );

        // Relación vieja temporal.
        // Se mantiene para no romper login, /me ni validaciones actuales mientras se migra.
        usuario.setAdministrativo(administrativo);
        validarUnSoloPerfil(usuario);

        return usuarioSistemaRepository.save(usuario);
    }

    public void crearParaConciliador(Conciliador conciliador) {
        validarPerfilConciliador(conciliador);

        UsuarioSistema usuario = crearUsuarioBase(
                conciliador.getEmail(),
                conciliador.getDocumento(),
                ROL_CONCILIADOR,
                TipoPerfilUsuario.CONCILIADOR
        );

        usuario.setConciliador(conciliador);
        validarUnSoloPerfil(usuario);

        usuarioSistemaRepository.save(usuario);
    }

    private UsuarioSistema crearUsuarioBase(
            String email,
            String documento,
            String nombreRol,
            TipoPerfilUsuario tipoPerfilActual) {

        String username = normalizarEmail(email);
        String passwordInicial = normalizarNumeroDocumento(documento);
        Rol rol = obtenerRolBaseActivo(nombreRol);

        if (username == null) {
            throw new BusinessException("El correo es obligatorio para crear el usuario del sistema");
        }

        if (passwordInicial == null) {
            throw new BusinessException("El documento es obligatorio para crear la contraseña inicial");
        }

        if (tipoPerfilActual == null) {
            throw new BusinessException("El tipo de perfil actual es obligatorio para crear el usuario del sistema");
        }

        if (usuarioSistemaRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("Ya existe un usuario del sistema con ese correo");
        }

        UsuarioSistema usuario = new UsuarioSistema();
        usuario.setUsername(username);

        // La contraseña inicial es el documento, pero se guarda cifrada.
        usuario.setPasswordHash(passwordEncoder.encode(passwordInicial));

        usuario.setRol(rol);
        usuario.setActivo(true);

        // Nuevo modelo de normalización.
        // Define cuál es el perfil real activo del usuario sin depender a futuro
        // de asesor_id, estudiante_id, monitor_id, administrativo_id o conciliador_id.
        usuario.setTipoPerfilActual(tipoPerfilActual);

        return usuario;
    }

    private Rol obtenerRolBaseActivo(String nombreRol) {
        return rolRepository.findByNombreIgnoreCaseAndActivoTrue(nombreRol)
                .orElseThrow(() -> new BusinessException(
                        "Rol base no encontrado o inactivo: " + nombreRol));
    }

    private void validarPerfilAsesor(Asesor asesor) {
        if (asesor == null || asesor.getId() == null) {
            throw new BusinessException("El asesor es obligatorio para crear el usuario del sistema");
        }

        if (usuarioSistemaRepository.existsByAsesor_Id(asesor.getId())) {
            throw new BusinessException("Ese asesor ya tiene usuario del sistema");
        }
    }

    private void validarPerfilEstudiante(Estudiante estudiante) {
        if (estudiante == null || estudiante.getId() == null) {
            throw new BusinessException("El estudiante es obligatorio para crear el usuario del sistema");
        }

        if (usuarioSistemaRepository.existsByEstudiante_Id(estudiante.getId())) {
            throw new BusinessException("Ese estudiante ya tiene usuario del sistema");
        }
    }

    private void validarPerfilMonitor(Monitor monitor) {
        if (monitor == null || monitor.getId() == null) {
            throw new BusinessException("El monitor es obligatorio para crear el usuario del sistema");
        }

        if (usuarioSistemaRepository.existsByMonitor_Id(monitor.getId())) {
            throw new BusinessException("Ese monitor ya tiene usuario del sistema");
        }
    }

    private void validarPerfilAdministrativo(Administrativo administrativo) {
        if (administrativo == null || administrativo.getId() == null) {
            throw new BusinessException("El administrativo es obligatorio para crear el usuario del sistema");
        }

        if (usuarioSistemaRepository.existsByAdministrativo_Id(administrativo.getId())) {
            throw new BusinessException("Ese administrativo ya tiene usuario del sistema");
        }
    }

    private void validarPerfilConciliador(Conciliador conciliador) {
        if (conciliador == null || conciliador.getId() == null) {
            throw new BusinessException("El conciliador es obligatorio para crear el usuario del sistema");
        }

        if (usuarioSistemaRepository.existsByConciliador_Id(conciliador.getId())) {
            throw new BusinessException("Ese conciliador ya tiene usuario del sistema");
        }
    }

    // Garantiza que el usuario del sistema quede asociado exactamente a un perfil real.
    private void validarUnSoloPerfil(UsuarioSistema usuario) {
        int cantidadPerfiles = 0;

        if (usuario.getAsesor() != null) {
            cantidadPerfiles++;
        }

        if (usuario.getEstudiante() != null) {
            cantidadPerfiles++;
        }

        if (usuario.getMonitor() != null) {
            cantidadPerfiles++;
        }

        if (usuario.getAdministrativo() != null) {
            cantidadPerfiles++;
        }

        if (usuario.getConciliador() != null) {
            cantidadPerfiles++;
        }

        if (cantidadPerfiles != 1) {
            throw new BusinessException("El usuario del sistema debe estar asociado exactamente a un perfil");
        }
    }
}