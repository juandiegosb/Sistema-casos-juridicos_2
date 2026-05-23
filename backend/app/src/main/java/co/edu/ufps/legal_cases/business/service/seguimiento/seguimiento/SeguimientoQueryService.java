package co.edu.ufps.legal_cases.business.service.seguimiento.seguimiento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoResponseDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoRepository;
import co.edu.ufps.legal_cases.business.service.acceso.seguimiento.SeguimientoAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class SeguimientoQueryService {

    private final SeguimientoRepository seguimientoRepository;
    private final SeguimientoAccessService seguimientoAccessService;
    private final SeguimientoMapper seguimientoMapper;
    private final SeguimientoValidator seguimientoValidator;

    public SeguimientoQueryService(
            SeguimientoRepository seguimientoRepository,
            SeguimientoAccessService seguimientoAccessService,
            SeguimientoMapper seguimientoMapper,
            SeguimientoValidator seguimientoValidator) {
        this.seguimientoRepository = seguimientoRepository;
        this.seguimientoAccessService = seguimientoAccessService;
        this.seguimientoMapper = seguimientoMapper;
        this.seguimientoValidator = seguimientoValidator;
    }

    // Lista seguimientos activos de una consulta después de validar alcance sobre esa consulta.
    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarPorConsulta(Long consultaId) {
        seguimientoAccessService.validarPuedeListarSeguimientosDeConsulta(consultaId);

        return seguimientoRepository.findByConsulta_IdAndActivoTrueOrderByFechaCreacionDesc(consultaId)
                .stream()
                .map(seguimientoMapper::convertirAResponseDTO)
                .toList();
    }

    // Para estudiante solo se muestran los seguimientos marcados como visibles.
    // Además se filtra por alcance para evitar exponer seguimientos de otra consulta.
    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarVisiblesParaEstudiantePorConsulta(Long consultaId) {
        seguimientoAccessService.validarPuedeListarSeguimientosVisiblesParaEstudiante(consultaId);

        return seguimientoRepository
                .findByConsulta_IdAndNotificarEstudianteTrueAndActivoTrueOrderByFechaCreacionDesc(consultaId)
                .stream()
                .filter(seguimientoAccessService::puedeVerSeguimiento)
                .map(seguimientoMapper::convertirAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarPorAutor(Long autorId) {
        seguimientoAccessService.validarPuedeListarSeguimientosPorAutor(autorId);

        return seguimientoRepository.findByAutor_IdAndActivoTrueOrderByFechaCreacionDesc(autorId)
                .stream()
                .filter(seguimientoAccessService::puedeVerSeguimiento)
                .map(seguimientoMapper::convertirAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarAlertasDisciplinarias() {
        seguimientoAccessService.validarPuedeListarAlertasDisciplinarias();

        return seguimientoRepository.findByAlertaDisciplinariaTrueAndActivoTrueOrderByFechaCreacionDesc()
                .stream()
                .map(seguimientoMapper::convertirAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarPorFechaEntrega(LocalDate fechaEntrega) {
        seguimientoAccessService.validarPuedeListarSeguimientosPorFechaEntrega();
        seguimientoValidator.validarFechaEntregaObligatoria(fechaEntrega);

        return seguimientoRepository.findByFechaEntregaAndActivoTrueOrderByFechaCreacionDesc(fechaEntrega)
                .stream()
                .filter(seguimientoAccessService::puedeVerSeguimiento)
                .map(seguimientoMapper::convertirAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeguimientoResponseDTO obtenerPorId(Long id) {
        seguimientoAccessService.validarPuedeVerSeguimiento(id);

        return seguimientoMapper.convertirAResponseDTO(buscarPorId(id));
    }

    private Seguimiento buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del seguimiento es obligatorio");
        }

        return seguimientoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Seguimiento no encontrado con id: " + id));
    }
}