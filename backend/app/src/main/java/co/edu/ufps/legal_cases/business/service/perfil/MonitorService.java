package co.edu.ufps.legal_cases.business.service.perfil;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.business.dto.perfil.MonitorDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Monitor;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.MonitorRepository;
import co.edu.ufps.legal_cases.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.UsuarioSistemaRegistroService;
import jakarta.transaction.Transactional;

import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarCodigo;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarUsuario;
import static co.edu.ufps.legal_cases.util.ComparacionUtils.equalsIgnoreCase;
import static co.edu.ufps.legal_cases.util.ComparacionUtils.mismoId;

@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;

    public MonitorService(
            MonitorRepository monitorRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            UsuarioSistemaRegistroService usuarioSistemaRegistroService) {
        this.monitorRepository = monitorRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sedeRepository = sedeRepository;
        this.usuarioSistemaRegistroService = usuarioSistemaRegistroService;
    }

    public List<MonitorDTO> listar() {
        return monitorRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<MonitorDTO> listarActivos() {
        return monitorRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public MonitorDTO obtenerPorId(Long id) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Monitor no encontrado con id: " + id));

        return convertirADTO(monitor);
    }

    @Transactional
    public MonitorDTO crear(MonitorDTO dto) {
        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(nombre, email, telefono, usuario, codigo);
        validarDuplicadosCreacion(documento, email, telefono, usuario, codigo);

        // En estos metodos valido que efectivamente existan esas entidades relacionadas
        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Monitor monitor = new Monitor();
        monitor.setNombre(nombre);
        monitor.setTipoDocumento(tipoDocumento);
        monitor.setDocumento(documento);
        monitor.setEmail(email);
        monitor.setTelefono(telefono);
        monitor.setUsuario(usuario);
        monitor.setCodigo(codigo);
        monitor.setSede(sede);
        monitor.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        Monitor monitorGuardado = monitorRepository.save(monitor);

        //Aqui estoy creando el usuario ante el sistema
        // También se conserva temporalmente la relación vieja UsuarioSistema.monitor.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaMonitor(monitorGuardado);

        // Nueva relación normalizada.
        // Ahora el perfil real monitor apunta al usuario del sistema.
        monitorGuardado.setUsuarioSistema(usuarioSistema);

        Monitor monitorActualizado = monitorRepository.save(monitorGuardado);

        return convertirADTO(monitorActualizado);
    }

    public MonitorDTO actualizar(Long id, MonitorDTO dto) {
        Monitor monitorExistente = monitorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Monitor no encontrado con id: " + id));

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(nombre, email, telefono, usuario, codigo);
        validarDuplicadosActualizacion(id, documento, email, telefono, usuario, codigo);

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Boolean nuevoActivo = dto.getActivo() != null ? dto.getActivo() : monitorExistente.getActivo();

        if (dto.getId() != null && !dto.getId().equals(monitorExistente.getId())) {
            throw new BusinessException("No se permite cambiar el id del monitor");
        }

        boolean sinCambios = equalsIgnoreCase(monitorExistente.getNombre(), nombre)
                && mismoId(monitorExistente.getTipoDocumento(), tipoDocumento, TipoDocumento::getId)
                && equalsIgnoreCase(monitorExistente.getDocumento(), documento)
                && equalsIgnoreCase(monitorExistente.getEmail(), email)
                && Objects.equals(monitorExistente.getTelefono(), telefono)
                && equalsIgnoreCase(monitorExistente.getUsuario(), usuario)
                && equalsIgnoreCase(monitorExistente.getCodigo(), codigo)
                && mismoId(monitorExistente.getSede(), sede, Sede::getId)
                && Objects.equals(monitorExistente.getActivo(), nuevoActivo);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        monitorExistente.setNombre(nombre);
        monitorExistente.setTipoDocumento(tipoDocumento);
        monitorExistente.setDocumento(documento);
        monitorExistente.setEmail(email);
        monitorExistente.setTelefono(telefono);
        monitorExistente.setUsuario(usuario);
        monitorExistente.setCodigo(codigo);
        monitorExistente.setSede(sede);
        monitorExistente.setActivo(nuevoActivo);

        return convertirADTO(monitorRepository.save(monitorExistente));
    }

    public MonitorDTO cambiarEstado(Long id, Boolean activo) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Monitor no encontrado con id: " + id));

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(monitor.getActivo(), activo)) {
            throw new BusinessException("El monitor ya tiene ese estado");
        }

        monitor.setActivo(activo);
        return convertirADTO(monitorRepository.save(monitor));
    }

    public void eliminar(Long id) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Monitor no encontrado con id: " + id));

        // A futuro conviene validar si el monitor está asociado a consultas antes de
        // eliminar.
        monitorRepository.delete(monitor);
    }

    private void validarCamposObligatorios(
            String nombre,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (nombre == null) {
            throw new BusinessException("El nombre es obligatorio");
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

        if (documento != null && monitorRepository.existsByDocumento(documento)) {
            throw new BusinessException("Ya existe un monitor con ese documento");
        }

        if (monitorRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe un monitor con ese email");
        }

        if (monitorRepository.existsByTelefono(telefono)) {
            throw new BusinessException("Ya existe un monitor con ese teléfono");
        }

        if (monitorRepository.existsByUsuarioIgnoreCase(usuario)) {
            throw new BusinessException("Ya existe un monitor con ese usuario");
        }

        if (monitorRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("Ya existe un monitor con ese código");
        }
    }

    private void validarDuplicadosActualizacion(
            Long id,
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (documento != null && monitorRepository.existsByDocumentoAndIdNot(documento, id)) {
            throw new BusinessException("Ya existe un monitor con ese documento");
        }

        if (monitorRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException("Ya existe un monitor con ese email");
        }

        if (monitorRepository.existsByTelefonoAndIdNot(telefono, id)) {
            throw new BusinessException("Ya existe un monitor con ese teléfono");
        }

        if (monitorRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario, id)) {
            throw new BusinessException("Ya existe un monitor con ese usuario");
        }

        if (monitorRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new BusinessException("Ya existe un monitor con ese código");
        }
    }

    private TipoDocumento obtenerTipoDocumento(Long tipoDocumentoId) {
        if (tipoDocumentoId == null) {
            return null;
        }

        return tipoDocumentoRepository.findById(tipoDocumentoId)
                .orElseThrow(() -> new BusinessException(
                        "Tipo de documento no encontrado con id: " + tipoDocumentoId));
    }

    private Sede obtenerSede(Long sedeId) {
        if (sedeId == null) {
            return null;
        }

        return sedeRepository.findById(sedeId)
                .orElseThrow(() -> new BusinessException("Sede no encontrada con id: " + sedeId));
    }

    private MonitorDTO convertirADTO(Monitor monitor) {
        MonitorDTO dto = new MonitorDTO();
        dto.setId(monitor.getId());
        dto.setNombre(monitor.getNombre());
        dto.setTipoDocumentoId(
                monitor.getTipoDocumento() != null ? monitor.getTipoDocumento().getId() : null);
        dto.setDocumento(monitor.getDocumento());
        dto.setEmail(monitor.getEmail());
        dto.setTelefono(monitor.getTelefono());
        dto.setUsuario(monitor.getUsuario());
        dto.setCodigo(monitor.getCodigo());
        dto.setSedeId(
                monitor.getSede() != null ? monitor.getSede().getId() : null);
        dto.setActivo(monitor.getActivo());
        return dto;
    }
}