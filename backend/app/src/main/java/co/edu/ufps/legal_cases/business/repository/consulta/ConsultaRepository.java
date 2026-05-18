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
                        WHERE LOWER(c.estado) <> 'archivado'
                          AND (:search IS NULL OR :search = ''
                           OR LOWER(c.descripcion)       LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.nombres)           LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.apellidos)         LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.numeroDocumento)   LIKE LOWER(CONCAT('%', :search, '%')))
                        ORDER BY c.fecha DESC
                        """)
    List<Consulta> buscar(@Param("search") String search);

    @Query("""
                        SELECT c FROM Consulta c
                        JOIN c.persona p
                        WHERE LOWER(c.estado) <> 'archivado'
                          AND (:search IS NULL OR :search = ''
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

    List<Consulta> findByEstadoIgnoreCase(String estado);

}