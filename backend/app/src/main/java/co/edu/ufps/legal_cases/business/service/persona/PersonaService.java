package co.edu.ufps.legal_cases.business.service.persona;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.business.dto.persona.PersonaDTO;
import co.edu.ufps.legal_cases.business.model.persona.Persona;
import co.edu.ufps.legal_cases.business.repository.persona.PersonaRepository;
import co.edu.ufps.legal_cases.exception.BusinessException;

import static co.edu.ufps.legal_cases.util.NormalizacionUtils.estaInformado;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;

@Service
public class PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    public List<PersonaDTO> listar() {
        return personaRepository.findAll()
                .stream()
                .map(persona -> convertirADTO(persona))
                .toList();
    }

    public PersonaDTO obtenerPorId(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Persona no encontrada con id: " + id));

        return convertirADTO(persona);
    }

    public PersonaDTO crear(PersonaDTO personaDTO) {
        validarTelefonoOCorreo(personaDTO);
        validarDatosAcudienteSiEsMenor(personaDTO);

        String numeroDocumento = normalizarNumeroDocumento(personaDTO.getNumeroDocumento());

        if (personaRepository.existsByNumeroDocumento(numeroDocumento)) {
            throw new BusinessException("Ya existe una persona con ese numero de documento");
        }

        Persona persona = convertirAEntidad(personaDTO);
        persona.setNumeroDocumento(numeroDocumento);

        Persona personaGuardada = personaRepository.save(persona);
        return convertirADTO(personaGuardada);
    }

    public PersonaDTO actualizar(Long id, PersonaDTO personaDTO) {
        Persona personaExistente = personaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Persona no encontrada con id: " + id));

        validarTelefonoOCorreo(personaDTO);
        validarDatosAcudienteSiEsMenor(personaDTO);

        String numeroDocumentoNuevo = normalizarNumeroDocumento(personaDTO.getNumeroDocumento());

        if (!personaExistente.getNumeroDocumento().equalsIgnoreCase(numeroDocumentoNuevo)
                && personaRepository.existsByNumeroDocumento(numeroDocumentoNuevo)) {
            throw new BusinessException("Ya existe una persona con ese numero de documento");
        }

        Persona personaActualizada = convertirAEntidad(personaDTO);
        personaActualizada.setId(personaExistente.getId());
        personaActualizada.setNumeroDocumento(numeroDocumentoNuevo);

        Persona personaGuardada = personaRepository.save(personaActualizada);
        return convertirADTO(personaGuardada);
    }

    public void eliminar(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Persona no encontrada con id: " + id));

        personaRepository.delete(persona);
    }

    private boolean esMenorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return false;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18;
    }

    private void validarDatosAcudienteSiEsMenor(PersonaDTO personaDTO) {
        if (esMenorDeEdad(personaDTO.getFechaNacimiento())) {
            if (!estaInformado(personaDTO.getNombreCompletoAcudiente())) {
                throw new BusinessException("Si la persona es menor de edad, el nombre del acudiente es obligatorio");
            }

            if (!estaInformado(personaDTO.getRelacionAcudiente())) {
                throw new BusinessException("Si la persona es menor de edad, la relacion del acudiente es obligatoria");
            }

            boolean telefonoAcudienteInformado = estaInformado(personaDTO.getTelefonoAcudiente());
            boolean correoAcudienteInformado = estaInformado(personaDTO.getCorreoAcudiente());

            if (!telefonoAcudienteInformado && !correoAcudienteInformado) {
                throw new BusinessException(
                        "Si la persona es menor de edad, debe informar telefono o correo del acudiente");
            }
        }
    }

    // En algunos casos el frontend envia "No informa" cuando la parte no tiene info
    // de la contraparte
    private void validarTelefonoOCorreo(PersonaDTO personaDTO) {
        boolean telefonoInformado = estaInformado(personaDTO.getTelefono());
        boolean correoInformado = estaInformado(personaDTO.getCorreo());

        if (!telefonoInformado && !correoInformado) {
            throw new BusinessException("Debe informar al menos telefono o correo");
        }
    }

    private PersonaDTO convertirADTO(Persona persona) {
        return new PersonaDTO(
                persona.getId(),
                persona.getTipoUsuario(),
                persona.getTipoDocumento(),
                persona.getNumeroDocumento(),
                persona.getFechaExpedicion(),
                persona.getCiudadExpedicion(),
                persona.getNombres(),
                persona.getApellidos(),
                persona.getNombreIdentitario(),
                persona.getPronombre(),
                persona.getSexo(),
                persona.getGenero(),
                persona.getOrientacionSexual(),
                persona.getFechaNacimiento(),
                persona.getTelefono(),
                persona.getCorreo(),
                persona.getNacionalidad(),
                persona.getEstadoCivil(),
                persona.getEscolaridad(),
                persona.getGrupoEtnico(),
                persona.getCondicionActual(),
                persona.getSabeLeerEscribir(),
                persona.getDiscapacidad(),
                persona.getCaracterizacionPcd(),
                persona.getNecesitaAjustePcd(),
                persona.getDepartamento(),
                persona.getMunicipio(),
                persona.getBarrio(),
                persona.getDireccion(),
                persona.getComuna(),
                persona.getLocalidad(),
                persona.getEstrato(),
                persona.getTipoVivienda(),
                persona.getZona(),
                persona.getTenencia(),
                persona.getNumeroPersonasACargo(),
                persona.getIngresosAdicionales(),
                persona.getEnergiaElectrica(),
                persona.getAcueducto(),
                persona.getAlcantarillado(),
                persona.getOcupacion(),
                persona.getEmpresa(),
                persona.getSalario(),
                persona.getCargo(),
                persona.getDireccionEmpresa(),
                persona.getTelefonoEmpresa(),
                persona.getNombreCompletoAcudiente(),
                persona.getRelacionAcudiente(),
                persona.getTelefonoAcudiente(),
                persona.getCorreoAcudiente(),
                persona.getDireccionAcudiente(),
                persona.getComoSeEntero(),
                persona.getRelacionConUniversidad());
    }

    private Persona convertirAEntidad(PersonaDTO dto) {
        Persona persona = new Persona();

        persona.setId(dto.getId());
        persona.setTipoUsuario(normalizarTexto(dto.getTipoUsuario()));
        persona.setTipoDocumento(normalizarTexto(dto.getTipoDocumento()));
        persona.setNumeroDocumento(normalizarNumeroDocumento(dto.getNumeroDocumento()));
        persona.setFechaExpedicion(dto.getFechaExpedicion());
        persona.setCiudadExpedicion(normalizarTexto(dto.getCiudadExpedicion()));
        persona.setNombres(normalizarTexto(dto.getNombres()));
        persona.setApellidos(normalizarTexto(dto.getApellidos()));
        persona.setNombreIdentitario(normalizarTexto(dto.getNombreIdentitario()));
        persona.setPronombre(normalizarTexto(dto.getPronombre()));
        persona.setSexo(normalizarTexto(dto.getSexo()));
        persona.setGenero(normalizarTexto(dto.getGenero()));
        persona.setOrientacionSexual(normalizarTexto(dto.getOrientacionSexual()));
        persona.setFechaNacimiento(dto.getFechaNacimiento());
        persona.setTelefono(normalizarTelefono(dto.getTelefono()));
        persona.setCorreo(normalizarTexto(dto.getCorreo()));
        persona.setNacionalidad(normalizarTexto(dto.getNacionalidad()));
        persona.setEstadoCivil(normalizarTexto(dto.getEstadoCivil()));
        persona.setEscolaridad(normalizarTexto(dto.getEscolaridad()));
        persona.setGrupoEtnico(normalizarTexto(dto.getGrupoEtnico()));
        persona.setCondicionActual(normalizarTexto(dto.getCondicionActual()));
        persona.setSabeLeerEscribir(dto.getSabeLeerEscribir());
        persona.setDiscapacidad(normalizarTexto(dto.getDiscapacidad()));
        persona.setCaracterizacionPcd(normalizarTexto(dto.getCaracterizacionPcd()));
        persona.setNecesitaAjustePcd(dto.getNecesitaAjustePcd());

        persona.setDepartamento(normalizarTexto(dto.getDepartamento()));
        persona.setMunicipio(normalizarTexto(dto.getMunicipio()));
        persona.setBarrio(normalizarTexto(dto.getBarrio()));
        persona.setDireccion(normalizarTexto(dto.getDireccion()));
        persona.setComuna(normalizarTexto(dto.getComuna()));
        persona.setLocalidad(normalizarTexto(dto.getLocalidad()));
        persona.setEstrato(dto.getEstrato());
        persona.setTipoVivienda(normalizarTexto(dto.getTipoVivienda()));
        persona.setZona(normalizarTexto(dto.getZona()));
        persona.setTenencia(normalizarTexto(dto.getTenencia()));
        persona.setNumeroPersonasACargo(dto.getNumeroPersonasACargo());
        persona.setIngresosAdicionales(dto.getIngresosAdicionales());
        persona.setEnergiaElectrica(dto.getEnergiaElectrica());
        persona.setAcueducto(dto.getAcueducto());
        persona.setAlcantarillado(dto.getAlcantarillado());

        persona.setOcupacion(normalizarTexto(dto.getOcupacion()));
        persona.setEmpresa(normalizarTexto(dto.getEmpresa()));
        persona.setSalario(dto.getSalario());
        persona.setCargo(normalizarTexto(dto.getCargo()));
        persona.setDireccionEmpresa(normalizarTexto(dto.getDireccionEmpresa()));
        persona.setTelefonoEmpresa(normalizarTelefono(dto.getTelefonoEmpresa()));

        persona.setNombreCompletoAcudiente(normalizarTexto(dto.getNombreCompletoAcudiente()));
        persona.setRelacionAcudiente(normalizarTexto(dto.getRelacionAcudiente()));
        persona.setTelefonoAcudiente(normalizarTelefono(dto.getTelefonoAcudiente()));
        persona.setCorreoAcudiente(normalizarTexto(dto.getCorreoAcudiente()));
        persona.setDireccionAcudiente(normalizarTexto(dto.getDireccionAcudiente()));

        persona.setComoSeEntero(normalizarTexto(dto.getComoSeEntero()));
        persona.setRelacionConUniversidad(normalizarTexto(dto.getRelacionConUniversidad()));

        return persona;
    }
}