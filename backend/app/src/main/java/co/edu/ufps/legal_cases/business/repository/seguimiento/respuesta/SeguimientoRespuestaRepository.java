package co.edu.ufps.legal_cases.business.repository.seguimiento.respuesta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.seguimiento.respuesta.EstadoRespuestaSeguimiento;
import co.edu.ufps.legal_cases.business.model.seguimiento.respuesta.SeguimientoRespuesta;

@Repository
public interface SeguimientoRespuestaRepository extends JpaRepository<SeguimientoRespuesta, Long> {

    Optional<SeguimientoRespuesta> findByIdAndActivoTrue(Long id);

    Optional<SeguimientoRespuesta> findBySeguimiento_IdAndEstudiante_IdAndActivoTrue(
            Long seguimientoId,
            Long estudianteId);

    // Trae el último intento activo del estudiante para ese seguimiento.
    // Se usa para decidir si puede crear una nueva respuesta.
    Optional<SeguimientoRespuesta> findFirstBySeguimiento_IdAndEstudiante_IdAndActivoTrueOrderByFechaCreacionDescIdDesc(
            Long seguimientoId,
            Long estudianteId);

    List<SeguimientoRespuesta> findBySeguimiento_IdAndActivoTrueOrderByFechaCreacionDesc(Long seguimientoId);

    List<SeguimientoRespuesta> findByEstadoAndActivoTrueOrderByFechaCreacionDesc(
            EstadoRespuestaSeguimiento estado);

    boolean existsBySeguimiento_IdAndActivoTrueAndEstado(
        Long seguimientoId,
        EstadoRespuestaSeguimiento estado);
}