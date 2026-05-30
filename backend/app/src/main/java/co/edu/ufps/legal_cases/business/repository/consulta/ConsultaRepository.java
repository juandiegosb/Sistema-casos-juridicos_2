package co.edu.ufps.legal_cases.business.repository.consulta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.SeguimientoDestinatarioDTO;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta;

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
                        WHERE c.estado <> co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta.ARCHIVADO
                          AND (:search IS NULL OR :search = ''
                           OR LOWER(c.descripcion)       LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.nombres)           LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.apellidos)         LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.numeroDocumento)   LIKE LOWER(CONCAT('%', :search, '%')))
                        ORDER BY c.fecha DESC
                        """)
    List<Consulta> buscar(@Param("search") String search);

    // Búsqueda para administrador.
    // El administrador puede ver todas las consultas activas.
    @Query("""
                        SELECT c FROM Consulta c
                        JOIN c.persona p
                        WHERE c.estado <> co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta.ARCHIVADO
                          AND (:search IS NULL OR :search = ''
                           OR LOWER(c.descripcion)       LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.nombres)           LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.apellidos)         LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.numeroDocumento)   LIKE LOWER(CONCAT('%', :search, '%')))
                        ORDER BY c.fecha DESC
                        """)
    List<Consulta> buscarParaAdministrador(@Param("search") String search);

    // Búsqueda para estudiante.
    // El estudiante solo ve las consultas asociadas a su perfil.
    @Query("""
                        SELECT c FROM Consulta c
                        JOIN c.persona p
                        WHERE c.estado <> co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta.ARCHIVADO
                          AND (:search IS NULL OR :search = ''
                           OR LOWER(c.descripcion)       LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.nombres)           LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.apellidos)         LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.numeroDocumento)   LIKE LOWER(CONCAT('%', :search, '%')))
                          AND c.estudiante.id = :estudianteId
                        ORDER BY c.fecha DESC
                        """)
    List<Consulta> buscarParaEstudiante(
            @Param("search") String search,
            @Param("estudianteId") Long estudianteId);

    // Búsqueda para asesor.
    // El asesor ve consultas asignadas directamente a él
    // y consultas de estudiantes que pertenecen a su asesoría.
    @Query("""
                        SELECT c FROM Consulta c
                        JOIN c.persona p
                        WHERE c.estado <> co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta.ARCHIVADO
                          AND (:search IS NULL OR :search = ''
                           OR LOWER(c.descripcion)       LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.nombres)           LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.apellidos)         LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.numeroDocumento)   LIKE LOWER(CONCAT('%', :search, '%')))
                          AND (
                                c.asesor.id = :asesorId
                                OR c.estudiante.asesor.id = :asesorId
                          )
                        ORDER BY c.fecha DESC
                        """)
    List<Consulta> buscarParaAsesor(
            @Param("search") String search,
            @Param("asesorId") Long asesorId);

    // Búsqueda para monitor.
    // El monitor solo ve consultas donde está asignado.
    @Query("""
                        SELECT c FROM Consulta c
                        JOIN c.persona p
                        WHERE c.estado <> co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta.ARCHIVADO
                          AND (:search IS NULL OR :search = ''
                           OR LOWER(c.descripcion)       LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.nombres)           LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.apellidos)         LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(p.numeroDocumento)   LIKE LOWER(CONCAT('%', :search, '%')))
                          AND c.monitor.id = :monitorId
                        ORDER BY c.fecha DESC
                        """)
    List<Consulta> buscarParaMonitor(
            @Param("search") String search,
            @Param("monitorId") Long monitorId);

    // Búsqueda filtrada anterior.
    // Se conserva temporalmente para no romper llamadas existentes mientras se migra el service.
    @Query("""
                        SELECT c FROM Consulta c
                        JOIN c.persona p
                        WHERE c.estado <> co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta.ARCHIVADO
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

    // Destinatario principal de la consulta.
    @Query("""
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.SeguimientoDestinatarioDTO(
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
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.SeguimientoDestinatarioDTO(
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
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.SeguimientoDestinatarioDTO(
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
                        SELECT new co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.SeguimientoDestinatarioDTO(
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

    List<Consulta> findByEstado(EstadoConsulta estado);

    // Cuenta consultas finalizadas (con resultado) y pendientes (sin resultado)
    // para un semestre dado, usando last_updated_at como fecha de referencia.
    // Semestre 1: 1 enero - 30 junio. Semestre 2: 1 julio - 31 diciembre.
    @Query(value = """
                SELECT
                    COUNT(*) FILTER (WHERE c.resultado IS NOT NULL) AS finished_consultas,
                    COUNT(*) FILTER (WHERE c.resultado IS NULL) AS unfinished_consultas
                FROM "DB_consultorioJuridico".consulta c
                WHERE c.last_updated_at >=
                    CASE
                        WHEN :semester = 1 THEN make_date(:year, 1, 1)
                        ELSE make_date(:year, 7, 1)
                    END
                AND c.last_updated_at <
                    CASE
                        WHEN :semester = 1 THEN make_date(:year, 7, 1)
                        ELSE make_date(:year + 1, 1, 1)
                    END
                """, nativeQuery = true)
    List<Object[]> contarFinalizadasYPendientesPorSemestreRaw(
            @Param("year") int year,
            @Param("semester") int semester);


    // Consultas agrupadas por área jurídica — todos los tiempos.
    @Query(value = """
                SELECT a.id AS area_id, a.nombre AS area_nombre, COUNT(c.id) AS total_consultas
                FROM "DB_consultorioJuridico".consulta c
                JOIN "DB_consultorioJuridico".area a ON a.id = c.area_id
                GROUP BY a.id, a.nombre
                ORDER BY total_consultas DESC
                """, nativeQuery = true)
    List<Object[]> contarConsultasPorAreaTodos();

    // Consultas agrupadas por área jurídica — filtradas por semestre.
    @Query(value = """
                SELECT a.id AS area_id, a.nombre AS area_nombre, COUNT(c.id) AS total_consultas
                FROM "DB_consultorioJuridico".consulta c
                JOIN "DB_consultorioJuridico".area a ON a.id = c.area_id
                WHERE c.last_updated_at >=
                    CASE WHEN :semester = 1 THEN make_date(:year, 1, 1) ELSE make_date(:year, 7, 1) END
                AND c.last_updated_at <
                    CASE WHEN :semester = 1 THEN make_date(:year, 7, 1) ELSE make_date(:year + 1, 1, 1) END
                GROUP BY a.id, a.nombre
                ORDER BY total_consultas DESC
                """, nativeQuery = true)
    List<Object[]> contarConsultasPorAreaPorSemestre(
            @Param("year") int year,
            @Param("semester") int semester);


    // Total de personas atendidas en el semestre.
    // Cuenta personas principales de consultas más partes adicionales.
    @Query(value = """
                WITH consultas_semestre AS (
                    SELECT c.id
                    FROM "DB_consultorioJuridico".consulta c
                    WHERE c.last_updated_at >=
                        CASE WHEN :semester = 1 THEN make_date(:year, 1, 1) ELSE make_date(:year, 7, 1) END
                    AND c.last_updated_at <
                        CASE WHEN :semester = 1 THEN make_date(:year, 7, 1) ELSE make_date(:year + 1, 1, 1) END
                )
                SELECT
                    (SELECT COUNT(*) FROM consultas_semestre)
                    +
                    (SELECT COUNT(*) FROM "DB_consultorioJuridico".consulta_parte cp
                     WHERE cp.consulta_id IN (SELECT id FROM consultas_semestre))
                AS total_personas_atendidas
                """, nativeQuery = true)
    List<Object[]> contarPersonasAtendidasPorSemestre(
            @Param("year") int year,
            @Param("semester") int semester);


    // Consultas finalizadas/pendientes por semestre filtradas por asesor.
    @Query(value = """
                SELECT COUNT(*) FILTER (WHERE c.resultado IS NOT NULL) AS finished_consultas,
                       COUNT(*) FILTER (WHERE c.resultado IS NULL) AS unfinished_consultas
                FROM "DB_consultorioJuridico".consulta c
                WHERE c.last_updated_at >=
                    CASE WHEN :semester = 1 THEN make_date(:year, 1, 1) ELSE make_date(:year, 7, 1) END
                AND c.last_updated_at <
                    CASE WHEN :semester = 1 THEN make_date(:year, 7, 1) ELSE make_date(:year + 1, 1, 1) END
                AND c.asesor_id = :asesorId
                """, nativeQuery = true)
    List<Object[]> contarFinalizadasYPendientesPorSemestreYAsesor(
            @Param("year") int year,
            @Param("semester") int semester,
            @Param("asesorId") Long asesorId);

    // Consultas finalizadas/pendientes por semestre filtradas por estudiante.
    @Query(value = """
                SELECT COUNT(*) FILTER (WHERE c.resultado IS NOT NULL) AS finished_consultas,
                       COUNT(*) FILTER (WHERE c.resultado IS NULL) AS unfinished_consultas
                FROM "DB_consultorioJuridico".consulta c
                WHERE c.last_updated_at >=
                    CASE WHEN :semester = 1 THEN make_date(:year, 1, 1) ELSE make_date(:year, 7, 1) END
                AND c.last_updated_at <
                    CASE WHEN :semester = 1 THEN make_date(:year, 7, 1) ELSE make_date(:year + 1, 1, 1) END
                AND c.estudiante_id = :estudianteId
                """, nativeQuery = true)
    List<Object[]> contarFinalizadasYPendientesPorSemestreYEstudiante(
            @Param("year") int year,
            @Param("semester") int semester,
            @Param("estudianteId") Long estudianteId);

    // Consultas finalizadas/pendientes por semestre filtradas por monitor.
    @Query(value = """
                SELECT COUNT(*) FILTER (WHERE c.resultado IS NOT NULL) AS finished_consultas,
                       COUNT(*) FILTER (WHERE c.resultado IS NULL) AS unfinished_consultas
                FROM "DB_consultorioJuridico".consulta c
                WHERE c.last_updated_at >=
                    CASE WHEN :semester = 1 THEN make_date(:year, 1, 1) ELSE make_date(:year, 7, 1) END
                AND c.last_updated_at <
                    CASE WHEN :semester = 1 THEN make_date(:year, 7, 1) ELSE make_date(:year + 1, 1, 1) END
                AND c.monitor_id = :monitorId
                """, nativeQuery = true)
    List<Object[]> contarFinalizadasYPendientesPorSemestreYMonitor(
            @Param("year") int year,
            @Param("semester") int semester,
            @Param("monitorId") Long monitorId);

    // Personas atendidas por semestre filtradas por asesor.
    @Query(value = """
                WITH consultas_semestre AS (
                    SELECT c.id FROM "DB_consultorioJuridico".consulta c
                    WHERE c.last_updated_at >=
                        CASE WHEN :semester = 1 THEN make_date(:year, 1, 1) ELSE make_date(:year, 7, 1) END
                    AND c.last_updated_at <
                        CASE WHEN :semester = 1 THEN make_date(:year, 7, 1) ELSE make_date(:year + 1, 1, 1) END
                    AND c.asesor_id = :asesorId
                )
                SELECT (SELECT COUNT(*) FROM consultas_semestre)
                    + (SELECT COUNT(*) FROM "DB_consultorioJuridico".consulta_parte cp
                       WHERE cp.consulta_id IN (SELECT id FROM consultas_semestre))
                AS total_personas_atendidas
                """, nativeQuery = true)
    List<Object[]> contarPersonasAtendidasPorSemestreYAsesor(
            @Param("year") int year,
            @Param("semester") int semester,
            @Param("asesorId") Long asesorId);

    // Personas atendidas por semestre filtradas por estudiante.
    @Query(value = """
                WITH consultas_semestre AS (
                    SELECT c.id FROM "DB_consultorioJuridico".consulta c
                    WHERE c.estudiante_id = :estudianteId
                    AND c.last_updated_at >=
                        CASE WHEN :semester = 1 THEN make_date(:year, 1, 1) ELSE make_date(:year, 7, 1) END
                    AND c.last_updated_at <
                        CASE WHEN :semester = 1 THEN make_date(:year, 7, 1) ELSE make_date(:year + 1, 1, 1) END
                )
                SELECT (SELECT COUNT(*) FROM consultas_semestre)
                    + (SELECT COUNT(*) FROM "DB_consultorioJuridico".consulta_parte cp
                       WHERE cp.consulta_id IN (SELECT id FROM consultas_semestre))
                AS total_personas_atendidas
                """, nativeQuery = true)
    List<Object[]> contarPersonasAtendidasPorSemestreYEstudiante(
            @Param("year") int year,
            @Param("semester") int semester,
            @Param("estudianteId") Long estudianteId);

    // Personas atendidas por semestre filtradas por monitor.
    @Query(value = """
                WITH consultas_semestre AS (
                    SELECT c.id FROM "DB_consultorioJuridico".consulta c
                    WHERE c.last_updated_at >=
                        CASE WHEN :semester = 1 THEN make_date(:year, 1, 1) ELSE make_date(:year, 7, 1) END
                    AND c.last_updated_at <
                        CASE WHEN :semester = 1 THEN make_date(:year, 7, 1) ELSE make_date(:year + 1, 1, 1) END
                    AND c.monitor_id = :monitorId
                )
                SELECT (SELECT COUNT(*) FROM consultas_semestre)
                    + (SELECT COUNT(*) FROM "DB_consultorioJuridico".consulta_parte cp
                       WHERE cp.consulta_id IN (SELECT id FROM consultas_semestre))
                AS total_personas_atendidas
                """, nativeQuery = true)
    List<Object[]> contarPersonasAtendidasPorSemestreYMonitor(
            @Param("year") int year,
            @Param("semester") int semester,
            @Param("monitorId") Long monitorId);


}