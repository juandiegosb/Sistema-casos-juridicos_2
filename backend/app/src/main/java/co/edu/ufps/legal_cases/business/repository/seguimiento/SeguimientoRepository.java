package co.edu.ufps.legal_cases.business.repository.seguimiento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    // Lista todos los seguimientos de una consulta.
    // Lo usarían asesor, monitor o administrativos según permisos.
    List<Seguimiento> findByConsulta_IdOrderByFechaCreacionDesc(Long consultaId);

    // Lista únicamente los seguimientos visibles para el estudiante.
    // En tu regla actual, notificarEstudiante = true significa:
    // se notifica al estudiante y también se le puede mostrar.
    List<Seguimiento> findByConsulta_IdAndNotificarEstudianteTrueOrderByFechaCreacionDesc(Long consultaId);

    // Lista seguimientos creados por un usuario del sistema.
    List<Seguimiento> findByAutor_IdOrderByFechaCreacionDesc(Long autorId);

    // Se usa para evitar eliminar categorías que ya tienen seguimientos asociados.
    boolean existsByCategoriaSeguimiento_Id(Long categoriaSeguimientoId);

    // Sirve para validar o consultar si una consulta tiene seguimientos.
    boolean existsByConsulta_Id(Long consultaId);

    // Lista seguimientos marcados como alerta disciplinaria.
    List<Seguimiento> findByAlertaDisciplinariaTrueOrderByFechaCreacionDesc();

    // Lista seguimientos por fecha de entrega.
    List<Seguimiento> findByFechaEntregaOrderByFechaCreacionDesc(LocalDate fechaEntrega);
}