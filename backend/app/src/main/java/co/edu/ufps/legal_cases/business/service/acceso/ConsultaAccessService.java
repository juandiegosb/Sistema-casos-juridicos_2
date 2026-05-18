package co.edu.ufps.legal_cases.business.service.acceso;

import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.ARCHIVAR_CONSULTAS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.ASIGNAR_RESPONSABLES_CONSULTA;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.CAMBIAR_ESTADO_CONSULTAS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.CREAR_CONSULTAS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.EDITAR_CONSULTAS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.VER_CONSULTAS;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;
import co.edu.ufps.legal_cases.security.service.context.UsuarioActualService;

@Service
public class ConsultaAccessService {

    private final UsuarioActualService usuarioActualService;
    private final ConsultaRepository consultaRepository;

    public ConsultaAccessService(
            UsuarioActualService usuarioActualService,
            ConsultaRepository consultaRepository) {
        this.usuarioActualService = usuarioActualService;
        this.consultaRepository = consultaRepository;
    }

    // Metodos para validar cada permiso de consulta que puede tener cada usuario

    @Transactional(readOnly = true)
    public void validarPuedeVerConsulta(Long consultaId) {
        validarTienePermiso(VER_CONSULTAS); // Primero ve si tiene permiso global para ver consultas

        Consulta consulta = obtenerConsulta(consultaId);

        if (!puedeAccederAConsulta(consulta)) { // MIra si el usuario si esta asociado a la consulta o no
            throw new AccessDeniedException("No tiene permisos para ver esta consulta");
        }
    }

    @Transactional(readOnly = true)
    public void validarPuedeCrearConsulta() {
        validarTienePermiso(CREAR_CONSULTAS);
    }

    @Transactional(readOnly = true)
    public void validarPuedeEditarConsulta(Long consultaId) {
        validarTienePermiso(EDITAR_CONSULTAS);

        Consulta consulta = obtenerConsulta(consultaId);

        if (!puedeAccederAConsulta(consulta)) {
            throw new AccessDeniedException("No tiene permisos para editar esta consulta");
        }
    }

    @Transactional(readOnly = true)
    public void validarPuedeCambiarEstadoConsulta(Long consultaId) {
        validarTienePermiso(CAMBIAR_ESTADO_CONSULTAS);

        Consulta consulta = obtenerConsulta(consultaId);

        if (!puedeAccederAConsulta(consulta)) {
            throw new AccessDeniedException("No tiene permisos para cambiar el estado de esta consulta");
        }

        if (usuarioActualService.esEstudiante()) {
            throw new AccessDeniedException("El estudiante no puede cambiar el estado de la consulta");
        }
    }

    @Transactional(readOnly = true)
    public void validarPuedeArchivarConsulta(Long consultaId) {
        validarTienePermiso(ARCHIVAR_CONSULTAS);

        if (!usuarioActualService.esRolAdministrador()) {
            throw new AccessDeniedException("Solo el administrador puede archivar consultas");
        }

        obtenerConsulta(consultaId);
    }

    @Transactional(readOnly = true)
    public void validarPuedeAsignarResponsablesConsulta() {
        validarTienePermiso(ASIGNAR_RESPONSABLES_CONSULTA);

        if (!usuarioActualService.esRolAdministrador()) {
            throw new AccessDeniedException("Solo el administrador puede asignar responsables de consulta");
        }
    }

    @Transactional(readOnly = true)
    public boolean puedeVerTodasLasConsultas() {
        return usuarioActualService.esRolAdministrador();
    }

    @Transactional(readOnly = true)
    public boolean puedeAccederAConsulta(Long consultaId) {
        Consulta consulta = obtenerConsulta(consultaId);
        return puedeAccederAConsulta(consulta);
    }

    // Aqui se verifica si el usuario tiene relacion con una consulta para que la puede ver o editar
    @Transactional(readOnly = true)
    public boolean puedeAccederAConsulta(Consulta consulta) {
        if (consulta == null) {
            return false;
        }

        // El administrador tiene acceso a todo de consultas
        if (usuarioActualService.esRolAdministrador()) {
            return true;
        }

        PerfilUsuarioActual perfil = usuarioActualService.obtenerPerfilActual();
        Long perfilId = perfil.getPerfilId();

        // Aqui se valida que el usuario tenga relacion con la consulta para que la vea o edite
        if (perfil.getTipoPerfil() == TipoPerfilUsuario.ESTUDIANTE) {
            return consulta.getEstudiante() != null
                    && Objects.equals(consulta.getEstudiante().getId(), perfilId);
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.ASESOR) {
            boolean asesorAsignado = consulta.getAsesor() != null
                    && Objects.equals(consulta.getAsesor().getId(), perfilId);

            boolean estudianteDelAsesor = consulta.getEstudiante() != null
                    && consulta.getEstudiante().getAsesor() != null
                    && Objects.equals(consulta.getEstudiante().getAsesor().getId(), perfilId);

            return asesorAsignado || estudianteDelAsesor;
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.MONITOR) {
            return consulta.getMonitor() != null
                    && Objects.equals(consulta.getMonitor().getId(), perfilId);
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.CONCILIADOR) {
            // Cuando se implemente el módulo de conciliaciones, aquí se validará:
            // conciliacion -> consulta.
            // Por ahora no se concede acceso global al conciliador.
            return false;
        }

        return false;
    }

    // Aqui se revisa si el usuario tiene permisos particulares de edicion o no

    public boolean usuarioPuedeAsignarResponsables() {
        return usuarioActualService.esRolAdministrador()
                && usuarioActualService.tienePermiso(ASIGNAR_RESPONSABLES_CONSULTA);
    }

    public boolean usuarioPuedeCambiarEstado() {
        return usuarioActualService.tienePermiso(CAMBIAR_ESTADO_CONSULTAS)
                && !usuarioActualService.esEstudiante();
    }

    // Metodos particulares para validar el tipo de usuario logueado

    public boolean usuarioEsEstudiante() {
        return usuarioActualService.esEstudiante();
    }

    public boolean usuarioEsAsesor() {
        return usuarioActualService.esAsesor();
    }

    public boolean usuarioEsMonitor() {
        return usuarioActualService.esMonitor();
    }

    public boolean usuarioEsAdministrador() {
        return usuarioActualService.esRolAdministrador();
    }

    @Transactional(readOnly = true)
    public PerfilUsuarioActual obtenerPerfilActual() {
        return usuarioActualService.obtenerPerfilActual();
    }

    private Consulta obtenerConsulta(Long consultaId) {
        if (consultaId == null) {
            throw new BusinessException("La consulta es obligatoria");
        }

        return consultaRepository.findById(consultaId)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + consultaId));
    }

    private void validarTienePermiso(String permiso) {
        if (!usuarioActualService.tienePermiso(permiso)) {
            throw new AccessDeniedException("No tiene el permiso requerido: " + permiso);
        }
    }
}