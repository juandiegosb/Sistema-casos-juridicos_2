package co.edu.ufps.legal_cases.business.service.proceso;

import co.edu.ufps.legal_cases.business.dto.proceso.EspecialidadDTO;
import co.edu.ufps.legal_cases.business.model.proceso.Especialidad;
import co.edu.ufps.legal_cases.business.repository.proceso.EspecialidadRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import co.edu.ufps.legal_cases.business.repository.proceso.OrganoControlRepository;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;


import java.util.List;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

@Service
public class EspecialidadService {
    private final OrganoControlRepository organoControlRepository;
    private final EspecialidadRepository especialidadRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository, OrganoControlRepository organoControlRepository) {
        this.especialidadRepository = especialidadRepository;
        this.organoControlRepository = organoControlRepository;
    }

    public List<EspecialidadDTO> listar() {
        return especialidadRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public EspecialidadDTO obtenerPorId(Long id) {
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Especialidad no encontrada con id: " + id));

        return convertirADTO(especialidad);
    }

    public EspecialidadDTO crear(EspecialidadDTO dto) {

        String nombre = normalizarTexto(dto.getNombre());


        OrganoControl organoControl = organoControlRepository.findById(dto.getOrganoControlId())
                .orElseThrow(() -> new BusinessException(
                        "Órgano de control no encontrado con id: " + dto.getOrganoControlId()
                ));

        if (especialidadRepository.existsByNombreIgnoreCaseAndOrganoControlId(nombre,organoControl.getId())) {
            throw new BusinessException("Ya existe una especialidad con ese nombre");
        }

        Especialidad especialidad = new Especialidad();
        especialidad.setOrganoControl(organoControl);
        especialidad.setNombre(nombre);
        especialidad.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        Especialidad guardado = especialidadRepository.save(especialidad);

        return convertirADTO(guardado);
    }

    public EspecialidadDTO actualizar(Long id, EspecialidadDTO dto) {

        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Especialidad no encontrada con id: " + id
                ));

        String nuevoNombre = normalizarTexto(dto.getNombre());

        OrganoControl organoControl = organoControlRepository
                .findById(dto.getOrganoControlId())
                .orElseThrow(() -> new BusinessException(
                        "Órgano de control no encontrado con id: "
                                + dto.getOrganoControlId()
                ));

        boolean mismoNombre =
                especialidad.getNombre().equalsIgnoreCase(nuevoNombre);

        boolean mismoActivo =
                especialidad.getActivo().equals(dto.getActivo());

        boolean mismoOrganoControl =
                especialidad.getOrganoControl()
                        .getId()
                        .equals(organoControl.getId());

        if (mismoNombre && mismoActivo && mismoOrganoControl) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        boolean existeDuplicado =
                especialidadRepository.existsByNombreIgnoreCaseAndOrganoControlId(
                        nuevoNombre,
                        organoControl.getId()
                );

        boolean cambioNombreOOrgano =
                !especialidad.getNombre().equalsIgnoreCase(nuevoNombre)
                        || !especialidad.getOrganoControl()
                        .getId()
                        .equals(organoControl.getId());

        if (cambioNombreOOrgano && existeDuplicado) {
            throw new BusinessException(
                    "Ya existe una especialidad con ese nombre para el órgano de control seleccionado"
            );
        }

        especialidad.setNombre(nuevoNombre);
        especialidad.setOrganoControl(organoControl);
        especialidad.setActivo(dto.getActivo());

        Especialidad actualizado = especialidadRepository.save(especialidad);

        return convertirADTO(actualizado);
    }

    public void eliminar(Long id) {

        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Especialidad no encontrado con id: " + id));

        especialidadRepository.delete(especialidad);
    }

    private EspecialidadDTO convertirADTO(Especialidad especialidad) {
        return new EspecialidadDTO(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getOrganoControl().getId(),
                especialidad.getActivo()
        );
    }
}