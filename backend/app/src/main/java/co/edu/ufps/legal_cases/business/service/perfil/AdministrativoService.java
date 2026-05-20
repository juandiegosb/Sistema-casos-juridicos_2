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

import co.edu.ufps.legal_cases.business.dto.perfil.AdministrativoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Administrativo;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.AdministrativoRepository;
import co.edu.ufps.legal_cases.business.service.acceso.AdministrativoAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaRegistroService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdministrativoService {

    private final AdministrativoRepository administrativoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;
    private final AdministrativoAccessService administrativoAccessService;

    public List<AdministrativoDTO> listar() {
        administrativoAccessService.validarPuedeVerAdministradores();

        return administrativoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<AdministrativoDTO> listarActivos() {
        administrativoAccessService.validarPuedeVerAdministradores();

        return administrativoRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<AdministrativoDTO> listarDirectoras() {
        administrativoAccessService.validarPuedeVerAdministradores();

        return administrativoRepository.findByDirectoraTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public AdministrativoDTO obtenerPorId(Long id) {
        administrativoAccessService.validarPuedeVerAdministradores();

        Administrativo administrativo = administrativoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Administrativo no encontrado con id: " + id));

        return convertirADTO(administrativo);
    }

    @Transactional
    public AdministrativoDTO crear(AdministrativoDTO dto) {
        // Solo la directora puede crear otros administrativos.
        administrativoAccessService.validarPuedeGestionarAdministradores();

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarDocumentoOpcional(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(nombre, email, telefono, usuario, codigo);
        validarDuplicadosCreacion(documento, email, telefono, usuario, codigo);

        // En estos metodos valido que efectivamente existan esas entidades relacionadas
        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Administrativo administrativo = new Administrativo();
        administrativo.setNombre(nombre);
        administrativo.setTipoDocumento(tipoDocumento);
        administrativo.setDocumento(documento);
        administrativo.setEmail(email);
        administrativo.setTelefono(telefono);
        administrativo.setUsuario(usuario);
        administrativo.setCodigo(codigo);
        administrativo.setSede(sede);
        administrativo.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        administrativo.setDirectora(dto.getDirectora() != null ? dto.getDirectora() : false);

        Administrativo administrativoGuardado = administrativoRepository.save(administrativo);

        //Aqui estoy creando el usuario ante el sistema
        // También se conserva temporalmente la relación vieja UsuarioSistema.administrativo.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaAdministrativo(administrativoGuardado);

        // Nueva relación normalizada.
        // Ahora el perfil real administrativo apunta al usuario del sistema.
        administrativoGuardado.setUsuarioSistema(usuarioSistema);

        Administrativo administrativoActualizado = administrativoRepository.save(administrativoGuardado);

        return convertirADTO(administrativoActualizado);
    }

    @Transactional
    public AdministrativoDTO actualizar(Long id, AdministrativoDTO dto) {
        // Solo la directora puede modificar administrativos.
        administrativoAccessService.validarPuedeGestionarAdministradores();

        Administrativo existente = administrativoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Administrativo no encontrado con id: " + id));

        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarDocumentoOpcional(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        validarCamposObligatorios(nombre, email, telefono, usuario, codigo);
        validarDuplicadosActualizacion(id, documento, email, telefono, usuario, codigo);

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Boolean nuevoActivo = dto.getActivo() != null ? dto.getActivo() : existente.getActivo();
        Boolean nuevaDirectora = dto.getDirectora() != null ? dto.getDirectora() : existente.getDirectora();

        if (dto.getId() != null && !dto.getId().equals(existente.getId())) {
            throw new BusinessException("No se permite cambiar el id del administrativo");
        }

        boolean sinCambios = equalsIgnoreCase(existente.getNombre(), nombre)
                && mismoId(existente.getTipoDocumento(), tipoDocumento, TipoDocumento::getId)
                && equalsIgnoreCase(existente.getDocumento(), documento)
                && equalsIgnoreCase(existente.getEmail(), email)
                && Objects.equals(existente.getTelefono(), telefono)
                && equalsIgnoreCase(existente.getUsuario(), usuario)
                && equalsIgnoreCase(existente.getCodigo(), codigo)
                && mismoId(existente.getSede(), sede, Sede::getId)
                && Objects.equals(existente.getActivo(), nuevoActivo)
                && Objects.equals(existente.getDirectora(), nuevaDirectora);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        existente.setNombre(nombre);
        existente.setTipoDocumento(tipoDocumento);
        existente.setDocumento(documento);
        existente.setEmail(email);
        existente.setTelefono(telefono);
        existente.setUsuario(usuario);
        existente.setCodigo(codigo);
        existente.setSede(sede);
        existente.setActivo(nuevoActivo);
        existente.setDirectora(nuevaDirectora);

        return convertirADTO(administrativoRepository.save(existente));
    }

    @Transactional
    public AdministrativoDTO cambiarEstado(Long id, Boolean activo) {
        // Solo la directora puede activar o desactivar administrativos.
        administrativoAccessService.validarPuedeGestionarAdministradores();

        Administrativo administrativo = administrativoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Administrativo no encontrado con id: " + id));

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(administrativo.getActivo(), activo)) {
            throw new BusinessException("El administrativo ya tiene ese estado");
        }

        administrativo.setActivo(activo);
        return convertirADTO(administrativoRepository.save(administrativo));
    }

    @Transactional
    public AdministrativoDTO cambiarDirectora(Long id, Boolean directora) {
        // Solo la directora actual puede cambiar el estado de directora.
        administrativoAccessService.validarPuedeGestionarAdministradores();

        Administrativo administrativo = administrativoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Administrativo no encontrado con id: " + id));

        if (directora == null) {
            throw new BusinessException("El estado de directora es obligatorio");
        }

        if (Objects.equals(administrativo.getDirectora(), directora)) {
            throw new BusinessException("El administrativo ya tiene ese estado de directora");
        }

        administrativo.setDirectora(directora);
        return convertirADTO(administrativoRepository.save(administrativo));
    }

    @Transactional
    public void eliminar(Long id) {
        // Solo la directora puede eliminar administrativos.
        administrativoAccessService.validarPuedeGestionarAdministradores();

        Administrativo administrativo = administrativoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Administrativo no encontrado con id: " + id));

        administrativoRepository.delete(administrativo);
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

        if (documento != null && administrativoRepository.existsByDocumento(documento)) {
            throw new BusinessException("Ya existe un administrativo con ese documento");
        }

        if (administrativoRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe un administrativo con ese email");
        }

        if (administrativoRepository.existsByTelefono(telefono)) {
            throw new BusinessException("Ya existe un administrativo con ese teléfono");
        }

        if (administrativoRepository.existsByUsuarioIgnoreCase(usuario)) {
            throw new BusinessException("Ya existe un administrativo con ese usuario");
        }

        if (administrativoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("Ya existe un administrativo con ese código");
        }
    }

    private void validarDuplicadosActualizacion(
            Long id,
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo) {

        if (documento != null && administrativoRepository.existsByDocumentoAndIdNot(documento, id)) {
            throw new BusinessException("Ya existe un administrativo con ese documento");
        }

        if (administrativoRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException("Ya existe un administrativo con ese email");
        }

        if (administrativoRepository.existsByTelefonoAndIdNot(telefono, id)) {
            throw new BusinessException("Ya existe un administrativo con ese teléfono");
        }

        if (administrativoRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario, id)) {
            throw new BusinessException("Ya existe un administrativo con ese usuario");
        }

        if (administrativoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new BusinessException("Ya existe un administrativo con ese código");
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

    private AdministrativoDTO convertirADTO(Administrativo administrativo) {
        AdministrativoDTO dto = new AdministrativoDTO();
        dto.setId(administrativo.getId());
        dto.setNombre(administrativo.getNombre());
        dto.setTipoDocumentoId(
                administrativo.getTipoDocumento() != null ? administrativo.getTipoDocumento().getId() : null);
        dto.setDocumento(administrativo.getDocumento());
        dto.setEmail(administrativo.getEmail());
        dto.setTelefono(administrativo.getTelefono());
        dto.setUsuario(administrativo.getUsuario());
        dto.setCodigo(administrativo.getCodigo());
        dto.setSedeId(
                administrativo.getSede() != null ? administrativo.getSede().getId() : null);
        dto.setActivo(administrativo.getActivo());
        dto.setDirectora(administrativo.getDirectora());
        return dto;
    }

    // Como el numero de documento es opcional aparte de normalizar con la utilidad
    // se agrega esta funcion para convertir " " en null
    private String normalizarDocumentoOpcional(String valor) {
        String documento = normalizarNumeroDocumento(valor);

        if (documento == null || documento.isBlank()) {
            return null;
        }

        return documento;
    }
}