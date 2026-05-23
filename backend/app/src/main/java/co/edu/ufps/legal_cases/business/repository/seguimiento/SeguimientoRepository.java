package co.edu.ufps.legal_cases.business.repository.seguimiento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.DatosCorreoSeguimientoDTO;
import co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.DatosNotificacionSeguimientoDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.EstadoSeguimiento;
import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    Optional<Seguimiento> findByIdAndActivoTrue(Long id);

    // Lista todos los seguimientos activos de una consulta.
    // Lo usarían asesor, monitor o administrativos según permisos.
    List<Seguimiento> findByConsulta_IdAndActivoTrueOrderByFechaCreacionDesc(Long consultaId);

    // Lista únicamente los seguimientos activos visibles para el estudiante.
    // En tu regla actual, notificarEstudiante = true significa:
    // se notifica al estudiante y también se le puede mostrar.
    List<Seguimiento> findByConsulta_IdAndNotificarEstudianteTrueAndActivoTrueOrderByFechaCreacionDesc(Long consultaId);

    // Lista seguimientos activos creados por un usuario del sistema.
    List<Seguimiento> findByAutor_IdAndActivoTrueOrderByFechaCreacionDesc(Long autorId);

    // Se usa para evitar eliminar categorías que ya tienen seguimientos asociados.
    boolean existsByCategoriaSeguimiento_Id(Long categoriaSeguimientoId);

    // Sirve para validar o consultar si una consulta tiene seguimientos.
    boolean existsByConsulta_Id(Long consultaId);

    // Lista seguimientos activos marcados como alerta disciplinaria.
    List<Seguimiento> findByAlertaDisciplinariaTrueAndActivoTrueOrderByFechaCreacionDesc();

    // Lista seguimientos activos por fecha de entrega.
    List<Seguimiento> findByFechaEntregaAndActivoTrueOrderByFechaCreacionDesc(LocalDate fechaEntrega);

    @Query("""
            SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.DatosNotificacionSeguimientoDTO(
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

    // Para enviar datos al servicio de correo sin exponer toda la entidad de
    // seguimiento.
    @Query("""
            SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.DatosCorreoSeguimientoDTO(
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

    boolean existsByConsulta_IdAndActivoTrueAndEstado(Long consultaId, EstadoSeguimiento estado);
}