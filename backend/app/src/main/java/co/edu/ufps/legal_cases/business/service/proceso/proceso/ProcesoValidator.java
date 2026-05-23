package co.edu.ufps.legal_cases.business.service.proceso.proceso;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.Objects;

import org.springframework.stereotype.Component;

import co.edu.ufps.legal_cases.business.dto.proceso.ProcesoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Departamento;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.proceso.Especialidad;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;
import co.edu.ufps.legal_cases.business.model.proceso.Proceso;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

// Valida las reglas propias del módulo de procesos.
// No valida permisos ni alcance, porque eso pertenece a ProcesoAccessService.
@Component
public class ProcesoValidator {

    public void validarCreacion(ProcesoDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    public void validarActualizacion(Long id, ProcesoDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null && !Objects.equals(dto.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id del proceso");
        }
    }

    public String normalizarNumeroRadicado(String numeroRadicado) {
        String normalizado = normalizarTexto(numeroRadicado);

        if (normalizado == null || normalizado.isBlank()) {
            throw new BusinessException("El número de radicado es obligatorio");
        }

        if (normalizado.length() != 23) {
            throw new BusinessException("El número de radicado debe tener exactamente 23 caracteres");
        }

        return normalizado;
    }

    public void validarNoCambieConsulta(Proceso proceso, ProcesoDTO dto) {
        if (dto.getConsultaId() == null) {
            throw new BusinessException("La consulta es obligatoria");
        }

        Long consultaActualId = proceso.getConsulta() != null
                ? proceso.getConsulta().getId()
                : null;

        // La consulta define el alcance del proceso, por eso no se permite cambiarla en
        // edición.
        if (!Objects.equals(consultaActualId, dto.getConsultaId())) {
            throw new BusinessException("No se permite cambiar la consulta de un proceso existente");
        }
    }

    public void validarEspecialidadPerteneceAlOrgano(
            Especialidad especialidad,
            OrganoControl organoControl) {

        if (especialidad == null) {
            return;
        }

        if (organoControl == null) {
            throw new BusinessException("Debe seleccionar un órgano de control para la especialidad");
        }

        if (especialidad.getOrganoControl() == null
                || !Objects.equals(especialidad.getOrganoControl().getId(), organoControl.getId())) {
            throw new BusinessException("La especialidad no pertenece al órgano de control seleccionado");
        }
    }

    public void validarExistenCambios(
            Proceso proceso,
            DatosProceso datos) {

        if (sinCambios(proceso, datos)) {
            throw new BusinessException("No hay cambios para actualizar");
        }
    }

    private void validarDtoObligatorio(ProcesoDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos del proceso son obligatorios");
        }
    }

    private boolean sinCambios(
            Proceso proceso,
            DatosProceso datos) {

        return Objects.equals(proceso.getNumeroRadicado(), datos.numeroRadicado())
                && Objects.equals(idDepartamento(proceso.getDepartamento()), idDepartamento(datos.departamento()))
                && Objects.equals(idConsulta(proceso.getConsulta()), idConsulta(datos.consulta()))
                && Objects.equals(idOrganoControl(proceso.getOrganoControl()), idOrganoControl(datos.organoControl()))
                && Objects.equals(idEspecialidad(proceso.getEspecialidad()), idEspecialidad(datos.especialidad()));
    }

    private Long idDepartamento(Departamento departamento) {
        return departamento != null ? departamento.getId() : null;
    }

    private Long idConsulta(Consulta consulta) {
        return consulta != null ? consulta.getId() : null;
    }

    private Long idOrganoControl(OrganoControl organoControl) {
        return organoControl != null ? organoControl.getId() : null;
    }

    private Long idEspecialidad(Especialidad especialidad) {
        return especialidad != null ? especialidad.getId() : null;
    }
}