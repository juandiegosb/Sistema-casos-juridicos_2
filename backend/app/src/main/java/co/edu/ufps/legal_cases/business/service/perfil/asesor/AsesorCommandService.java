package co.edu.ufps.legal_cases.business.service.perfil.asesor;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarCodigo;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarUsuario;

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
import co.edu.ufps.legal_cases.business.service.acceso.perfil.AsesorMonitorAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaRegistroService;

@Service
public class AsesorCommandService {

    private final AsesorRepository asesorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final AreaRepository areaRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;
    private final AsesorMonitorAccessService asesorMonitorAccessService;
    private final AsesorValidator asesorValidator;
    private final AsesorMapper asesorMapper;

    public AsesorCommandService(
            AsesorRepository asesorRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            AreaRepository areaRepository,
            UsuarioSistemaRegistroService usuarioSistemaRegistroService,
            AsesorMonitorAccessService asesorMonitorAccessService,
            AsesorValidator asesorValidator,
            AsesorMapper asesorMapper) {
        this.asesorRepository = asesorRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sedeRepository = sedeRepository;
        this.areaRepository = areaRepository;
        this.usuarioSistemaRegistroService = usuarioSistemaRegistroService;
        this.asesorMonitorAccessService = asesorMonitorAccessService;
        this.asesorValidator = asesorValidator;
        this.asesorMapper = asesorMapper;
    }

    @Transactional
    public AsesorDTO crear(AsesorDTO dto) {
        asesorMonitorAccessService.validarPuedeGestionarAsesoresYMonitores();
        asesorValidator.validarIdNoEnviadoEnCreacion(dto.getId());

        DatosAsesor datos = prepararDatos(dto);

        asesorValidator.validarDuplicadosCreacion(
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        Asesor asesor = new Asesor();
        asesorMapper.aplicarDatos(asesor, datos);

        Asesor asesorGuardado = asesorRepository.save(asesor);

        // El perfil se guarda primero porque el usuario del sistema se crea con datos del asesor persistido.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaAsesor(asesorGuardado);

        // El perfil real queda apuntando al usuario del sistema normalizado.
        asesorGuardado.setUsuarioSistema(usuarioSistema);

        return asesorMapper.convertirADTO(asesorRepository.save(asesorGuardado));
    }

    @Transactional
    public AsesorDTO actualizar(Long id, AsesorDTO dto) {
        asesorMonitorAccessService.validarPuedeGestionarAsesoresYMonitores();

        Asesor existente = buscarPorId(id);

        asesorValidator.validarIdNoCambiado(id, dto.getId());

        DatosAsesor datos = prepararDatos(dto);

        asesorValidator.validarDuplicadosActualizacion(
                id,
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        asesorValidator.validarExistenCambios(existente, datos);

        asesorMapper.aplicarDatos(existente, datos);

        return asesorMapper.convertirADTO(asesorRepository.save(existente));
    }

    @Transactional
    public AsesorDTO cambiarEstado(Long id, Boolean activo) {
        asesorMonitorAccessService.validarPuedeGestionarAsesoresYMonitores();

        Asesor asesor = buscarPorId(id);

        asesorValidator.validarCambioEstado(asesor, activo);

        asesor.setActivo(activo);

        return asesorMapper.convertirADTO(asesorRepository.save(asesor));
    }

    @Transactional
    public void eliminar(Long id) {
        asesorMonitorAccessService.validarPuedeGestionarAsesoresYMonitores();

        Asesor asesor = buscarPorId(id);

        // Se conserva el comportamiento actual: eliminación física.
        // En la fase de estandarización técnica revisamos si debe pasar a desactivación lógica.
        asesorRepository.delete(asesor);
    }

    private DatosAsesor prepararDatos(AsesorDTO dto) {
        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        asesorValidator.validarCamposObligatorios(
                nombre,
                documento,
                email,
                telefono,
                usuario,
                codigo);

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());
        Area area = obtenerArea(dto.getAreaId());

        Boolean activo = dto.getActivo() != null ? dto.getActivo() : true;

        return new DatosAsesor(
                nombre,
                documento,
                email,
                telefono,
                usuario,
                codigo,
                tipoDocumento,
                sede,
                area,
                activo);
    }

    private Asesor buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del asesor es obligatorio");
        }

        return asesorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Asesor no encontrado con id: " + id));
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
}