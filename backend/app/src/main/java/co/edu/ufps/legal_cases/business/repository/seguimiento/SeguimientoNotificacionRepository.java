package co.edu.ufps.legal_cases.business.repository.seguimiento;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.seguimiento.SeguimientoNotificacion;
import co.edu.ufps.legal_cases.business.model.seguimiento.TipoNotificacionSeguimiento;

@Repository
public interface SeguimientoNotificacionRepository extends JpaRepository<SeguimientoNotificacion, Long> {

    Optional<SeguimientoNotificacion> findBySeguimiento_IdAndTipoNotificacion(
            Long seguimientoId,
            TipoNotificacionSeguimiento tipoNotificacion
    );

    List<SeguimientoNotificacion> findBySeguimiento_Id(Long seguimientoId);

    List<SeguimientoNotificacion> findByFechaProgramadaLessThanEqualAndEnviadaFalse(LocalDate fecha);

    void deleteBySeguimiento_Id(Long seguimientoId);

    void deleteBySeguimiento_IdAndTipoNotificacionNotIn(
            Long seguimientoId,
            Collection<TipoNotificacionSeguimiento> tiposNotificacion
    );
}