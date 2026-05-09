package co.edu.ufps.legal_cases.business.service.perfil;

import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.equalsIgnoreCase;
import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.mismoId;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarCodigo;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarUsuario;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.perfil.EstudianteDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Asesor;
import co.edu.ufps.legal_cases.business.model.perfil.Estudiante;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.AsesorRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.EstudianteRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaRegistroService;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final AsesorRepository asesorRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;

    public EstudianteService(
            EstudianteRepository estudianteRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            AsesorRepository asesorRepository,
            UsuarioSistemaRegistroService usuarioSistemaRegistroService) {
        this.estudianteRepository = estudianteRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sedeRepository = sedeRepository;
        this.asesorRepository = asesorRepository;
        this.usuarioSistemaRegistroService = usuarioSistemaRegistroService;
    }

    public List<EstudianteDTO> listar() {
        return estudianteRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<EstudianteDTO> listarActivos() {
        return estudianteRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<EstudianteDTO> listarConConciliacion() {
        return estudianteRepository.findByConciliacionTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<EstudianteDTO> listarPorAsesor(Long asesorId) {
        return estudianteRepository.findByAsesorId(asesorId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public EstudianteDTO obtenerPorId(Long id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado con id: " + id));

        return convertirADTO(estudiante);
    }

    @Transactional
    public EstudianteDTO crear(EstudianteDTO dto) {
        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(nombre, documento, email, telefono, usuario, codigo);
        validarDuplicadosCreacion(documento, email, telefono, usuario, codigo);

        //En estos metodos valido que efectivamente existan esas entidades relacionadas
        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        // Regla de negocio: todo estudiante debe estar asociado a un asesor existente y activo.
        Asesor asesor = obtenerAsesor(dto.getAsesorId());

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(nombre);
        estudiante.setTipoDocumento(tipoDocumento);
        estudiante.setDocumento(documento);
        estudiante.setEmail(email);
        estudiante.setTelefono(telefono);
        estudiante.setUsuario(usuario);
        estudiante.setSede(sede);
        estudiante.setCodigo(codigo);
        estudiante.setAsesor(asesor);
        estudiante.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        estudiante.setConciliacion(dto.getConciliacion() != null ? dto.getConciliacion() : false);

        Estudiante estudianteGuardado = estudianteRepository.save(estudiante);

        //Aqui estoy creando el usuario ante el sistema
        // También se conserva temporalmente la relación vieja UsuarioSistema.estudiante.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaEstudiante(estudianteGuardado);

        // Nueva relación normalizada.
        // Ahora el perfil real estudiante apunta al usuario del sistema.
        estudianteGuardado.setUsuarioSistema(usuarioSistema);

        Estudiante estudianteActualizado = estudianteRepository.save(estudianteGuardado);

        return convertirADTO(estudianteActualizado);
    }

    public EstudianteDTO actualizar(Long id, EstudianteDTO dto) {
        Estudiante existente = estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado con id: " + id));

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(nombre, documento, email, telefono, usuario, codigo);
        validarDuplicadosActualizacion(id, documento, email, telefono, usuario, codigo);

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());
        Asesor asesor = obtenerAsesor(dto.getAsesorId());

        Boolean nuevoActivo = dto.getActivo() != null ? dto.getActivo() : existente.getActivo();
        Boolean nuevaConciliacion = dto.getConciliacion() != null ? dto.getConciliacion() : existente.getConciliacion();

        if (dto.getId() != null && !dto.getId().equals(existente.getId())) {
            throw new BusinessException("No se permite cambiar el id del estudiante");
        }

        boolean sinCambios =
                equalsIgnoreCase(existente.getNombre(), nombre)
                && mismoId(existente.getTipoDocumento(), tipoDocumento, TipoDocumento::getId)
                && Objects.equals(existente.getDocumento(), documento)
                && equalsIgnoreCase(existente.getEmail(), email)
                && Objects.equals(existente.getTelefono(), telefono)
                && equalsIgnoreCase(existente.getUsuario(), usuario)
                && mismoId(existente.getSede(), sede, Sede::getId)
                && equalsIgnoreCase(existente.getCodigo(), codigo)
                && mismoId(existente.getAsesor(), asesor, Asesor::getId)
                && Objects.equals(existente.getActivo(), nuevoActivo)
                && Objects.equals(existente.getConciliacion(), nuevaConciliacion);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        existente.setNombre(nombre);
        existente.setTipoDocumento(tipoDocumento);
        existente.setDocumento(documento);
        existente.setEmail(email);
        existente.setTelefono(telefono);
        existente.setUsuario(usuario);
        existente.setSede(sede);
        existente.setCodigo(codigo);
        existente.setAsesor(asesor);
        existente.setActivo(nuevoActivo);
        existente.setConciliacion(nuevaConciliacion);

        return convertirADTO(estudianteRepository.save(existente));
    }

    public EstudianteDTO cambiarEstado(Long id, Boolean activo) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado con id: " + id));

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(estudiante.getActivo(), activo)) {
            throw new BusinessException("El estudiante ya tiene ese estado");
        }

        estudiante.setActivo(activo);
        return convertirADTO(estudianteRepository.save(estudiante));
    }

    public EstudianteDTO cambiarConciliacion(Long id, Boolean conciliacion) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado con id: " + id));

        if (conciliacion == null) {
            throw new BusinessException("El estado de conciliación es obligatorio");
        }

        if (Objects.equals(estudiante.getConciliacion(), conciliacion)) {
            throw new BusinessException("El estudiante ya tiene ese estado de conciliación");
        }

        estudiante.setConciliacion(conciliacion);
        return convertirADTO(estudianteRepository.save(estudiante));
    }

    public void eliminar(Long id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado con id: " + id));

        // A futuro conviene validar si el estudiante está asociado a consultas o conciliaciones antes de eliminar.
        estudianteRepository.delete(estudiante);
    }

    private void validarCamposObligatorios(
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

    private void validarDuplicadosCreacion(
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

    private void validarDuplicadosActualizacion(
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

    private TipoDocumento obtenerTipoDocumento(Long tipoDocumentoId) {
        if (tipoDocumentoId == null) {
            throw new BusinessException("El tipo de documento es obligatorio");
        }

        return tipoDocumentoRepository.findById(tipoDocumentoId)
                .orElseThrow(() -> new BusinessException(
                        "Tipo de documento no encontrado con id: " + tipoDocumentoId));
    }

    private Sede obtenerSede(Long sedeId) {
        if (sedeId == null) {
            throw new BusinessException("La sede es obligatoria");
        }

        return sedeRepository.findById(sedeId)
                .orElseThrow(() -> new BusinessException("Sede no encontrada con id: " + sedeId));
    }

    private Asesor obtenerAsesor(Long asesorId) {
        if (asesorId == null) {
            throw new BusinessException("El asesor es obligatorio");
        }

        return asesorRepository.findByIdAndActivoTrue(asesorId)
                .orElseThrow(() -> new BusinessException("Asesor no encontrado o inactivo con id: " + asesorId));
    }

    private EstudianteDTO convertirADTO(Estudiante estudiante) {
        EstudianteDTO dto = new EstudianteDTO();
        dto.setId(estudiante.getId());
        dto.setNombre(estudiante.getNombre());
        dto.setTipoDocumentoId(
                estudiante.getTipoDocumento() != null ? estudiante.getTipoDocumento().getId() : null
        );
        dto.setDocumento(estudiante.getDocumento());
        dto.setEmail(estudiante.getEmail());
        dto.setTelefono(estudiante.getTelefono());
        dto.setUsuario(estudiante.getUsuario());
        dto.setSedeId(
                estudiante.getSede() != null ? estudiante.getSede().getId() : null
        );
        dto.setCodigo(estudiante.getCodigo());
        dto.setAsesorId(
                estudiante.getAsesor() != null ? estudiante.getAsesor().getId() : null
        );
        dto.setActivo(estudiante.getActivo());
        dto.setConciliacion(estudiante.getConciliacion());
        return dto;
    }
}