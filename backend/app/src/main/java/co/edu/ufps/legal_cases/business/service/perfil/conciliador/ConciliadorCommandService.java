package co.edu.ufps.legal_cases.business.service.perfil.conciliador;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarCodigo;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarUsuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.perfil.ConciliadorDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Conciliador;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.ConciliadorRepository;
import co.edu.ufps.legal_cases.business.service.acceso.perfil.ConciliadorAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaRegistroService;

@Service
public class ConciliadorCommandService {

    private final ConciliadorRepository conciliadorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;
    private final ConciliadorAccessService conciliadorAccessService;
    private final ConciliadorValidator conciliadorValidator;
    private final ConciliadorMapper conciliadorMapper;

    public ConciliadorCommandService(
            ConciliadorRepository conciliadorRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            UsuarioSistemaRegistroService usuarioSistemaRegistroService,
            ConciliadorAccessService conciliadorAccessService,
            ConciliadorValidator conciliadorValidator,
            ConciliadorMapper conciliadorMapper) {
        this.conciliadorRepository = conciliadorRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sedeRepository = sedeRepository;
        this.usuarioSistemaRegistroService = usuarioSistemaRegistroService;
        this.conciliadorAccessService = conciliadorAccessService;
        this.conciliadorValidator = conciliadorValidator;
        this.conciliadorMapper = conciliadorMapper;
    }

    @Transactional
    public ConciliadorDTO crear(ConciliadorDTO dto) {
        conciliadorAccessService.validarPuedeGestionarConciliadores();
        conciliadorValidator.validarIdNoEnviadoEnCreacion(dto.getId());

        DatosConciliador datos = prepararDatos(dto);

        conciliadorValidator.validarCamposObligatorios(datos);
        conciliadorValidator.validarDuplicadosCreacion(
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        Conciliador conciliador = new Conciliador();
        conciliadorMapper.aplicarDatos(conciliador, datos);

        Conciliador conciliadorGuardado = conciliadorRepository.save(conciliador);

        // El perfil se guarda primero porque el usuario del sistema se crea con datos del conciliador persistido.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaConciliador(conciliadorGuardado);

        // El perfil real queda apuntando al usuario del sistema normalizado.
        conciliadorGuardado.setUsuarioSistema(usuarioSistema);

        return conciliadorMapper.convertirADTO(conciliadorRepository.save(conciliadorGuardado));
    }

    @Transactional
    public ConciliadorDTO actualizar(Long id, ConciliadorDTO dto) {
        conciliadorAccessService.validarPuedeGestionarConciliadores();

        Conciliador existente = buscarPorId(id);

        conciliadorValidator.validarIdNoCambiado(id, dto.getId());

        DatosConciliador datos = prepararDatos(dto);

        conciliadorValidator.validarCamposObligatorios(datos);
        conciliadorValidator.validarDuplicadosActualizacion(
                id,
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        conciliadorValidator.validarExistenCambios(existente, datos);

        conciliadorMapper.aplicarDatos(existente, datos);

        return conciliadorMapper.convertirADTO(conciliadorRepository.save(existente));
    }

    @Transactional
    public ConciliadorDTO cambiarEstado(Long id, Boolean activo) {
        conciliadorAccessService.validarPuedeGestionarConciliadores();

        Conciliador conciliador = buscarPorId(id);

        conciliadorValidator.validarCambioEstado(conciliador, activo);

        conciliador.setActivo(activo);

        return conciliadorMapper.convertirADTO(conciliadorRepository.save(conciliador));
    }

    @Transactional
    public void eliminar(Long id) {
        conciliadorAccessService.validarPuedeGestionarConciliadores();

        Conciliador conciliador = buscarPorId(id);

        // Se conserva el comportamiento actual: eliminación física.
        // En la fase de estandarización técnica revisamos si debe pasar a desactivación lógica.
        conciliadorRepository.delete(conciliador);
    }

    private DatosConciliador prepararDatos(ConciliadorDTO dto) {
        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Boolean activo = dto.getActivo() != null ? dto.getActivo() : true;

        return new DatosConciliador(
                nombre,
                documento,
                email,
                telefono,
                usuario,
                codigo,
                tipoDocumento,
                sede,
                dto.getTipoConciliador(),
                activo);
    }

    private Conciliador buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del conciliador es obligatorio");
        }

        return conciliadorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conciliador no encontrado con id: " + id));
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
}