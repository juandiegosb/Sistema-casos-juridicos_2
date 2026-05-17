package co.edu.ufps.legal_cases.security.service.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.model.perfil.Administrativo;
import co.edu.ufps.legal_cases.business.model.perfil.Asesor;
import co.edu.ufps.legal_cases.business.model.perfil.Conciliador;
import co.edu.ufps.legal_cases.business.model.perfil.Estudiante;
import co.edu.ufps.legal_cases.business.model.perfil.Monitor;
import co.edu.ufps.legal_cases.business.repository.perfil.AdministrativoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.AsesorRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.ConciliadorRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.EstudianteRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.MonitorRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;

// Este servicio se encarga de buscar el perfil activo por la id del usuario del sistema
// que se esta autenticando con ayuda del TipoPerfilUsuario que esta en el usuario de sistema
@Service
@Transactional(readOnly = true, noRollbackFor = BusinessException.class)
public class PerfilUsuarioResolverService {

    private final EstudianteRepository estudianteRepository;
    private final AsesorRepository asesorRepository;
    private final MonitorRepository monitorRepository;
    private final AdministrativoRepository administrativoRepository;
    private final ConciliadorRepository conciliadorRepository;

    public PerfilUsuarioResolverService(
            EstudianteRepository estudianteRepository,
            AsesorRepository asesorRepository,
            MonitorRepository monitorRepository,
            AdministrativoRepository administrativoRepository,
            ConciliadorRepository conciliadorRepository) {
        this.estudianteRepository = estudianteRepository;
        this.asesorRepository = asesorRepository;
        this.monitorRepository = monitorRepository;
        this.administrativoRepository = administrativoRepository;
        this.conciliadorRepository = conciliadorRepository;
    }

    public PerfilUsuarioActual obtenerPerfilActivoObligatorio(UsuarioSistema usuario) {
        validarUsuario(usuario);

        TipoPerfilUsuario tipoPerfil = usuario.getTipoPerfilActual();

        if (tipoPerfil == null) {
            throw new BusinessException("El usuario del sistema no tiene tipo de perfil actual definido");
        }

        // como el usuario del sistema tiene el tipo de perfil entonces por ese tipo sabe en que tabla buscar
        return switch (tipoPerfil) {
            case ESTUDIANTE -> obtenerEstudianteActivo(usuario.getId());
            case ASESOR -> obtenerAsesorActivo(usuario.getId());
            case MONITOR -> obtenerMonitorActivo(usuario.getId());
            case ADMINISTRATIVO -> obtenerAdministrativoActivo(usuario.getId());
            case CONCILIADOR -> obtenerConciliadorActivo(usuario.getId());
        };
    }

    public boolean tienePerfilActivo(UsuarioSistema usuario) {
        try {
            obtenerPerfilActivoObligatorio(usuario);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    // Metodos para obtener el id del perfil asociado a la id de usuario del sistema que se esta autenticado
    private PerfilUsuarioActual obtenerEstudianteActivo(Long usuarioSistemaId) {
        Estudiante estudiante = estudianteRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El estudiante asociado al usuario no existe o se encuentra inactivo"));

        return new PerfilUsuarioActual(estudiante.getId(), TipoPerfilUsuario.ESTUDIANTE);
    }

    private PerfilUsuarioActual obtenerAsesorActivo(Long usuarioSistemaId) {
        Asesor asesor = asesorRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El asesor asociado al usuario no existe o se encuentra inactivo"));

        return new PerfilUsuarioActual(asesor.getId(), TipoPerfilUsuario.ASESOR);
    }

    private PerfilUsuarioActual obtenerMonitorActivo(Long usuarioSistemaId) {
        Monitor monitor = monitorRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El monitor asociado al usuario no existe o se encuentra inactivo"));

        return new PerfilUsuarioActual(monitor.getId(), TipoPerfilUsuario.MONITOR);
    }

    private PerfilUsuarioActual obtenerAdministrativoActivo(Long usuarioSistemaId) {
        Administrativo administrativo = administrativoRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El administrativo asociado al usuario no existe o se encuentra inactivo"));

        return new PerfilUsuarioActual(administrativo.getId(), TipoPerfilUsuario.ADMINISTRATIVO);
    }

    private PerfilUsuarioActual obtenerConciliadorActivo(Long usuarioSistemaId) {
        Conciliador conciliador = conciliadorRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El conciliador asociado al usuario no existe o se encuentra inactivo"));

        return new PerfilUsuarioActual(conciliador.getId(), TipoPerfilUsuario.CONCILIADOR);
    }

    private void validarUsuario(UsuarioSistema usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new BusinessException("El usuario del sistema es obligatorio para resolver el perfil");
        }
    }
}