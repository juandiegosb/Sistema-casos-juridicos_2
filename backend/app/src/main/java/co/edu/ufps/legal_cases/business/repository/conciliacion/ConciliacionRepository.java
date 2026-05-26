package co.edu.ufps.legal_cases.business.repository.conciliacion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.conciliacion.Conciliacion;
import co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta;

@Repository
public interface ConciliacionRepository extends JpaRepository<Conciliacion, Long> {

    Optional<Conciliacion> findByIdAndActivoTrue(Long id);

    Optional<Conciliacion> findByIdAndActivoTrueAndConsulta_EstadoNot(
            Long id,
            EstadoConsulta estado);

    List<Conciliacion> findByActivoTrueOrderByIdDesc();

    List<Conciliacion> findByActivoTrueAndConsulta_EstadoNotOrderByIdDesc(
            EstadoConsulta estado);

    List<Conciliacion> findByConsulta_IdAndActivoTrueOrderByIdDesc(Long consultaId);

    List<Conciliacion> findByConsulta_IdAndActivoTrueAndConsulta_EstadoNotOrderByIdDesc(
            Long consultaId,
            EstadoConsulta estado);

    boolean existsByConsulta_IdAndActivoTrueAndEstado_CodigoIn(
            Long consultaId,
            Collection<String> codigosEstado);

    long countByEstudiante_IdAndActivoTrueAndEstado_CodigoIn(
            Long estudianteId,
            Collection<String> codigosEstado);

    long countByConciliador_IdAndActivoTrueAndEstado_CodigoIn(
            Long conciliadorId,
            Collection<String> codigosEstado);
}