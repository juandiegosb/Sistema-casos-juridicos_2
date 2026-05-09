package co.edu.ufps.legal_cases.business.repository.perfil;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.perfil.Estudiante;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByTelefono(String telefono);

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByTelefonoAndIdNot(String telefono, Long id);

    boolean existsByUsuarioIgnoreCaseAndIdNot(String usuario, Long id);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    List<Estudiante> findByActivoTrue();

    List<Estudiante> findByConciliacionTrue();

    List<Estudiante> findByAsesorId(Long asesorId);

    Optional<Estudiante> findByUsuarioSistema_IdAndActivoTrue(Long usuarioSistemaId);
}