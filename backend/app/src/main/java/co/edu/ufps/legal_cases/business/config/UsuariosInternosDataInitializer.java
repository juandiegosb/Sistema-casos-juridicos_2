package co.edu.ufps.legal_cases.business.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import co.edu.ufps.legal_cases.business.dto.AdministrativoDTO;
import co.edu.ufps.legal_cases.business.dto.AsesorDTO;
import co.edu.ufps.legal_cases.business.dto.ConciliadorDTO;
import co.edu.ufps.legal_cases.business.dto.EstudianteDTO;
import co.edu.ufps.legal_cases.business.dto.MonitorDTO;
import co.edu.ufps.legal_cases.business.model.Area;
import co.edu.ufps.legal_cases.business.model.Asesor;
import co.edu.ufps.legal_cases.business.model.Sede;
import co.edu.ufps.legal_cases.business.model.TipoConciliador;
import co.edu.ufps.legal_cases.business.model.TipoDocumento;
import co.edu.ufps.legal_cases.business.repository.AdministrativoRepository;
import co.edu.ufps.legal_cases.business.repository.AreaRepository;
import co.edu.ufps.legal_cases.business.repository.AsesorRepository;
import co.edu.ufps.legal_cases.business.repository.ConciliadorRepository;
import co.edu.ufps.legal_cases.business.repository.EstudianteRepository;
import co.edu.ufps.legal_cases.business.repository.MonitorRepository;
import co.edu.ufps.legal_cases.business.repository.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.service.AdministrativoService;
import co.edu.ufps.legal_cases.business.service.AsesorService;
import co.edu.ufps.legal_cases.business.service.ConciliadorService;
import co.edu.ufps.legal_cases.business.service.EstudianteService;
import co.edu.ufps.legal_cases.business.service.MonitorService;

@Configuration
public class UsuariosInternosDataInitializer {

    @Bean
    @Order(4)
    CommandLineRunner initUsuariosInternos(
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            AreaRepository areaRepository,
            AsesorRepository asesorRepository,
            MonitorRepository monitorRepository,
            AdministrativoRepository administrativoRepository,
            ConciliadorRepository conciliadorRepository,
            EstudianteRepository estudianteRepository,
            AsesorService asesorService,
            MonitorService monitorService,
            AdministrativoService administrativoService,
            ConciliadorService conciliadorService,
            EstudianteService estudianteService) {
        return args -> inicializarUsuariosInternos(
                tipoDocumentoRepository,
                sedeRepository,
                areaRepository,
                asesorRepository,
                monitorRepository,
                administrativoRepository,
                conciliadorRepository,
                estudianteRepository,
                asesorService,
                monitorService,
                administrativoService,
                conciliadorService,
                estudianteService);
    }

    private void inicializarUsuariosInternos(
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            AreaRepository areaRepository,
            AsesorRepository asesorRepository,
            MonitorRepository monitorRepository,
            AdministrativoRepository administrativoRepository,
            ConciliadorRepository conciliadorRepository,
            EstudianteRepository estudianteRepository,
            AsesorService asesorService,
            MonitorService monitorService,
            AdministrativoService administrativoService,
            ConciliadorService conciliadorService,
            EstudianteService estudianteService) {

        TipoDocumento cc = buscarTipoDocumento(tipoDocumentoRepository, "CC");
        Sede sedePrincipal = buscarSede(sedeRepository, "Sede principal");
        Area civil = buscarArea(areaRepository, "Civil");

        Asesor asesorBase = null;

        if (asesorRepository.count() == 0) {
            AsesorDTO asesor = new AsesorDTO();
            asesor.setNombre("Carlos Alberto Ramirez");
            asesor.setTipoDocumentoId(cc.getId());
            asesor.setDocumento("1098001122");
            asesor.setEmail("asesor@example.com");
            asesor.setTelefono("3001234567");
            asesor.setUsuario("cramirez");
            asesor.setSedeId(sedePrincipal.getId());
            asesor.setCodigo("ASE001");
            asesor.setAreaId(civil.getId());
            asesor.setActivo(true);

            AsesorDTO asesorCreado = asesorService.crear(asesor);
            asesorBase = asesorRepository.findById(asesorCreado.getId())
                    .orElseThrow(() -> new IllegalStateException("Asesor inicial no encontrado"));
        } else {
            asesorBase = asesorRepository.findAll().get(0);
        }

        if (monitorRepository.count() == 0) {
            MonitorDTO monitor = new MonitorDTO();
            monitor.setNombre("Laura Marcela Torres");
            monitor.setTipoDocumentoId(cc.getId());
            monitor.setDocumento("1098001123");
            monitor.setEmail("monitor@example.com");
            monitor.setTelefono("3001234568");
            monitor.setUsuario("ltorres");
            monitor.setSedeId(sedePrincipal.getId());
            monitor.setCodigo("MON001");
            monitor.setActivo(true);

            monitorService.crear(monitor);
        }

        if (administrativoRepository.count() == 0) {
            AdministrativoDTO administrativo = new AdministrativoDTO();
            administrativo.setNombre("Ana Maria Gomez");
            administrativo.setTipoDocumentoId(cc.getId());
            administrativo.setDocumento("0");
            administrativo.setEmail("admin@example.com");
            administrativo.setTelefono("3001234569");
            administrativo.setUsuario("agomez");
            administrativo.setSedeId(sedePrincipal.getId());
            administrativo.setCodigo("ADM001");
            administrativo.setDirectora(false);
            administrativo.setActivo(true);

            administrativoService.crear(administrativo);
        }

        // Usuarios administrativos adicionales para pruebas con correos reales.
        // Se crean solo si no existen, para no duplicarlos al reiniciar la aplicación.
        crearAdministrativoSiNoExiste(
                administrativoRepository,
                administrativoService,
                cc,
                sedePrincipal,
                "Nelson Enrrique Conde Tarazona",
                "1002003001",
                "nelsonenrriquect@ufps.edu.co",
                "3009876543",
                "nconde",
                "ADM002");

        crearAdministrativoSiNoExiste(
                administrativoRepository,
                administrativoService,
                cc,
                sedePrincipal,
                "Juan Diego Sarmiento Barroyeta",
                "1002003002",
                "juandiegosb@ufps.edu.co",
                "3009876544",
                "jdsarmiento",
                "ADM003");

        if (conciliadorRepository.count() == 0) {
            ConciliadorDTO conciliador = new ConciliadorDTO();
            conciliador.setNombre("Pedro Jose Rojas");
            conciliador.setTipoDocumentoId(cc.getId());
            conciliador.setDocumento("1098001125");
            conciliador.setEmail("conciliador@example.com");
            conciliador.setTelefono("3001234570");
            conciliador.setUsuario("projas");
            conciliador.setSedeId(sedePrincipal.getId());
            conciliador.setCodigo("CON001");
            conciliador.setTipoConciliador(TipoConciliador.INTERNO);
            conciliador.setActivo(true);

            conciliadorService.crear(conciliador);
        }

        if (estudianteRepository.count() == 0) {
            EstudianteDTO estudiante = new EstudianteDTO();
            estudiante.setNombre("Daniel Felipe Mora");
            estudiante.setTipoDocumentoId(cc.getId());
            estudiante.setDocumento("1098001126");
            estudiante.setEmail("estudiante@example.com");
            estudiante.setTelefono("3001234571");
            estudiante.setUsuario("dmora");
            estudiante.setSedeId(sedePrincipal.getId());
            estudiante.setCodigo("EST001");
            estudiante.setAsesorId(asesorBase.getId());
            estudiante.setActivo(true);
            estudiante.setConciliacion(false);

            estudianteService.crear(estudiante);
        }
    }

    private void crearAdministrativoSiNoExiste(
            AdministrativoRepository administrativoRepository,
            AdministrativoService administrativoService,
            TipoDocumento tipoDocumento,
            Sede sede,
            String nombre,
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        boolean existe = administrativoRepository.findAll()
                .stream()
                .anyMatch(administrativo -> administrativo.getEmail().equalsIgnoreCase(email));

        if (existe) {
            return;
        }

        AdministrativoDTO administrativo = new AdministrativoDTO();
        administrativo.setNombre(nombre);
        administrativo.setTipoDocumentoId(tipoDocumento.getId());
        administrativo.setDocumento(documento);
        administrativo.setEmail(email);
        administrativo.setTelefono(telefono);
        administrativo.setUsuario(usuario);
        administrativo.setSedeId(sede.getId());
        administrativo.setCodigo(codigo);
        administrativo.setDirectora(false);
        administrativo.setActivo(true);

        administrativoService.crear(administrativo);
    }

    private TipoDocumento buscarTipoDocumento(
            TipoDocumentoRepository tipoDocumentoRepository,
            String displayname) {

        return tipoDocumentoRepository.findAll()
                .stream()
                .filter(tipo -> tipo.getDisplayName().equalsIgnoreCase(displayname))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tipo de documento no encontrado: " + displayname));
    }

    private Sede buscarSede(
            SedeRepository sedeRepository,
            String nombre) {

        return sedeRepository.findAll()
                .stream()
                .filter(sede -> sede.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Sede no encontrada: " + nombre));
    }

    private Area buscarArea(
            AreaRepository areaRepository,
            String nombre) {

        return areaRepository.findAll()
                .stream()
                .filter(area -> area.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Area no encontrada: " + nombre));
    }
}