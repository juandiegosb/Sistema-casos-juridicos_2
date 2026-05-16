package co.edu.ufps.legal_cases.business.repository.consulta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.model.consulta.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Hibernate no permite hacer JOIN FETCH de dos colecciones (bags) al mismo tiempo.
    // Se usan dos queries separadas: una carga partes, la otra contrapartes.
    // El service las llama en secuencia dentro de una misma transacción.

    @Query("""
            SELECT DISTINCT c FROM Consulta c
            LEFT JOIN FETCH c.partes
            WHERE c.id = :id
            """)
    Optional<Consulta> findByIdConPartes(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT c FROM Consulta c
            LEFT JOIN FETCH c.contrapartes
            WHERE c.id = :id
            """)
    Optional<Consulta> findByIdConContrapartes(@Param("id") Long id);

    @Query("""
            SELECT c FROM Consulta c
            JOIN c.persona p
            WHERE :search IS NULL OR :search = ''
               OR LOWER(c.descripcion)       LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.nombres)           LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.apellidos)         LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.numeroDocumento)   LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY c.fecha DESC
            """)
    List<Consulta> buscar(@Param("search") String search);

    // Búsqueda filtrada por rol del usuario autenticado.
    // Los parámetros de filtro son opcionales: si vienen null, esa condición se ignora.
    // ADMINISTRATIVO y CONCILIADOR pasan todos null → ven todas las consultas.
    @Query("""
            SELECT c FROM Consulta c
            JOIN c.persona p
            WHERE (:search IS NULL OR :search = ''
               OR LOWER(c.descripcion)       LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.nombres)           LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.apellidos)         LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.numeroDocumento)   LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:estudianteId IS NULL OR c.estudiante.id = :estudianteId)
              AND (:asesorId     IS NULL OR c.asesor.id     = :asesorId)
              AND (:monitorId    IS NULL OR c.monitor.id    = :monitorId)
            ORDER BY c.fecha DESC
            """)
    List<Consulta> buscarFiltrado(
            @Param("search") String search,
            @Param("estudianteId") Long estudianteId,
            @Param("asesorId") Long asesorId,
            @Param("monitorId") Long monitorId);
}