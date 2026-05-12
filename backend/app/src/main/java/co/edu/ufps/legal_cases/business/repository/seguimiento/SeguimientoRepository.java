package co.edu.ufps.legal_cases.business.repository.seguimiento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    List<Seguimiento> findByConsulta_IdOrderByFechaCreacionDesc(Long consultaId);

    List<Seguimiento> findByAutor_IdOrderByFechaCreacionDesc(Long autorId);

    boolean existsByCategoriaSeguimiento_Id(Long categoriaSeguimientoId);

    boolean existsByConsulta_Id(Long consultaId);

    // Para notificaciones
    // Permite buscar seguimientos con fecha de entrega, días de notificación y marcados para notificar a partes.
    List<Seguimiento> findByFechaEntregaIsNotNullAndDiasNotificacionIsNotNullAndNotificarPartesTrue();

    // Para alertas disciplinarias.
    List<Seguimiento> findByAlertaDisciplinariaTrueOrderByFechaCreacionDesc();

    // Para consultar seguimientos por fecha de entrega.
    List<Seguimiento> findByFechaEntregaOrderByFechaCreacionDesc(LocalDate fechaEntrega);
}