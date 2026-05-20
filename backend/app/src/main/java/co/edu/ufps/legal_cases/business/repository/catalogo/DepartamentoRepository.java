package co.edu.ufps.legal_cases.business.repository.catalogo;

import co.edu.ufps.legal_cases.business.model.catalogo.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}