package co.edu.ufps.legal_cases.business.service.perfil;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.perfil.AsesorDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Area;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Asesor;
import co.edu.ufps.legal_cases.business.repository.catalogo.AreaRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.AsesorRepository;
import co.edu.ufps.legal_cases.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaRegistroService;

import static co.edu.ufps.legal_cases.util.ComparacionUtils.equalsIgnoreCase;
import static co.edu.ufps.legal_cases.util.ComparacionUtils.mismoId;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarCodigo;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarUsuario;

@Service
public class AsesorService {

    private final AsesorRepository asesorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final AreaRepository areaRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;

    public AsesorService(
            AsesorRepository asesorRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            AreaRepository areaRepository,
            UsuarioSistemaRegistroService usuarioSistemaRegistroService) {
        this.asesorRepository = asesorRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sedeRepository = sedeRepository;
        this.areaRepository = areaRepository;
        this.usuarioSistemaRegistroService = usuarioSistemaRegistroService;
    }

    public List<AsesorDTO> listar() {
        return asesorRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<AsesorDTO> listarActivos() {
        return asesorRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public AsesorDTO obtenerPorId(Long id) {
        Asesor asesor = asesorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Asesor no encontrado con id: " + id));

        return convertirADTO(asesor);
    }

    @Transactional
    public AsesorDTO crear(AsesorDTO dto) {
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

        // En estos metodos valido que efectivamente existan esas entidades relacionadas
        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());
        Area area = obtenerArea(dto.getAreaId());

        Asesor asesor = new Asesor();
        asesor.setNombre(nombre);
        asesor.setTipoDocumento(tipoDocumento);
        asesor.setDocumento(documento);
        asesor.setEmail(email);
        asesor.setTelefono(telefono);
        asesor.setUsuario(usuario);
        asesor.setSede(sede);
        asesor.setCodigo(codigo);
        asesor.setArea(area);
        asesor.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        Asesor asesorGuardado = asesorRepository.save(asesor);

        //Aqui estoy creando el usuario ante el sistema
        // También se conserva temporalmente la relación vieja UsuarioSistema.asesor.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaAsesor(asesorGuardado);

        // Nueva relación normalizada.
        // Ahora el perfil real asesor apunta al usuario del sistema.
        asesorGuardado.setUsuarioSistema(usuarioSistema);

        Asesor asesorActualizado = asesorRepository.save(asesorGuardado);

        return convertirADTO(asesorActualizado);
    }

    public AsesorDTO actualizar(Long id, AsesorDTO dto) {
        Asesor existente = asesorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Asesor no encontrado con id: " + id));

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(nombre, documento, email, telefono, usuario, codigo);
        validarDuplicadosActualizacion(id, documento, email, telefono, usuario, codigo);

        // En estos metodos valido que efectivamente existan esas entidades relacionadas
        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());
        Area area = obtenerArea(dto.getAreaId());

        Boolean nuevoActivo = dto.getActivo() != null ? dto.getActivo() : existente.getActivo();

        if (dto.getId() != null && !dto.getId().equals(existente.getId())) {
            throw new BusinessException("No se permite cambiar el id del asesor");
        }

        boolean sinCambios = equalsIgnoreCase(existente.getNombre(), nombre)
                && mismoId(existente.getTipoDocumento(), tipoDocumento, TipoDocumento::getId)
                && Objects.equals(existente.getDocumento(), documento)
                && equalsIgnoreCase(existente.getEmail(), email)
                && Objects.equals(existente.getTelefono(), telefono)
                && equalsIgnoreCase(existente.getUsuario(), usuario)
                && mismoId(existente.getSede(), sede, Sede::getId)
                && equalsIgnoreCase(existente.getCodigo(), codigo)
                && mismoId(existente.getArea(), area, Area::getId)
                && Objects.equals(existente.getActivo(), nuevoActivo);

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
        existente.setArea(area);
        existente.setActivo(nuevoActivo);

        return convertirADTO(asesorRepository.save(existente));
    }

    public AsesorDTO cambiarEstado(Long id, Boolean activo) {
        Asesor asesor = asesorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Asesor no encontrado con id: " + id));

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(asesor.getActivo(), activo)) {
            throw new BusinessException("El asesor ya tiene ese estado");
        }

        asesor.setActivo(activo);
        return convertirADTO(asesorRepository.save(asesor));
    }

    public void eliminar(Long id) {
        Asesor asesor = asesorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Asesor no encontrado con id: " + id));

        // A futuro conviene validar si el asesor está asociado a estudiantes o
        // consultas antes de eliminar.
        asesorRepository.delete(asesor);
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

        if (asesorRepository.existsByDocumento(documento)) {
            throw new BusinessException("Ya existe un asesor con ese documento");
        }

        if (asesorRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe un asesor con ese email");
        }

        if (asesorRepository.existsByTelefono(telefono)) {
            throw new BusinessException("Ya existe un asesor con ese teléfono");
        }

        if (asesorRepository.existsByUsuarioIgnoreCase(usuario)) {
            throw new BusinessException("Ya existe un asesor con ese usuario");
        }

        if (asesorRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("Ya existe un asesor con ese código");
        }
    }

    private void validarDuplicadosActualizacion(
            Long id,
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (asesorRepository.existsByDocumentoAndIdNot(documento, id)) {
            throw new BusinessException("Ya existe un asesor con ese documento");
        }

        if (asesorRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException("Ya existe un asesor con ese email");
        }

        if (asesorRepository.existsByTelefonoAndIdNot(telefono, id)) {
            throw new BusinessException("Ya existe un asesor con ese teléfono");
        }

        if (asesorRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario, id)) {
            throw new BusinessException("Ya existe un asesor con ese usuario");
        }

        if (asesorRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new BusinessException("Ya existe un asesor con ese código");
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

    private Area obtenerArea(Long areaId) {
        if (areaId == null) {
            throw new BusinessException("El área es obligatoria");
        }

        return areaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException("Área no encontrada con id: " + areaId));
    }

    private AsesorDTO convertirADTO(Asesor asesor) {
        AsesorDTO dto = new AsesorDTO();
        dto.setId(asesor.getId());
        dto.setNombre(asesor.getNombre());
        dto.setTipoDocumentoId(
                asesor.getTipoDocumento() != null ? asesor.getTipoDocumento().getId() : null);
        dto.setDocumento(asesor.getDocumento());
        dto.setEmail(asesor.getEmail());
        dto.setTelefono(asesor.getTelefono());
        dto.setUsuario(asesor.getUsuario());
        dto.setSedeId(
                asesor.getSede() != null ? asesor.getSede().getId() : null);
        dto.setCodigo(asesor.getCodigo());
        dto.setAreaId(
                asesor.getArea() != null ? asesor.getArea().getId() : null);
        dto.setActivo(asesor.getActivo());
        return dto;
    }
}