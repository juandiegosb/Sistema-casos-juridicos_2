package co.edu.ufps.legal_cases.business.repository.proceso;

import co.edu.ufps.legal_cases.business.model.proceso.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    boolean existsByNumeroRadicado(String numeroRadicado);
}