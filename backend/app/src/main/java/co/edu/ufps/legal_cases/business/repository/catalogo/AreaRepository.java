package co.edu.ufps.legal_cases.business.repository.catalogo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.catalogo.Area;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findByNombre(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}