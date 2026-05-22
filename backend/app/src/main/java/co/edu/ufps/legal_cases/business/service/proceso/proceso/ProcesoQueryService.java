package co.edu.ufps.legal_cases.business.service.proceso.proceso;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.proceso.ProcesoDTO;
import co.edu.ufps.legal_cases.business.model.proceso.Proceso;
import co.edu.ufps.legal_cases.business.repository.proceso.ProcesoRepository;
import co.edu.ufps.legal_cases.business.service.acceso.ProcesoAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class ProcesoQueryService {

    private final ProcesoRepository procesoRepository;
    private final ProcesoAccessService procesoAccessService;
    private final ProcesoMapper procesoMapper;

    public ProcesoQueryService(
            ProcesoRepository procesoRepository,
            ProcesoAccessService procesoAccessService,
            ProcesoMapper procesoMapper) {
        this.procesoRepository = procesoRepository;
        this.procesoAccessService = procesoAccessService;
        this.procesoMapper = procesoMapper;
    }

    // Lee procesos activos y aplica alcance registro por registro.
    // El permiso general ya existe, pero el listado no debe exponer procesos fuera del alcance del usuario.
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listar() {
        procesoAccessService.validarPuedeListarProcesos();

        return procesoRepository.findByActivoTrueOrderByIdDesc()
                .stream()
                .filter(procesoAccessService::puedeAccederAProceso)
                .map(procesoMapper::convertirADTO)
                .toList();
    }

    // Obtiene un proceso activo específico después de validar acceso sobre ese registro.
    @Transactional(readOnly = true)
    public ProcesoDTO obtenerPorId(Long id) {
        procesoAccessService.validarPuedeVerProceso(id);

        Proceso proceso = buscarProcesoActivo(id);

        return procesoMapper.convertirADTO(proceso);
    }

    private Proceso buscarProcesoActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id del proceso es obligatorio");
        }

        return procesoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Proceso no encontrado con id: " + id));
    }
}