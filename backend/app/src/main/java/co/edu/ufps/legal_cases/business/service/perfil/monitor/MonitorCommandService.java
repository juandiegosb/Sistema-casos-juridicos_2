package co.edu.ufps.legal_cases.business.service.perfil.monitor;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarCodigo;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarUsuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.perfil.MonitorDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Monitor;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.MonitorRepository;
import co.edu.ufps.legal_cases.business.service.acceso.perfil.AsesorMonitorAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaRegistroService;

@Service
public class MonitorCommandService {

    private final MonitorRepository monitorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;
    private final AsesorMonitorAccessService asesorMonitorAccessService;
    private final MonitorValidator monitorValidator;
    private final MonitorMapper monitorMapper;

    public MonitorCommandService(
            MonitorRepository monitorRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            SedeRepository sedeRepository,
            UsuarioSistemaRegistroService usuarioSistemaRegistroService,
            AsesorMonitorAccessService asesorMonitorAccessService,
            MonitorValidator monitorValidator,
            MonitorMapper monitorMapper) {
        this.monitorRepository = monitorRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sedeRepository = sedeRepository;
        this.usuarioSistemaRegistroService = usuarioSistemaRegistroService;
        this.asesorMonitorAccessService = asesorMonitorAccessService;
        this.monitorValidator = monitorValidator;
        this.monitorMapper = monitorMapper;
    }

    @Transactional
    public MonitorDTO crear(MonitorDTO dto) {
        asesorMonitorAccessService.validarPuedeGestionarAsesoresYMonitores();
        monitorValidator.validarIdNoEnviadoEnCreacion(dto.getId());

        DatosMonitor datos = prepararDatos(dto);

        monitorValidator.validarDuplicadosCreacion(
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        Monitor monitor = new Monitor();
        monitorMapper.aplicarDatos(monitor, datos);

        Monitor monitorGuardado = monitorRepository.save(monitor);

        // El perfil se guarda primero porque el usuario del sistema se crea con datos del monitor persistido.
        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaMonitor(monitorGuardado);

        // El perfil real queda apuntando al usuario del sistema normalizado.
        monitorGuardado.setUsuarioSistema(usuarioSistema);

        return monitorMapper.convertirADTO(monitorRepository.save(monitorGuardado));
    }

    @Transactional
    public MonitorDTO actualizar(Long id, MonitorDTO dto) {
        asesorMonitorAccessService.validarPuedeGestionarAsesoresYMonitores();

        Monitor existente = buscarPorId(id);

        monitorValidator.validarIdNoCambiado(id, dto.getId());

        DatosMonitor datos = prepararDatos(dto);

        monitorValidator.validarDuplicadosActualizacion(
                id,
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        monitorValidator.validarExistenCambios(existente, datos);

        monitorMapper.aplicarDatos(existente, datos);

        return monitorMapper.convertirADTO(monitorRepository.save(existente));
    }

    @Transactional
    public MonitorDTO cambiarEstado(Long id, Boolean activo) {
        asesorMonitorAccessService.validarPuedeGestionarAsesoresYMonitores();

        Monitor monitor = buscarPorId(id);

        monitorValidator.validarCambioEstado(monitor, activo);

        monitor.setActivo(activo);

        return monitorMapper.convertirADTO(monitorRepository.save(monitor));
    }

    @Transactional
    public void eliminar(Long id) {
        asesorMonitorAccessService.validarPuedeGestionarAsesoresYMonitores();

        Monitor monitor = buscarPorId(id);

        // Se conserva el comportamiento actual: eliminación física.
        // En la fase de estandarización técnica revisamos si debe pasar a desactivación lógica.
        monitorRepository.delete(monitor);
    }

    private DatosMonitor prepararDatos(MonitorDTO dto) {
        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        monitorValidator.validarCamposObligatorios(
                nombre,
                email,
                telefono,
                usuario,
                codigo);

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());

        Boolean activo = dto.getActivo() != null ? dto.getActivo() : true;

        return new DatosMonitor(
                nombre,
                documento,
                email,
                telefono,
                usuario,
                codigo,
                tipoDocumento,
                sede,
                activo);
    }

    private Monitor buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del monitor es obligatorio");
        }

        return monitorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Monitor no encontrado con id: " + id));
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