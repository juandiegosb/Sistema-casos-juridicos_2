package co.edu.ufps.legal_cases.business.repository.proceso;

import co.edu.ufps.legal_cases.business.model.proceso.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    boolean existsByNombreIgnoreCaseAndOrganoControlId(
            String nombre,
            Long organoControlId
    );

    List<Especialidad> findByOrganoControlId(Long organoControlId);
}