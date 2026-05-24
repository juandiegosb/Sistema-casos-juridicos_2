package co.edu.ufps.legal_cases.security.service.account.perfil;

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
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;

// Aqui busco el perfil que voy a cambiar y lo desactivo
@Service
@Transactional
public class PerfilEstadoService {

    private final EstudianteRepository estudianteRepository;
    private final AsesorRepository asesorRepository;
    private final MonitorRepository monitorRepository;
    private final AdministrativoRepository administrativoRepository;
    private final ConciliadorRepository conciliadorRepository;

    public PerfilEstadoService(
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

    public void desactivarPerfilActual(Long usuarioSistemaId, TipoPerfilUsuario tipoPerfilActual) {
        if (usuarioSistemaId == null) {
            throw new BusinessException("El id del usuario del sistema es obligatorio");
        }

        if (tipoPerfilActual == null) {
            throw new BusinessException("El tipo de perfil actual es obligatorio");
        }

        switch (tipoPerfilActual) {
            case ESTUDIANTE -> desactivarEstudiante(usuarioSistemaId);
            case ASESOR -> desactivarAsesor(usuarioSistemaId);
            case MONITOR -> desactivarMonitor(usuarioSistemaId);
            case ADMINISTRATIVO -> desactivarAdministrativo(usuarioSistemaId);
            case CONCILIADOR -> desactivarConciliador(usuarioSistemaId);
        }
    }

    private void desactivarEstudiante(Long usuarioSistemaId) {
        Estudiante estudiante = estudianteRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El estudiante actual no existe o ya se encuentra inactivo"));

        estudiante.setActivo(false);
        estudianteRepository.save(estudiante);
    }

    private void desactivarAsesor(Long usuarioSistemaId) {
        Asesor asesor = asesorRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El asesor actual no existe o ya se encuentra inactivo"));

        asesor.setActivo(false);
        asesorRepository.save(asesor);
    }

    private void desactivarMonitor(Long usuarioSistemaId) {
        Monitor monitor = monitorRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El monitor actual no existe o ya se encuentra inactivo"));

        monitor.setActivo(false);
        monitorRepository.save(monitor);
    }

    private void desactivarAdministrativo(Long usuarioSistemaId) {
        Administrativo administrativo = administrativoRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El administrativo actual no existe o ya se encuentra inactivo"));

        administrativo.setActivo(false);
        administrativoRepository.save(administrativo);
    }

    private void desactivarConciliador(Long usuarioSistemaId) {
        Conciliador conciliador = conciliadorRepository.findByUsuarioSistema_IdAndActivoTrue(usuarioSistemaId)
                .orElseThrow(() -> new BusinessException(
                        "El conciliador actual no existe o ya se encuentra inactivo"));

        conciliador.setActivo(false);
        conciliadorRepository.save(conciliador);
    }
}