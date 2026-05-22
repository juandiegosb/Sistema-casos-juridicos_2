package co.edu.ufps.legal_cases.business.service.persona.persona;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.persona.PersonaDTO;
import co.edu.ufps.legal_cases.business.model.persona.Persona;
import co.edu.ufps.legal_cases.business.repository.persona.PersonaRepository;
import co.edu.ufps.legal_cases.business.service.acceso.PersonaAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class PersonaCommandService {

    private final PersonaRepository personaRepository;
    private final PersonaAccessService personaAccessService;
    private final PersonaMapper personaMapper;
    private final PersonaValidator personaValidator;

    public PersonaCommandService(
            PersonaRepository personaRepository,
            PersonaAccessService personaAccessService,
            PersonaMapper personaMapper,
            PersonaValidator personaValidator) {
        this.personaRepository = personaRepository;
        this.personaAccessService = personaAccessService;
        this.personaMapper = personaMapper;
        this.personaValidator = personaValidator;
    }

    @Transactional
    public PersonaDTO crear(PersonaDTO dto) {
        personaAccessService.validarPuedeCrearPersonas();
        personaValidator.validarCreacion(dto);

        String numeroDocumento = personaValidator.normalizarDocumento(dto.getNumeroDocumento());

        personaValidator.validarDocumentoDisponible(numeroDocumento);

        Persona persona = personaMapper.crearEntidad(dto, numeroDocumento);

        return personaMapper.convertirADTO(personaRepository.save(persona));
    }

    @Transactional
    public PersonaDTO actualizar(Long id, PersonaDTO dto) {
        personaAccessService.validarPuedeEditarPersonas();
        personaValidator.validarActualizacion(id, dto);

        Persona persona = buscarPorId(id);
        String numeroDocumentoNuevo = personaValidator.normalizarDocumento(dto.getNumeroDocumento());

        personaValidator.validarDocumentoDisponibleParaActualizacion(persona, numeroDocumentoNuevo);

        // Se actualiza sobre la entidad existente para no tocar campos de control como activo.
        personaMapper.aplicarDatos(persona, dto, numeroDocumentoNuevo);

        return personaMapper.convertirADTO(personaRepository.save(persona));
    }

    @Transactional
    public void desactivar(Long id) {
        personaAccessService.validarPuedeCambiarEstadoPersonas();

        Persona persona = buscarPorId(id);

        persona.setActivo(false);

        personaRepository.save(persona);
    }

    @Transactional
    public void reactivar(Long id) {
        personaAccessService.validarPuedeCambiarEstadoPersonas();

        Persona persona = buscarPorId(id);

        persona.setActivo(true);

        personaRepository.save(persona);
    }

    private Persona buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id de la persona es obligatorio");
        }

        return personaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Persona no encontrada con id: " + id));
    }
}