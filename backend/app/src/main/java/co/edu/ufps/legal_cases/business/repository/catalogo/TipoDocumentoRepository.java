package co.edu.ufps.legal_cases.business.repository.catalogo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    boolean existsByDisplayNameIgnoreCase(String displayName);

    boolean existsByDisplayNameIgnoreCaseAndIdNot(String displayName, Long id);

    List<TipoDocumento> findByActivoTrue();
}