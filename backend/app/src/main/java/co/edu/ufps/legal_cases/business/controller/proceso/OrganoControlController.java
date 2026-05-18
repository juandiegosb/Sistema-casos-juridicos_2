package co.edu.ufps.legal_cases.business.controller.proceso;

import co.edu.ufps.legal_cases.business.dto.proceso.OrganoControlDTO;
import co.edu.ufps.legal_cases.business.service.proceso.OrganoControlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organos-control")
@PreAuthorize("hasAuthority('Gestionar catálogos')")
public class OrganoControlController {

    private final OrganoControlService organoControlService;

    public OrganoControlController(OrganoControlService organoControlService) {
        this.organoControlService = organoControlService;
    }

    @GetMapping
    public List<OrganoControlDTO> listar() {
        return organoControlService.listar();
    }

    @GetMapping("/{id}")
    public OrganoControlDTO obtenerPorId(@PathVariable Long id) {
        return organoControlService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganoControlDTO crear(@Valid @RequestBody OrganoControlDTO dto) {
        return organoControlService.crear(dto);
    }

    @PutMapping("/{id}")
    public OrganoControlDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrganoControlDTO dto
    ) {
        return organoControlService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        organoControlService.eliminar(id);
    }
}