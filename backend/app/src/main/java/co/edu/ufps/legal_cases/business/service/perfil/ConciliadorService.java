package co.edu.ufps.legal_cases.business.service.perfil;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.business.dto.perfil.ConciliadorDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Conciliador;
import co.edu.ufps.legal_cases.business.model.perfil.TipoConciliador;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.ConciliadorRepository;
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
public class ConciliadorService {

    private final ConciliadorRepository conciliadorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;

    public ConciliadorService(
            ConciliadorRepository conciliadorRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            UsuarioSistemaRegistroService usuarioSistemaRegistroService) {
        this.conciliadorRepository = conciliadorRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sedeRepository = sedeRepository;
        this.usuarioSistemaRegistroService = usuarioSistemaRegistroService;
    }

    public List<ConciliadorDTO> listar() {
        return conciliadorRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<ConciliadorDTO> listarActivos() {
        return conciliadorRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public ConciliadorDTO obtenerPorId(Long id) {
        Conciliador conciliador = conciliadorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conciliador no encontrado con id: " + id));

        return convertirADTO(conciliador);
    }

    @Transactional
    public ConciliadorDTO crear(ConciliadorDTO dto) {
        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(dto.getTipoConciliador(), nombre, documento, email, telefono, usuario, codigo);
        validarDuplicadosCreacion(documento, email, telefono, usuario, codigo);

        // En estos metodos valido que efectivamente existan esas entidades relacionadas
        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Conciliador conciliador = new Conciliador();
        conciliador.setNombre(nombre);
        conciliador.setTipoDocumento(tipoDocumento);
        conciliador.setDocumento(documento);
        conciliador.setEmail(email);
        conciliador.setTelefono(telefono);
        conciliador.setUsuario(usuario);
        conciliador.setSede(sede);
        conciliador.setCodigo(codigo);
        conciliador.setTipoConciliador(dto.getTipoConciliador());
        conciliador.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        Conciliador conciliadorGuardado = conciliadorRepository.save(conciliador);

        //Aqui estoy creando el usuario ante el sistema
        // También se conserva temporalmente la relación vieja UsuarioSistema.conciliador.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaConciliador(conciliadorGuardado);

        // Nueva relación normalizada.
        // Ahora el perfil real conciliador apunta al usuario del sistema.
        conciliadorGuardado.setUsuarioSistema(usuarioSistema);

        Conciliador conciliadorActualizado = conciliadorRepository.save(conciliadorGuardado);

        return convertirADTO(conciliadorActualizado);
    }

    public ConciliadorDTO actualizar(Long id, ConciliadorDTO dto) {
        Conciliador existente = conciliadorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conciliador no encontrado con id: " + id));

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(dto.getTipoConciliador(), nombre, documento, email, telefono, usuario, codigo);
        validarDuplicadosActualizacion(id, documento, email, telefono, usuario, codigo);

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Boolean nuevoActivo = dto.getActivo() != null ? dto.getActivo() : existente.getActivo();

        if (dto.getId() != null && !dto.getId().equals(existente.getId())) {
            throw new BusinessException("No se permite cambiar el id del conciliador");
        }

        boolean sinCambios = equalsIgnoreCase(existente.getNombre(), nombre)
                && mismoId(existente.getTipoDocumento(), tipoDocumento, TipoDocumento::getId)
                && Objects.equals(existente.getDocumento(), documento)
                && equalsIgnoreCase(existente.getEmail(), email)
                && Objects.equals(existente.getTelefono(), telefono)
                && equalsIgnoreCase(existente.getUsuario(), usuario)
                && mismoId(existente.getSede(), sede, Sede::getId)
                && equalsIgnoreCase(existente.getCodigo(), codigo)
                && Objects.equals(existente.getTipoConciliador(), dto.getTipoConciliador())
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
        existente.setTipoConciliador(dto.getTipoConciliador());
        existente.setActivo(nuevoActivo);

        return convertirADTO(conciliadorRepository.save(existente));
    }

    public ConciliadorDTO cambiarEstado(Long id, Boolean activo) {
        Conciliador conciliador = conciliadorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conciliador no encontrado con id: " + id));

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(conciliador.getActivo(), activo)) {
            throw new BusinessException("El conciliador ya tiene ese estado");
        }

        conciliador.setActivo(activo);
        return convertirADTO(conciliadorRepository.save(conciliador));
    }

    public void eliminar(Long id) {
        Conciliador conciliador = conciliadorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conciliador no encontrado con id: " + id));

        // A futuro conviene validar aquí si tiene conciliaciones asociadas antes de
        // eliminar.
        conciliadorRepository.delete(conciliador);
    }

    private void validarCamposObligatorios(
            TipoConciliador tipoConciliador,
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

        if (tipoConciliador == null) {
            throw new BusinessException("El tipo de conciliador es obligatorio");
        }
    }

    private void validarDuplicadosCreacion(
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (conciliadorRepository.existsByDocumento(documento)) {
            throw new BusinessException("Ya existe un conciliador con ese documento");
        }

        if (conciliadorRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe un conciliador con ese email");
        }

        if (conciliadorRepository.existsByTelefono(telefono)) {
            throw new BusinessException("Ya existe un conciliador con ese teléfono");
        }

        if (conciliadorRepository.existsByUsuarioIgnoreCase(usuario)) {
            throw new BusinessException("Ya existe un conciliador con ese usuario");
        }

        if (conciliadorRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("Ya existe un conciliador con ese código");
        }
    }

    private void validarDuplicadosActualizacion(
            Long id,
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (conciliadorRepository.existsByDocumentoAndIdNot(documento, id)) {
            throw new BusinessException("Ya existe un conciliador con ese documento");
        }

        if (conciliadorRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException("Ya existe un conciliador con ese email");
        }

        if (conciliadorRepository.existsByTelefonoAndIdNot(telefono, id)) {
            throw new BusinessException("Ya existe un conciliador con ese teléfono");
        }

        if (conciliadorRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario, id)) {
            throw new BusinessException("Ya existe un conciliador con ese usuario");
        }

        if (conciliadorRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new BusinessException("Ya existe un conciliador con ese código");
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

    private ConciliadorDTO convertirADTO(Conciliador conciliador) {
        ConciliadorDTO dto = new ConciliadorDTO();
        dto.setId(conciliador.getId());
        dto.setNombre(conciliador.getNombre());
        dto.setTipoDocumentoId(
                conciliador.getTipoDocumento() != null ? conciliador.getTipoDocumento().getId() : null);
        dto.setDocumento(conciliador.getDocumento());
        dto.setEmail(conciliador.getEmail());
        dto.setTelefono(conciliador.getTelefono());
        dto.setUsuario(conciliador.getUsuario());
        dto.setSedeId(
                conciliador.getSede() != null ? conciliador.getSede().getId() : null);
        dto.setCodigo(conciliador.getCodigo());
        dto.setTipoConciliador(conciliador.getTipoConciliador());
        dto.setActivo(conciliador.getActivo());
        return dto;
    }
}