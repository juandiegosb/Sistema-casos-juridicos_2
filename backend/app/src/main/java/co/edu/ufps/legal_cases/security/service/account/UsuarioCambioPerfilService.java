package co.edu.ufps.legal_cases.security.service.account;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.dto.account.UsuarioSistemaDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAAdministrativoDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAAsesorDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAConciliadorDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAEstudianteDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAMonitorDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilBaseDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.ResultadoCambioPerfil;
import co.edu.ufps.legal_cases.security.model.access.Rol;
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.access.RolRepository;
import co.edu.ufps.legal_cases.security.repository.account.UsuarioSistemaRepository;
import co.edu.ufps.legal_cases.security.service.account.cambio.PerfilCambioHandler;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;

// Este servicio es el que va a orquestar todo el cambio desde escribir el historial hasta orquestar los perfiles actualizados
@Service
@Transactional
public class UsuarioCambioPerfilService {

    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final RolRepository rolRepository;
    private final PerfilUsuarioResolverService perfilUsuarioResolverService;
    private final PerfilEstadoService perfilEstadoService;
    private final UsuarioCambioPerfilHistorialService historialService;
    private final UsuarioSistemaService usuarioSistemaService;
    private final Map<TipoPerfilUsuario, PerfilCambioHandler<? extends CambiarPerfilBaseDTO>> handlers; // Trae a todos
                                                                                                        // los
                                                                                                        // manejadores
                                                                                                        // por cada
                                                                                                        // perfil

    public UsuarioCambioPerfilService(
            UsuarioSistemaRepository usuarioSistemaRepository,
            RolRepository rolRepository,
            PerfilUsuarioResolverService perfilUsuarioResolverService,
            PerfilEstadoService perfilEstadoService,
            UsuarioCambioPerfilHistorialService historialService,
            UsuarioSistemaService usuarioSistemaService,
            List<PerfilCambioHandler<? extends CambiarPerfilBaseDTO>> handlers) {
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.rolRepository = rolRepository;
        this.perfilUsuarioResolverService = perfilUsuarioResolverService;
        this.perfilEstadoService = perfilEstadoService;
        this.historialService = historialService;
        this.usuarioSistemaService = usuarioSistemaService;
        this.handlers = construirMapaHandlers(handlers);
    }

    // Metodos para que el controller use y no tenga destras la logica de negocio
    public UsuarioSistemaDTO cambiarAAdministrativo(
            Long usuarioSistemaId,
            CambiarPerfilAAdministrativoDTO dto,
            String cambiadoPorUsername) {

        return cambiarPerfil(
                usuarioSistemaId,
                TipoPerfilUsuario.ADMINISTRATIVO,
                dto,
                cambiadoPorUsername);
    }

    public UsuarioSistemaDTO cambiarAEstudiante(
            Long usuarioSistemaId,
            CambiarPerfilAEstudianteDTO dto,
            String cambiadoPorUsername) {

        return cambiarPerfil(
                usuarioSistemaId,
                TipoPerfilUsuario.ESTUDIANTE,
                dto,
                cambiadoPorUsername);
    }

    public UsuarioSistemaDTO cambiarAAsesor(
            Long usuarioSistemaId,
            CambiarPerfilAAsesorDTO dto,
            String cambiadoPorUsername) {

        return cambiarPerfil(
                usuarioSistemaId,
                TipoPerfilUsuario.ASESOR,
                dto,
                cambiadoPorUsername);
    }

    public UsuarioSistemaDTO cambiarAMonitor(
            Long usuarioSistemaId,
            CambiarPerfilAMonitorDTO dto,
            String cambiadoPorUsername) {

        return cambiarPerfil(
                usuarioSistemaId,
                TipoPerfilUsuario.MONITOR,
                dto,
                cambiadoPorUsername);
    }

    public UsuarioSistemaDTO cambiarAConciliador(
            Long usuarioSistemaId,
            CambiarPerfilAConciliadorDTO dto,
            String cambiadoPorUsername) {

        return cambiarPerfil(
                usuarioSistemaId,
                TipoPerfilUsuario.CONCILIADOR,
                dto,
                cambiadoPorUsername);
    }

    private <T extends CambiarPerfilBaseDTO> UsuarioSistemaDTO cambiarPerfil(
            Long usuarioSistemaId,
            TipoPerfilUsuario tipoPerfilDestino,
            T dto,
            String cambiadoPorUsername) {

        // Valida que los datos básicos para hacer el cambio no vengan vacíos
        validarDatosCambio(usuarioSistemaId, tipoPerfilDestino, dto);

        // Busca el usuario del sistema que se va a cambiar y el perfil que tiene
        // actualmente
        UsuarioSistema usuario = obtenerUsuarioSistemaActivo(usuarioSistemaId);
        PerfilUsuarioActual perfilAnterior = obtenerPerfilAnteriorValidandoDestino(usuario, tipoPerfilDestino);

        Rol rolAnterior = usuario.getRol();
        // Busca y valida el rol nuevo que tendrá el usuario y mira que los 2 esten
        // relacionados
        Rol rolNuevo = obtenerRolDestino(dto.getRolId(), tipoPerfilDestino);

        UsuarioSistema cambiadoPorUsuario = obtenerUsuarioCambiador(cambiadoPorUsername);

        // Desactiva el perfil anterior del usuario.
        perfilEstadoService.desactivarPerfilActual(
                usuario.getId(),
                perfilAnterior.getTipoPerfil());

        // Busca el handler correspondiente al perfil destino y se le pide que
        // cree o actualice el nuevo perfil
        ResultadoCambioPerfil resultadoCambio = obtenerHandler(tipoPerfilDestino, dto).crearOActualizarPerfil(usuario,
                dto);

        // Le cambia el rol y el tipo de perfil en la tabla de usuario del sistema
        actualizarUsuarioSistema(usuario, rolNuevo, tipoPerfilDestino);

        // Registra el cambio
        historialService.registrarCambio(
                usuario,
                perfilAnterior,
                rolAnterior,
                resultadoCambio.getTipoPerfil(),
                resultadoCambio.getPerfilId(),
                rolNuevo,
                dto.getMotivo(),
                cambiadoPorUsuario,
                cambiadoPorUsername);

        return usuarioSistemaService.obtenerPorId(usuario.getId());
    }

    // En una especie de diccionario organiza por tipo de perfil el manejador de
    // cada uno
    private Map<TipoPerfilUsuario, PerfilCambioHandler<? extends CambiarPerfilBaseDTO>> construirMapaHandlers(
            List<PerfilCambioHandler<? extends CambiarPerfilBaseDTO>> handlers) {

        Map<TipoPerfilUsuario, PerfilCambioHandler<? extends CambiarPerfilBaseDTO>> mapa = new EnumMap<>(
                TipoPerfilUsuario.class);

        // Recorre todos los manejadores para irlos asociando a un tipo de perfil
        for (PerfilCambioHandler<? extends CambiarPerfilBaseDTO> handler : handlers) {
            // Verifica que no halla ya un mas de un manejador asociado
            if (mapa.containsKey(handler.getTipoPerfil())) {
                throw new BusinessException(
                        "Hay más de un handler registrado para el perfil " + handler.getTipoPerfil());
            }

            mapa.put(handler.getTipoPerfil(), handler);
        }

        return mapa;
    }

    // Como se usa de forma generica se debe validar que exista un manejador de
    // cambio y un dto con los datos para el cambio, tambien que el manejador de
    // cambio
    @SuppressWarnings("unchecked") // Esto es para que no muestre la advertencia por el cast, porque ya validé el
                                   // tipo antes.
    private <T extends CambiarPerfilBaseDTO> PerfilCambioHandler<T> obtenerHandler(
            TipoPerfilUsuario tipoPerfilDestino,
            T dto) {

        // Obtiene el manejador del perfil al cual va a cambiar
        PerfilCambioHandler<? extends CambiarPerfilBaseDTO> handler = handlers.get(tipoPerfilDestino);

        if (handler == null) {
            throw new BusinessException("No existe handler para el perfil destino " + tipoPerfilDestino);
        }

        // Verifica que el dto sea el que el manejador usa
        if (!handler.getDtoClass().isInstance(dto)) {
            throw new BusinessException("Los datos enviados no corresponden al perfil destino " + tipoPerfilDestino);
        }

        return (PerfilCambioHandler<T>) handler;
    }

    // Valida nulos
    private void validarDatosCambio(
            Long usuarioSistemaId,
            TipoPerfilUsuario tipoPerfilDestino,
            CambiarPerfilBaseDTO dto) {

        if (usuarioSistemaId == null) {
            throw new BusinessException("El id del usuario del sistema es obligatorio");
        }

        if (tipoPerfilDestino == null) {
            throw new BusinessException("El tipo de perfil destino es obligatorio");
        }

        if (dto == null) {
            throw new BusinessException("Los datos para el cambio de perfil son obligatorios");
        }
    }

    // Se asegura que el que quiere cambiar de perfil pueda hacerlo (si es inactivo
    // no puede)
    private UsuarioSistema obtenerUsuarioSistemaActivo(Long usuarioSistemaId) {
        UsuarioSistema usuario = usuarioSistemaRepository.findWithRolAndPermisosById(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "Usuario del sistema no encontrado con id: " + usuarioSistemaId));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessException("No se puede cambiar el perfil de un usuario inactivo");
        }

        if (usuario.getRol() == null || !Boolean.TRUE.equals(usuario.getRol().getActivo())) {
            throw new BusinessException("No se puede cambiar el perfil porque el rol actual está inactivo");
        }

        return usuario;
    }

    // Valida que el cambio no vaya a ser del mismo perfil (No se cambia por el
    // mismo)
    private PerfilUsuarioActual obtenerPerfilAnteriorValidandoDestino(
            UsuarioSistema usuario,
            TipoPerfilUsuario tipoPerfilDestino) {

        PerfilUsuarioActual perfilAnterior = perfilUsuarioResolverService.obtenerPerfilActivoObligatorio(usuario);

        if (perfilAnterior.getTipoPerfil().equals(tipoPerfilDestino)) {
            throw new BusinessException("El usuario ya tiene activo el perfil " + tipoPerfilDestino);
        }

        return perfilAnterior;
    }

    // Aqui valida la existencia del rol y del perfil al que se quiere cambiar
    // tambien se valida que el peril y el rol esten relacionados
    // no puede haber un rol: Admin -> perfil: estudiante, los dos deben ser iguales
    private Rol obtenerRolDestino(Long rolId, TipoPerfilUsuario tipoPerfilDestino) {
        if (rolId == null) {
            throw new BusinessException("El rol destino es obligatorio");
        }

        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new BusinessException("Rol no encontrado con id: " + rolId));

        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new BusinessException("El rol destino se encuentra inactivo");
        }

        if (rol.getTipoPerfil() == null) {
            throw new BusinessException("El rol destino no tiene tipo de perfil configurado");
        }

        if (!rol.getTipoPerfil().equals(tipoPerfilDestino)) {
            throw new BusinessException("El rol destino no corresponde al perfil " + tipoPerfilDestino);
        }

        return rol;
    }

    // Es para buscar a el usuario del sistema que se va a someter a cambio
    private UsuarioSistema obtenerUsuarioCambiador(String cambiadoPorUsername) {
        String username = normalizarEmail(cambiadoPorUsername);

        if (username == null) {
            return null;
        }

        return usuarioSistemaRepository.findByUsernameIgnoreCase(username)
                .orElse(null);
    }

    // Cuando ya se tiene un historial de cambio de perfil/rol entonces solo se
    // actualiza
    // el usuario de sistema con el nuevo rol y tipo de perfil
    private void actualizarUsuarioSistema(
            UsuarioSistema usuario,
            Rol rolNuevo,
            TipoPerfilUsuario tipoPerfilNuevo) {

        usuario.setRol(rolNuevo);
        usuario.setTipoPerfilActual(tipoPerfilNuevo);

        usuarioSistemaRepository.save(usuario);
    }

}