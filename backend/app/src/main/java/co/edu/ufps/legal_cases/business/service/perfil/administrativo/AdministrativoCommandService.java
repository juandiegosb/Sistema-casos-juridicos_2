package co.edu.ufps.legal_cases.business.service.perfil.administrativo;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarCodigo;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarUsuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.perfil.AdministrativoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Administrativo;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.AdministrativoRepository;
import co.edu.ufps.legal_cases.business.service.acceso.perfil.AdministrativoAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaRegistroService;

@Service
public class AdministrativoCommandService {

    private final AdministrativoRepository administrativoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;
    private final AdministrativoAccessService administrativoAccessService;
    private final AdministrativoValidator administrativoValidator;
    private final AdministrativoMapper administrativoMapper;

    public AdministrativoCommandService(
            AdministrativoRepository administrativoRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            UsuarioSistemaRegistroService usuarioSistemaRegistroService,
            AdministrativoAccessService administrativoAccessService,
            AdministrativoValidator administrativoValidator,
            AdministrativoMapper administrativoMapper) {
        this.administrativoRepository = administrativoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sedeRepository = sedeRepository;
        this.usuarioSistemaRegistroService = usuarioSistemaRegistroService;
        this.administrativoAccessService = administrativoAccessService;
        this.administrativoValidator = administrativoValidator;
        this.administrativoMapper = administrativoMapper;
    }

    @Transactional
    public AdministrativoDTO crear(AdministrativoDTO dto) {
        administrativoAccessService.validarPuedeGestionarAdministradores();
        administrativoValidator.validarIdNoEnviadoEnCreacion(dto.getId());

        DatosAdministrativo datos = prepararDatos(dto);

        administrativoValidator.validarCamposObligatorios(datos);
        administrativoValidator.validarDuplicadosCreacion(
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        Administrativo administrativo = new Administrativo();
        administrativoMapper.aplicarDatos(administrativo, datos);

        Administrativo administrativoGuardado = administrativoRepository.save(administrativo);

        // El perfil se guarda primero porque el usuario del sistema se crea con datos del administrativo persistido.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaAdministrativo(administrativoGuardado);

        // El perfil real queda apuntando al usuario del sistema normalizado.
        administrativoGuardado.setUsuarioSistema(usuarioSistema);

        return administrativoMapper.convertirADTO(administrativoRepository.save(administrativoGuardado));
    }

    @Transactional
    public AdministrativoDTO actualizar(Long id, AdministrativoDTO dto) {
        administrativoAccessService.validarPuedeGestionarAdministradores();

        Administrativo existente = buscarPorId(id);

        administrativoValidator.validarIdNoCambiado(id, dto.getId());

        DatosAdministrativo datos = prepararDatos(dto);

        administrativoValidator.validarCamposObligatorios(datos);
        administrativoValidator.validarDuplicadosActualizacion(
                id,
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        administrativoValidator.validarExistenCambios(existente, datos);

        administrativoMapper.aplicarDatos(existente, datos);

        return administrativoMapper.convertirADTO(administrativoRepository.save(existente));
    }

    @Transactional
    public AdministrativoDTO cambiarEstado(Long id, Boolean activo) {
        administrativoAccessService.validarPuedeGestionarAdministradores();

        Administrativo administrativo = buscarPorId(id);

        administrativoValidator.validarCambioEstado(administrativo, activo);

        administrativo.setActivo(activo);

        return administrativoMapper.convertirADTO(administrativoRepository.save(administrativo));
    }

    @Transactional
    public AdministrativoDTO cambiarDirectora(Long id, Boolean directora) {
        administrativoAccessService.validarPuedeGestionarAdministradores();

        Administrativo administrativo = buscarPorId(id);

        administrativoValidator.validarCambioDirectora(administrativo, directora);

        administrativo.setDirectora(directora);

        return administrativoMapper.convertirADTO(administrativoRepository.save(administrativo));
    }

    @Transactional
    public void eliminar(Long id) {
        administrativoAccessService.validarPuedeGestionarAdministradores();

        Administrativo administrativo = buscarPorId(id);

        // Se conserva el comportamiento actual: eliminación física.
        // En la fase de estandarización técnica revisamos si debe pasar a desactivación lógica.
        administrativoRepository.delete(administrativo);
    }

    private DatosAdministrativo prepararDatos(AdministrativoDTO dto) {
        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarDocumentoOpcional(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Boolean activo = dto.getActivo() != null ? dto.getActivo() : true;
        Boolean directora = dto.getDirectora() != null ? dto.getDirectora() : false;

        return new DatosAdministrativo(
                nombre,
                documento,
                email,
                telefono,
                usuario,
                codigo,
                tipoDocumento,
                sede,
                activo,
                directora);
    }

    private Administrativo buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del administrativo es obligatorio");
        }

        return administrativoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Administrativo no encontrado con id: " + id));
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

    private String normalizarDocumentoOpcional(String valor) {
        String documento = normalizarNumeroDocumento(valor);

        // En administrativos el documento es opcional; vacío se guarda como null.
        if (documento == null || documento.isBlank()) {
            return null;
        }

        return documento;
    }
}