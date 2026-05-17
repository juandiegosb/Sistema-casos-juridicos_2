package co.edu.ufps.legal_cases.business.repository.consulta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoDestinatarioDTO;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

        // Hibernate no permite hacer JOIN FETCH de dos colecciones al mismo tiempo.
        // Por eso partes y contrapartes se cargan en consultas separadas.
        @Query("""
                        SELECT DISTINCT c
                        FROM Consulta c
                        LEFT JOIN FETCH c.partes
                        WHERE c.id = :id
                        """)
        Optional<Consulta> findByIdConPartes(@Param("id") Long id);

        @Query("""
                        SELECT DISTINCT c
                        FROM Consulta c
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

        // Destinatario principal de la consulta.
        @Query("""
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoDestinatarioDTO(
                            p.correo,
                            TRIM(CONCAT(CONCAT(COALESCE(p.nombres, ''), ' '), COALESCE(p.apellidos, '')))
                        )
                        FROM Consulta c
                        JOIN c.persona p
                        WHERE c.id = :consultaId
                        AND p.correo IS NOT NULL
                        AND TRIM(p.correo) <> ''
                        """)
        List<SeguimientoDestinatarioDTO> findDestinatarioPersonaPrincipalByConsultaId(
                        @Param("consultaId") Long consultaId);

        // Partes adicionales de la consulta.
        @Query("""
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoDestinatarioDTO(
                            p.correo,
                            TRIM(CONCAT(CONCAT(COALESCE(p.nombres, ''), ' '), COALESCE(p.apellidos, '')))
                        )
                        FROM Consulta c
                        JOIN c.partes p
                        WHERE c.id = :consultaId
                        AND p.correo IS NOT NULL
                        AND TRIM(p.correo) <> ''
                        """)
        List<SeguimientoDestinatarioDTO> findDestinatariosPartesByConsultaId(
                        @Param("consultaId") Long consultaId);

        // Contrapartes de la consulta.
        @Query("""
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoDestinatarioDTO(
                            p.correo,
                            TRIM(CONCAT(CONCAT(COALESCE(p.nombres, ''), ' '), COALESCE(p.apellidos, '')))
                        )
                        FROM Consulta c
                        JOIN c.contrapartes p
                        WHERE c.id = :consultaId
                        AND p.correo IS NOT NULL
                        AND TRIM(p.correo) <> ''
                        """)
        List<SeguimientoDestinatarioDTO> findDestinatariosContrapartesByConsultaId(
                        @Param("consultaId") Long consultaId);

        // Estudiante asignado a la consulta.
        @Query("""
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoDestinatarioDTO(
                            e.email,
                            e.nombre
                        )
                        FROM Consulta c
                        JOIN c.estudiante e
                        WHERE c.id = :consultaId
                        AND e.activo = true
                        """)
        Optional<SeguimientoDestinatarioDTO> findDestinatarioEstudianteByConsultaId(
                        @Param("consultaId") Long consultaId);
}