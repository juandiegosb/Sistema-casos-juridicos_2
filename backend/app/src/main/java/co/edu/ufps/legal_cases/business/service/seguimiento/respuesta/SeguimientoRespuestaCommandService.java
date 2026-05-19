package co.edu.ufps.legal_cases.business.service.seguimiento.respuesta;

import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.equalsIgnoreCase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.seguimiento.respuesta.SeguimientoRespuestaDecisionDTO;
import co.edu.ufps.legal_cases.business.dto.seguimiento.respuesta.SeguimientoRespuestaRequestDTO;
import co.edu.ufps.legal_cases.business.dto.seguimiento.respuesta.SeguimientoRespuestaResponseDTO;
import co.edu.ufps.legal_cases.business.model.perfil.Estudiante;
import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;
import co.edu.ufps.legal_cases.business.model.seguimiento.respuesta.EstadoRespuestaSeguimiento;
import co.edu.ufps.legal_cases.business.model.seguimiento.respuesta.SeguimientoRespuesta;
import co.edu.ufps.legal_cases.business.repository.perfil.EstudianteRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.respuesta.SeguimientoRespuestaRepository;
import co.edu.ufps.legal_cases.business.service.acceso.SeguimientoRespuestaAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.account.UsuarioSistemaRepository;

@Service
public class SeguimientoRespuestaCommandService {

    private final SeguimientoRespuestaRepository seguimientoRespuestaRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final SeguimientoRespuestaAccessService seguimientoRespuestaAccessService;
    private final SeguimientoRespuestaValidator seguimientoRespuestaValidator;
    private final SeguimientoRespuestaMapper seguimientoRespuestaMapper;

    public SeguimientoRespuestaCommandService(
            SeguimientoRespuestaRepository seguimientoRespuestaRepository,
            SeguimientoRepository seguimientoRepository,
            EstudianteRepository estudianteRepository,
            UsuarioSistemaRepository usuarioSistemaRepository,
            SeguimientoRespuestaAccessService seguimientoRespuestaAccessService,
            SeguimientoRespuestaValidator seguimientoRespuestaValidator,
            SeguimientoRespuestaMapper seguimientoRespuestaMapper) {
        this.seguimientoRespuestaRepository = seguimientoRespuestaRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.estudianteRepository = estudianteRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.seguimientoRespuestaAccessService = seguimientoRespuestaAccessService;
        this.seguimientoRespuestaValidator = seguimientoRespuestaValidator;
        this.seguimientoRespuestaMapper = seguimientoRespuestaMapper;
    }

    @Transactional
    public SeguimientoRespuestaResponseDTO crear(Long seguimientoId, SeguimientoRespuestaRequestDTO dto) {
        seguimientoRespuestaAccessService.validarPuedeResponderSeguimiento(seguimientoId);
        seguimientoRespuestaValidator.validarCreacion(dto);

        Long estudianteActualId = seguimientoRespuestaAccessService.obtenerEstudianteActualId();

        if (seguimientoRespuestaRepository.existsBySeguimiento_IdAndEstudiante_Id(seguimientoId, estudianteActualId)) {
            throw new BusinessException("Ya existe una respuesta para este seguimiento");
        }

        Seguimiento seguimiento = obtenerSeguimientoActivo(seguimientoId);
        Estudiante estudiante = obtenerEstudianteActivo(estudianteActualId);

        SeguimientoRespuesta respuesta = new SeguimientoRespuesta();
        respuesta.setSeguimiento(seguimiento);
        respuesta.setEstudiante(estudiante);
        respuesta.setContenido(seguimientoRespuestaValidator.normalizarContenido(dto.getContenido()));
        respuesta.setEstado(EstadoRespuestaSeguimiento.PENDIENTE);
        respuesta.setActivo(true);

        return seguimientoRespuestaMapper.convertirAResponseDTO(
                seguimientoRespuestaRepository.save(respuesta)
        );
    }

    @Transactional
    public SeguimientoRespuestaResponseDTO actualizar(Long id, SeguimientoRespuestaRequestDTO dto) {
        seguimientoRespuestaAccessService.validarPuedeEditarRespuesta(id);
        seguimientoRespuestaValidator.validarActualizacion(id, dto);

        SeguimientoRespuesta respuesta = obtenerRespuestaActiva(id);
        String contenido = seguimientoRespuestaValidator.normalizarContenido(dto.getContenido());

        if (equalsIgnoreCase(respuesta.getContenido(), contenido)) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        respuesta.setContenido(contenido);

        return seguimientoRespuestaMapper.convertirAResponseDTO(
                seguimientoRespuestaRepository.save(respuesta)
        );
    }

    @Transactional
    public SeguimientoRespuestaResponseDTO decidir(Long id, SeguimientoRespuestaDecisionDTO dto) {
        seguimientoRespuestaAccessService.validarPuedeRevisarRespuesta(id);
        seguimientoRespuestaValidator.validarDecision(dto);

        SeguimientoRespuesta respuesta = obtenerRespuestaActiva(id);
        UsuarioSistema revisor = obtenerUsuarioActual();

        respuesta.setEstado(dto.getEstado());
        respuesta.setObservacionRevision(
                seguimientoRespuestaValidator.normalizarObservacionRevision(dto.getObservacionRevision())
        );
        respuesta.setRevisadoPor(revisor);
        respuesta.setFechaDecision(LocalDateTime.now());

        return seguimientoRespuestaMapper.convertirAResponseDTO(
                seguimientoRespuestaRepository.save(respuesta)
        );
    }

    private Seguimiento obtenerSeguimientoActivo(Long seguimientoId) {
        if (seguimientoId == null) {
            throw new BusinessException("El id del seguimiento es obligatorio");
        }

        return seguimientoRepository.findByIdAndActivoTrue(seguimientoId)
                .orElseThrow(() -> new BusinessException("Seguimiento no encontrado con id: " + seguimientoId));
    }

    private SeguimientoRespuesta obtenerRespuestaActiva(Long respuestaId) {
        if (respuestaId == null) {
            throw new BusinessException("El id de la respuesta es obligatorio");
        }

        return seguimientoRespuestaRepository.findByIdAndActivoTrue(respuestaId)
                .orElseThrow(() -> new BusinessException("Respuesta de seguimiento no encontrada con id: " + respuestaId));
    }

    private Estudiante obtenerEstudianteActivo(Long estudianteId) {
        if (estudianteId == null) {
            throw new BusinessException("El id del estudiante es obligatorio");
        }

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado con id: " + estudianteId));

        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            throw new BusinessException("El estudiante se encuentra inactivo");
        }

        return estudiante;
    }

    private UsuarioSistema obtenerUsuarioActual() {
        Long usuarioActualId = seguimientoRespuestaAccessService.obtenerUsuarioActualId();

        return usuarioSistemaRepository.findById(usuarioActualId)
                .orElseThrow(() -> new BusinessException("Usuario revisor no encontrado con id: " + usuarioActualId));
    }
}