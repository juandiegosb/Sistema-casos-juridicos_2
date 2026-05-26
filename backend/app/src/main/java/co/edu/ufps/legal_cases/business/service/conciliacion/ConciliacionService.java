package co.edu.ufps.legal_cases.business.service.conciliacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import co.edu.ufps.legal_cases.business.dto.conciliacion.ConciliacionDetalleResponseDTO;
import co.edu.ufps.legal_cases.business.dto.conciliacion.ConciliacionResponseDTO;
import co.edu.ufps.legal_cases.business.service.conciliacion.conciliacion.ConciliacionCommandService;
import co.edu.ufps.legal_cases.business.service.conciliacion.conciliacion.ConciliacionQueryService;
import lombok.AllArgsConstructor;

// Fachada del módulo de conciliación.
// El controller usa este service y no conoce CommandService ni QueryService.
@Service
@AllArgsConstructor
public class ConciliacionService {

    private final ConciliacionCommandService conciliacionCommandService;
    private final ConciliacionQueryService conciliacionQueryService;

    public List<ConciliacionResponseDTO> listar() {
        return conciliacionQueryService.listar();
    }

    public List<ConciliacionResponseDTO> listarPorConsulta(Long consultaId) {
        return conciliacionQueryService.listarPorConsulta(consultaId);
    }

    public ConciliacionDetalleResponseDTO obtenerDetalle(Long id) {
        return conciliacionQueryService.obtenerDetalle(id);
    }

    public ConciliacionResponseDTO crearDesdeConsulta(Long consultaId, MultipartFile solicitud) {
        return conciliacionCommandService.crearDesdeConsulta(consultaId, solicitud);
    }

    public ConciliacionResponseDTO asignarEstudiante(Long id, Long estudianteId) {
        return conciliacionCommandService.asignarEstudiante(id, estudianteId);
    }

    public ConciliacionResponseDTO asignarConciliador(Long id, Long conciliadorId) {
        return conciliacionCommandService.asignarConciliador(id, conciliadorId);
    }

    public ConciliacionResponseDTO cambiarEstado(Long id, String estado) {
        return conciliacionCommandService.cambiarEstado(id, estado);
    }

    public ConciliacionResponseDTO finalizar(Long id, String estado, MultipartFile acta) {
        return conciliacionCommandService.finalizar(id, estado, acta);
    }

    public ConciliacionResponseDTO reemplazarSolicitud(Long id, MultipartFile solicitud) {
        return conciliacionCommandService.reemplazarSolicitud(id, solicitud);
    }

    public void desactivar(Long id) {
        conciliacionCommandService.desactivar(id);
    }
}