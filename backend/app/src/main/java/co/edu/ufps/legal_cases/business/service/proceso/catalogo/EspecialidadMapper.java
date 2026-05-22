package co.edu.ufps.legal_cases.business.service.proceso.catalogo;

import org.springframework.stereotype.Component;

import co.edu.ufps.legal_cases.business.dto.proceso.EspecialidadDTO;
import co.edu.ufps.legal_cases.business.model.proceso.Especialidad;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;

@Component
public class EspecialidadMapper {

    // Define cómo se expone la especialidad hacia la API.
    public EspecialidadDTO convertirADTO(Especialidad especialidad) {
        return new EspecialidadDTO(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getOrganoControl().getId(),
                especialidad.getActivo());
    }

    public Especialidad crearEntidad(String nombre, OrganoControl organoControl, Boolean activo) {
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(nombre);
        especialidad.setOrganoControl(organoControl);
        especialidad.setActivo(activo != null ? activo : true);
        return especialidad;
    }

    public void aplicarDatos(
            Especialidad especialidad,
            String nombre,
            OrganoControl organoControl,
            Boolean activo) {
        especialidad.setNombre(nombre);
        especialidad.setOrganoControl(organoControl);
        especialidad.setActivo(activo);
    }
}