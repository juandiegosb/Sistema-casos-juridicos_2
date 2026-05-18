package co.edu.ufps.legal_cases.business.repository.proceso;

import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganoControlRepository extends JpaRepository<OrganoControl, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}