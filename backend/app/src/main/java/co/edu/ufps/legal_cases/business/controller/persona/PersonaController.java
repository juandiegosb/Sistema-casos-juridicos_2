package co.edu.ufps.legal_cases.business.controller.persona;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.persona.PersonaDTO;
import co.edu.ufps.legal_cases.business.service.persona.PersonaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/personas")
@PreAuthorize("hasAuthority('Gestionar personas')")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping
    public List<PersonaDTO> listar() {
        return personaService.listar();
    }

    @GetMapping("/{id}")
    public PersonaDTO obtenerPorId(@PathVariable Long id) {
        return personaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    //En el correo no se puede usar el "No informa" como estrategia porque en @Email no se admite (debe ser null o correcto)
    public PersonaDTO crear(@Valid @RequestBody PersonaDTO personaDTO) {
        return personaService.crear(personaDTO);
    }

    @PutMapping("/{id}")
    public PersonaDTO actualizar(@PathVariable Long id, @Valid @RequestBody PersonaDTO personaDTO) {
        return personaService.actualizar(id, personaDTO);
    }

    @GetMapping("/activos")
    public List<PersonaDTO> listarActivos() {
        return personaService.listarActivos();
    }

    @PatchMapping("/{id}/desactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        personaService.desactivar(id);
    }

    @PatchMapping("/{id}/reactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivar(@PathVariable Long id) {
        personaService.reactivar(id);
    }
}