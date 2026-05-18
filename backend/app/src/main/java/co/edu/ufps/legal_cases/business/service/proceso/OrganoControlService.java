package co.edu.ufps.legal_cases.business.service.proceso;

import co.edu.ufps.legal_cases.business.dto.proceso.OrganoControlDTO;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;
import co.edu.ufps.legal_cases.business.repository.proceso.OrganoControlRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

@Service
public class OrganoControlService {

    private final OrganoControlRepository organoControlRepository;

    public OrganoControlService(OrganoControlRepository organoControlRepository) {
        this.organoControlRepository = organoControlRepository;
    }

    public List<OrganoControlDTO> listar() {
        return organoControlRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public OrganoControlDTO obtenerPorId(Long id) {
        OrganoControl organoControl = organoControlRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Organo de control no encontrado con id: " + id));

        return convertirADTO(organoControl);
    }

    public OrganoControlDTO crear(OrganoControlDTO dto) {

        String nombre = normalizarTexto(dto.getNombre());

        if (organoControlRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un organo de control con ese nombre");
        }

        OrganoControl organoControl = new OrganoControl();
        organoControl.setNombre(nombre);
        organoControl.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        OrganoControl guardado = organoControlRepository.save(organoControl);

        return convertirADTO(guardado);
    }

    public OrganoControlDTO actualizar(Long id, OrganoControlDTO dto) {

        OrganoControl organoControl = organoControlRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Organo de control no encontrado con id: " + id));

        String nuevoNombre = normalizarTexto(dto.getNombre());

        boolean mismoNombre = organoControl.getNombre().equalsIgnoreCase(nuevoNombre);
        boolean mismoActivo = organoControl.getActivo().equals(dto.getActivo());

        if (mismoNombre && mismoActivo) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        if (!organoControl.getNombre().equalsIgnoreCase(nuevoNombre)
                && organoControlRepository.existsByNombreIgnoreCase(nuevoNombre)) {

            throw new BusinessException("Ya existe un organo de control con ese nombre");
        }

        organoControl.setNombre(nuevoNombre);
        organoControl.setActivo(dto.getActivo());

        OrganoControl actualizado = organoControlRepository.save(organoControl);

        return convertirADTO(actualizado);
    }

    public void eliminar(Long id) {

        OrganoControl organoControl = organoControlRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Organo de control no encontrado con id: " + id));

        organoControlRepository.delete(organoControl);
    }

    private OrganoControlDTO convertirADTO(OrganoControl organoControl) {
        return new OrganoControlDTO(
                organoControl.getId(),
                organoControl.getNombre(),
                organoControl.getActivo()
        );
    }
}