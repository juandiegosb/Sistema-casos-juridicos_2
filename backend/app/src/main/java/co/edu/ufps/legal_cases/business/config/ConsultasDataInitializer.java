package co.edu.ufps.legal_cases.business.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import co.edu.ufps.legal_cases.business.model.Area;
import co.edu.ufps.legal_cases.business.model.Consulta;
import co.edu.ufps.legal_cases.business.model.Persona;
import co.edu.ufps.legal_cases.business.model.Sede;
import co.edu.ufps.legal_cases.business.model.Tema;
import co.edu.ufps.legal_cases.business.repository.AreaRepository;
import co.edu.ufps.legal_cases.business.repository.ConsultaRepository;
import co.edu.ufps.legal_cases.business.repository.PersonaRepository;
import co.edu.ufps.legal_cases.business.repository.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.TemaRepository;

@Configuration
public class ConsultasDataInitializer {

    @Bean
    @Order(6)
    CommandLineRunner initConsultas(
            ConsultaRepository consultaRepository,
            PersonaRepository personaRepository,
            SedeRepository sedeRepository,
            AreaRepository areaRepository,
            TemaRepository temaRepository) {
        return args -> {

            if (consultaRepository.count() > 5) {
                return;
            }

            // Tomamos los datos ya inicializados por los otros initializers
            Persona persona1 = personaRepository.findAll().get(0);
            Sede sede = sedeRepository.findAll().get(0);
            Area civil = areaRepository.findAll().get(0);
            Area penal = areaRepository.findAll().get(1);
            Area laboral = areaRepository.findAll().get(2);
            Tema contratos = temaRepository.findAll().get(0);
            Tema sucesiones = temaRepository.findAll().get(1);
            Tema lesiones = temaRepository.findAll().get(2);
            Tema despidos = temaRepository.findAll().get(3);

            // Consulta 1 - Juan Perez (persona ya existente)
            Consulta c1 = new Consulta();
            c1.setFecha(LocalDate.of(2024, 2, 10));
            c1.setDescripcion("Incumplimiento de contrato de arrendamiento");
            c1.setHechos("El arrendatario dejó de pagar el canon mensual desde hace 3 meses sin justificación");
            c1.setPretensiones("Terminación del contrato y pago de cánones adeudados");
            c1.setConceptoJuridico("Aplica artículo 2000 del Código Civil sobre incumplimiento de obligaciones contractuales");
            c1.setTramite("Proceso verbal sumario");
            c1.setEstado("Activo");
            c1.setPersona(persona1);
            c1.setSede(sede);
            c1.setArea(civil);
            c1.setTema(contratos);
            consultaRepository.save(c1);

            // Consulta 2 - María González (segunda persona si existe, si no usa la misma)
            Persona persona2 = personaRepository.findAll().size() > 1
                    ? personaRepository.findAll().get(1)
                    : persona1;

            Consulta c2 = new Consulta();
            c2.setFecha(LocalDate.of(2024, 4, 22));
            c2.setDescripcion("Reclamación de herencia por exclusión testamentaria");
            c2.setHechos("El causante excluyó a uno de sus hijos del testamento sin causa legal justificada");
            c2.setPretensiones("Reconocimiento de la porción legítima de herencia");
            c2.setConceptoJuridico("Código Civil artículo 1226, protección de la legítima rigurosa");
            c2.setTramite("Proceso de sucesión");
            c2.setEstado("En proceso");
            c2.setPersona(persona2);
            c2.setSede(sede);
            c2.setArea(civil);
            c2.setTema(sucesiones);
            consultaRepository.save(c2);

            // Consulta 3
            Persona persona3 = personaRepository.findAll().size() > 2
                    ? personaRepository.findAll().get(2)
                    : persona1;

            Consulta c3 = new Consulta();
            c3.setFecha(LocalDate.of(2024, 6, 5));
            c3.setDescripcion("Lesiones personales en accidente de tránsito");
            c3.setHechos("El consultante sufrió fracturas en brazo derecho por colisión vehicular causada por tercero");
            c3.setPretensiones("Indemnización por daños físicos y perjuicios morales");
            c3.setConceptoJuridico("Artículo 111 del Código Penal, responsabilidad civil extracontractual");
            c3.setTramite("Conciliación extrajudicial");
            c3.setEstado("Pendiente");
            c3.setPersona(persona3);
            c3.setSede(sede);
            c3.setArea(penal);
            c3.setTema(lesiones);
            consultaRepository.save(c3);

            // Consulta 4
            Persona persona4 = personaRepository.findAll().size() > 3
                    ? personaRepository.findAll().get(3)
                    : persona1;

            Consulta c4 = new Consulta();
            c4.setFecha(LocalDate.of(2024, 8, 14));
            c4.setDescripcion("Despido injustificado después de 7 años de servicio");
            c4.setHechos("El empleado fue despedido sin justa causa y sin el pago de prestaciones sociales completas");
            c4.setPretensiones("Pago de indemnización, cesantías y demás prestaciones pendientes");
            c4.setConceptoJuridico("Artículo 64 del Código Sustantivo del Trabajo, indemnización por terminación unilateral");
            c4.setTramite("Conciliación laboral");
            c4.setEstado("Activo");
            c4.setPersona(persona4);
            c4.setSede(sede);
            c4.setArea(laboral);
            c4.setTema(despidos);
            consultaRepository.save(c4);

            // Consulta 5
            Consulta c5 = new Consulta();
            c5.setFecha(LocalDate.of(2024, 10, 30));
            c5.setDescripcion("Violencia intrafamiliar y solicitud de medidas de protección");
            c5.setHechos("La consultante reporta agresiones físicas y verbales reiteradas por parte de su cónyuge");
            c5.setPretensiones("Medida de protección y separación de cuerpos");
            c5.setConceptoJuridico("Ley 294 de 1996, protección contra la violencia intrafamiliar");
            c5.setTramite("Medida de protección");
            c5.setTipoViolencia("Violencia física y psicológica");
            c5.setEstado("Urgente");
            c5.setPersona(persona1);
            c5.setSede(sede);
            c5.setArea(penal);
            c5.setTema(lesiones);
            consultaRepository.save(c5);
        };
    }
}