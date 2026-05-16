package co.edu.ufps.legal_cases.business.repository.seguimiento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.dto.seguimiento.DatosNotificacionSeguimientoDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

        List<Seguimiento> findByConsulta_IdOrderByFechaCreacionDesc(Long consultaId);

        List<Seguimiento> findByConsulta_IdAndNotificarEstudianteTrueOrderByFechaCreacionDesc(Long consultaId);

        List<Seguimiento> findByAutor_IdOrderByFechaCreacionDesc(Long autorId);

        boolean existsByCategoriaSeguimiento_Id(Long categoriaSeguimientoId);

        boolean existsByConsulta_Id(Long consultaId);

        // Para alertas disciplinarias.
        List<Seguimiento> findByAlertaDisciplinariaTrueOrderByFechaCreacionDesc();

        // Para consultar seguimientos por fecha de entrega.
        List<Seguimiento> findByFechaEntregaOrderByFechaCreacionDesc(LocalDate fechaEntrega);

        @Query("""
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.DatosNotificacionSeguimientoDTO(
                            s.id,
                            s.consulta.id,
                            s.autor.id,
                            s.autor.username
                        )
                        FROM Seguimiento s
                        WHERE s.id = :seguimientoId
                        """)
        Optional<DatosNotificacionSeguimientoDTO> findDatosNotificacionById(
                        @Param("seguimientoId") Long seguimientoId);
}