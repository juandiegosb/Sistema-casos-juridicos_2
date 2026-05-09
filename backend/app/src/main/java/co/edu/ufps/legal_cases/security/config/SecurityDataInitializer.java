package co.edu.ufps.legal_cases.security.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import co.edu.ufps.legal_cases.security.model.access.Permiso;
import co.edu.ufps.legal_cases.security.model.access.Rol;
import co.edu.ufps.legal_cases.security.repository.access.PermisoRepository;
import co.edu.ufps.legal_cases.security.repository.access.RolRepository;

import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;

@Configuration
public class SecurityDataInitializer {

    @Bean
    //Tiene que inyectarse primero para que luego se puedan asignar roles a la inyeccion de usuarios
    @Order(1)
    CommandLineRunner initSecurityData(
            PermisoRepository permisoRepository,
            RolRepository rolRepository) {
        return args -> {
            inicializarPermisosYRoles(permisoRepository, rolRepository);
        };
    }

    private void inicializarPermisosYRoles(
            PermisoRepository permisoRepository,
            RolRepository rolRepository) {

        // Permisos base del sistema.
        Permiso gestionarUsuarios = crearPermisoSiNoExiste(
                permisoRepository,
                "Gestionar usuarios",
                "Permite administrar usuarios del sistema");

        Permiso gestionarRoles = crearPermisoSiNoExiste(
                permisoRepository,
                "Gestionar roles",
                "Permite administrar roles del sistema");

        Permiso gestionarPermisos = crearPermisoSiNoExiste(
                permisoRepository,
                "Gestionar permisos",
                "Permite administrar permisos del sistema");

        Permiso gestionarCatalogos = crearPermisoSiNoExiste(
                permisoRepository,
                "Gestionar catálogos",
                "Permite administrar catálogos del sistema");

        Permiso gestionarPersonas = crearPermisoSiNoExiste(
                permisoRepository,
                "Gestionar personas",
                "Permite administrar personas registradas");

        Permiso gestionarConsultas = crearPermisoSiNoExiste(
                permisoRepository,
                "Gestionar consultas",
                "Permite administrar consultas jurídicas");

        Permiso gestionarConciliaciones = crearPermisoSiNoExiste(
                permisoRepository,
                "Gestionar conciliaciones",
                "Permite administrar conciliaciones");

        Permiso verReportes = crearPermisoSiNoExiste(
                permisoRepository,
                "Ver reportes",
                "Permite consultar reportes del sistema");

        // Roles base del sistema.
        crearRolSiNoExiste(
                rolRepository,
                "Administrador",
                "Rol encargado de la administración general del sistema",
                Set.of(
                        gestionarUsuarios,
                        gestionarRoles,
                        gestionarPermisos,
                        gestionarCatalogos,
                        gestionarPersonas,
                        gestionarConsultas,
                        gestionarConciliaciones,
                        verReportes));

        crearRolSiNoExiste(
                rolRepository,
                "Asesor",
                "Rol base para usuarios asesores",
                Set.of(
                        gestionarPersonas,
                        gestionarConsultas));

        crearRolSiNoExiste(
                rolRepository,
                "Estudiante",
                "Rol base para usuarios estudiantes",
                Set.of(
                        gestionarPersonas,
                        gestionarConsultas));

        crearRolSiNoExiste(
                rolRepository,
                "Monitor",
                "Rol base para usuarios monitores",
                Set.of(
                        gestionarConsultas,
                        verReportes));

        crearRolSiNoExiste(
                rolRepository,
                "Conciliador",
                "Rol base para usuarios conciliadores",
                Set.of(
                        gestionarPersonas,
                        gestionarConciliaciones));
    }

    private Permiso crearPermisoSiNoExiste(
            PermisoRepository permisoRepository,
            String nombre,
            String descripcion) {

        String nombreNormalizado = normalizarTexto(nombre);
        String descripcionNormalizada = normalizarTexto(descripcion);

        return permisoRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseGet(() -> {
                    Permiso permiso = new Permiso();
                    permiso.setNombre(nombreNormalizado);
                    permiso.setDescripcion(descripcionNormalizada);
                    permiso.setActivo(true);
                    return permisoRepository.save(permiso);
                });
    }

    private Rol crearRolSiNoExiste(
            RolRepository rolRepository,
            String nombre,
            String descripcion,
            Set<Permiso> permisos) {

        String nombreNormalizado = normalizarTexto(nombre);
        String descripcionNormalizada = normalizarTexto(descripcion);

        return rolRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(nombreNormalizado);
                    rol.setDescripcion(descripcionNormalizada);
                    rol.setActivo(true);
                    rol.setPermisos(permisos);
                    return rolRepository.save(rol);
                });
    }
}