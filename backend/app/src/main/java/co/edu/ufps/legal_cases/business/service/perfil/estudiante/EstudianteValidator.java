package co.edu.ufps.legal_cases.business.service.perfil.estudiante;

import java.util.Objects;

import org.springframework.stereotype.Component;

import co.edu.ufps.legal_cases.business.repository.perfil.EstudianteRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

// Valida reglas de negocio para la entidad Estudiante
@Component
public class EstudianteValidator {

    private final EstudianteRepository estudianteRepository;

    public EstudianteValidator(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public void validarIdNoEnviadoEnCreacion(Long id) {
        if (id != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    public void validarIdNoCambiado(Long idRuta, Long idDto) {
        if (idDto != null && !Objects.equals(idDto, idRuta)) {
            throw new BusinessException("No se permite cambiar el id del estudiante");
        }
    }

    public void validarCamposObligatorios(
            String nombre,
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (nombre == null) {
            throw new BusinessException("El nombre es obligatorio");
        }

        if (documento == null || documento.isBlank()) {
            throw new BusinessException("El documento es obligatorio");
        }

        if (email == null || email.isBlank()) {
            throw new BusinessException("El email es obligatorio");
        }

        if (telefono == null || telefono.isBlank()) {
            throw new BusinessException("El teléfono es obligatorio");
        }

        if (usuario == null || usuario.isBlank()) {
            throw new BusinessException("El usuario es obligatorio");
        }

        if (codigo == null || codigo.isBlank()) {
            throw new BusinessException("El código es obligatorio");
        }
    }

    public void validarDuplicadosCreacion(
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (estudianteRepository.existsByDocumento(documento)) {
            throw new BusinessException("Ya existe un estudiante con ese documento");
        }

        if (estudianteRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe un estudiante con ese email");
        }

        if (estudianteRepository.existsByTelefono(telefono)) {
            throw new BusinessException("Ya existe un estudiante con ese teléfono");
        }

        if (estudianteRepository.existsByUsuarioIgnoreCase(usuario)) {
            throw new BusinessException("Ya existe un estudiante con ese usuario");
        }

        if (estudianteRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("Ya existe un estudiante con ese código");
        }
    }

    public void validarDuplicadosActualizacion(
            Long id,
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (estudianteRepository.existsByDocumentoAndIdNot(documento, id)) {
            throw new BusinessException("Ya existe un estudiante con ese documento");
        }

        if (estudianteRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException("Ya existe un estudiante con ese email");
        }

        if (estudianteRepository.existsByTelefonoAndIdNot(telefono, id)) {
            throw new BusinessException("Ya existe un estudiante con ese teléfono");
        }

        if (estudianteRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario, id)) {
            throw new BusinessException("Ya existe un estudiante con ese usuario");
        }

        if (estudianteRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new BusinessException("Ya existe un estudiante con ese código");
        }
    }
}