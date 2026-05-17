package co.edu.ufps.legal_cases.business.repository.seguimiento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.dto.seguimiento.DatosCorreoSeguimientoDTO;
import co.edu.ufps.legal_cases.business.dto.seguimiento.DatosNotificacionSeguimientoDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    Optional<Seguimiento> findByIdAndActivoTrue(Long id);

    List<Seguimiento> findByConsulta_IdAndActivoTrueOrderByFechaCreacionDesc(Long consultaId);

    List<Seguimiento> findByConsulta_IdAndNotificarEstudianteTrueAndActivoTrueOrderByFechaCreacionDesc(Long consultaId);

    List<Seguimiento> findByAutor_IdAndActivoTrueOrderByFechaCreacionDesc(Long autorId);

    boolean existsByCategoriaSeguimiento_Id(Long categoriaSeguimientoId);

    boolean existsByConsulta_Id(Long consultaId);

    // Para alertas disciplinarias.
    List<Seguimiento> findByAlertaDisciplinariaTrueAndActivoTrueOrderByFechaCreacionDesc();

    // Para consultar seguimientos por fecha de entrega.
    List<Seguimiento> findByFechaEntregaAndActivoTrueOrderByFechaCreacionDesc(LocalDate fechaEntrega);

    @Query("""
            SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.DatosNotificacionSeguimientoDTO(
                s.id,
                s.consulta.id,
                s.autor.id,
                s.autor.username
            )
            FROM Seguimiento s
            WHERE s.id = :seguimientoId
            AND s.activo = true
            """)
    Optional<DatosNotificacionSeguimientoDTO> findDatosNotificacionById(
            @Param("seguimientoId") Long seguimientoId);

    // Para enviar datos al servicio de correo sin exponer toda la entidad de seguimiento.
    @Query("""
            SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.DatosCorreoSeguimientoDTO(
                s.id,
                s.descripcion,
                s.categoriaSeguimiento.nombre,
                s.consulta.id,
                s.fechaEntrega,
                s.diasNotificacion,
                s.notificarPartes,
                s.notificarEstudiante,
                s.alertaDisciplinaria
            )
            FROM Seguimiento s
            WHERE s.id = :seguimientoId
            AND s.activo = true
            """)
    Optional<DatosCorreoSeguimientoDTO> findDatosCorreoById(@Param("seguimientoId") Long seguimientoId);
}