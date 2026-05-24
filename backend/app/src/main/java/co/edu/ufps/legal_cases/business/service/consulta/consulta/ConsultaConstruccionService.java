package co.edu.ufps.legal_cases.business.service.consulta.consulta;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaDTO;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.perfil.Asesor;
import co.edu.ufps.legal_cases.business.model.perfil.Estudiante;
import co.edu.ufps.legal_cases.business.service.acceso.consulta.ConsultaAccessService;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;
import lombok.AllArgsConstructor;

// Centraliza la construcción de Consulta desde el DTO.
// Mantiene fuera del CommandService la asignación de relaciones,
// normalización de textos y asignación automática de responsables.
@Service
@AllArgsConstructor
public class ConsultaConstruccionService {

    private final ConsultaRelacionService consultaRelacionService;
    private final ConsultaAccessService consultaAccessService;

    public Consulta aplicarDatos(
            Consulta consulta,
            ConsultaDTO dto,
            boolean puedeAsignarResponsables) {

        consulta.setFecha(dto.getFecha());
        consulta.setDescripcion(normalizarTexto(dto.getDescripcion()));
        consulta.setHechos(normalizarTexto(dto.getHechos()));
        consulta.setPretensiones(normalizarTexto(dto.getPretensiones()));
        consulta.setConceptoJuridico(normalizarTexto(dto.getConceptoJuridico()));
        consulta.setTramite(normalizarTexto(dto.getTramite()));
        consulta.setObservaciones(normalizarTexto(dto.getObservaciones()));
        consulta.setTipoViolencia(normalizarTexto(dto.getTipoViolencia()));
        consulta.setEstado(dto.getEstado());
        consulta.setResultado(normalizarTexto(dto.getResultado()));

        consulta.setPersona(consultaRelacionService.obtenerPersona(dto.getPersonaId()));
        consulta.setSede(consultaRelacionService.obtenerSede(dto.getSedeId()));
        consulta.setArea(consultaRelacionService.obtenerArea(dto.getAreaId()));
        consulta.setTema(consultaRelacionService.obtenerTema(dto.getTemaId()));
        consulta.setTipo(consultaRelacionService.obtenerTipo(dto.getTipoId()));

        if (puedeAsignarResponsables) {
            asignarResponsablesDesdeDto(consulta, dto);
        }

        consulta.getPartes().clear();
        consulta.getPartes().addAll(consultaRelacionService.obtenerPersonas(dto.getPartesIds()));

        consulta.getContrapartes().clear();
        consulta.getContrapartes().addAll(consultaRelacionService.obtenerPersonas(dto.getContrapartesIds()));

        return consulta;
    }

    public void asignarResponsablesSegunUsuarioActual(Consulta consulta) {
        PerfilUsuarioActual perfil = consultaAccessService.obtenerPerfilActual();

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.ESTUDIANTE) {
            Estudiante estudiante = consultaRelacionService.obtenerEstudiante(perfil.getPerfilId());
            consulta.setEstudiante(estudiante);
            consulta.setAsesor(estudiante.getAsesor());
            return;
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.ASESOR) {
            consulta.setAsesor(consultaRelacionService.obtenerAsesor(perfil.getPerfilId()));
            return;
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.MONITOR) {
            consulta.setMonitor(consultaRelacionService.obtenerMonitor(perfil.getPerfilId()));
        }
    }

    private void asignarResponsablesDesdeDto(Consulta consulta, ConsultaDTO dto) {
        Asesor asesor = consultaRelacionService.obtenerAsesor(dto.getAsesorId());
        Estudiante estudiante = consultaRelacionService.obtenerEstudiante(dto.getEstudianteId());

        // Si se asigna estudiante sin asesor explícito, se toma el asesor activo del
        // estudiante.
        // Esto evita consultas con estudiante asignado pero sin responsable académico.
        if (asesor == null && estudiante != null) {
            asesor = consultaRelacionService.obtenerAsesorDelEstudianteActivo(estudiante);
        }

        consulta.setAsesor(asesor);
        consulta.setMonitor(consultaRelacionService.obtenerMonitor(dto.getMonitorId()));
        consulta.setEstudiante(estudiante);
    }
}