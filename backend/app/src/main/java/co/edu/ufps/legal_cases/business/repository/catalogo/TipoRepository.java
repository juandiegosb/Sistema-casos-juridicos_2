package co.edu.ufps.legal_cases.business.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.catalogo.Tipo;

import java.util.List;

@Repository
public interface TipoRepository extends JpaRepository<Tipo, Long> {

    boolean existsByNombreIgnoreCaseAndTemaId(String nombre, Long temaId);

    List<Tipo> findByTemaId(Long temaId);
}