package co.edu.ufps.legal_cases.business.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.catalogo.Tema;

import java.util.List;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {

    boolean existsByNombreIgnoreCaseAndAreaId(String nombre, Long areaId);

    List<Tema> findByAreaId(Long areaId);
}
