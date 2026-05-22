package co.edu.ufps.legal_cases.business.service.seguimiento.seguimiento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoRequestDTO;
import co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoResponseDTO;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.seguimiento.CategoriaSeguimiento;
import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.CategoriaSeguimientoRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoRepository;
import co.edu.ufps.legal_cases.business.service.acceso.SeguimientoAccessService;
import co.edu.ufps.legal_cases.business.service.seguimiento.SeguimientoNotificacionService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.account.UsuarioSistemaRepository;

@Service
public class SeguimientoCommandService {

    private final SeguimientoRepository seguimientoRepository;
    private final CategoriaSeguimientoRepository categoriaSeguimientoRepository;
    private final ConsultaRepository consultaRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final SeguimientoNotificacionService seguimientoNotificacionService;
    private final SeguimientoAccessService seguimientoAccessService;
    private final SeguimientoMapper seguimientoMapper;
    private final SeguimientoValidator seguimientoValidator;

    public SeguimientoCommandService(
            SeguimientoRepository seguimientoRepository,
            CategoriaSeguimientoRepository categoriaSeguimientoRepository,
            ConsultaRepository consultaRepository,
            UsuarioSistemaRepository usuarioSistemaRepository,
            SeguimientoNotificacionService seguimientoNotificacionService,
            SeguimientoAccessService seguimientoAccessService,
            SeguimientoMapper seguimientoMapper,
            SeguimientoValidator seguimientoValidator) {
        this.seguimientoRepository = seguimientoRepository;
        this.categoriaSeguimientoRepository = categoriaSeguimientoRepository;
        this.consultaRepository = consultaRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.seguimientoNotificacionService = seguimientoNotificacionService;
        this.seguimientoAccessService = seguimientoAccessService;
        this.seguimientoMapper = seguimientoMapper;
        this.seguimientoValidator = seguimientoValidator;
    }

    @Transactional
    public SeguimientoResponseDTO crear(SeguimientoRequestDTO dto) {
        seguimientoValidator.validarCreacion(dto);
        seguimientoAccessService.validarPuedeCrearSeguimiento(dto.getConsultaId());

        DatosSeguimiento datos = prepararDatos(dto);
        UsuarioSistema autor = obtenerAutorActual();

        Seguimiento seguimiento = new Seguimiento();
        seguimientoMapper.aplicarDatos(seguimiento, datos);
        seguimiento.setAutor(autor);

        // Todo seguimiento nuevo inicia activo para permitir borrado lógico después.
        seguimiento.setActivo(true);

        Seguimiento seguimientoGuardado = seguimientoRepository.save(seguimiento);

        // Las notificaciones dependen del id del seguimiento, por eso se sincronizan después de guardar.
        seguimientoNotificacionService.sincronizarNotificaciones(seguimientoGuardado.getId());

        return seguimientoMapper.convertirAResponseDTO(seguimientoGuardado);
    }

    @Transactional
    public SeguimientoResponseDTO actualizar(Long id, SeguimientoRequestDTO dto) {
        seguimientoAccessService.validarPuedeEditarSeguimiento(id);

        Seguimiento seguimiento = buscarPorId(id);

        seguimientoValidator.validarActualizacion(id, dto);
        seguimientoValidator.validarNoCambieConsulta(seguimiento, dto);

        DatosSeguimiento datos = prepararDatos(dto);

        seguimientoValidator.validarExistenCambios(seguimiento, datos);

        seguimientoMapper.aplicarDatos(seguimiento, datos);

        Seguimiento seguimientoGuardado = seguimientoRepository.save(seguimiento);

        // Si cambian fechas o banderas, se recalculan pendientes y se cancelan las que ya no aplican.
        seguimientoNotificacionService.sincronizarNotificaciones(seguimientoGuardado.getId());

        return seguimientoMapper.convertirAResponseDTO(seguimientoGuardado);
    }

    @Transactional
    public void eliminar(Long id) {
        seguimientoAccessService.validarPuedeEliminarSeguimiento(id);

        Seguimiento seguimiento = buscarPorId(id);

        // Primero se cancelan pendientes; las enviadas quedan como historial.
        seguimientoNotificacionService.cancelarNotificacionesPendientes(seguimiento.getId());

        // El seguimiento queda inactivo para no perder trazabilidad dentro de la consulta.
        seguimiento.setActivo(false);

        seguimientoRepository.save(seguimiento);
    }

    private DatosSeguimiento prepararDatos(SeguimientoRequestDTO dto) {
        String descripcion = seguimientoValidator.normalizarDescripcion(dto.getDescripcion());

        seguimientoValidator.validarDatosSeguimiento(
                dto.getFechaEntrega(),
                dto.getDiasNotificacion());

        CategoriaSeguimiento categoria = obtenerCategoriaActiva(dto.getCategoriaSeguimientoId());
        Consulta consulta = obtenerConsulta(dto.getConsultaId());

        return new DatosSeguimiento(
                descripcion,
                dto.getFechaEntrega(),
                dto.getDiasNotificacion(),
                valorBooleano(dto.getNotificarPartes()),
                valorBooleano(dto.getNotificarEstudiante()),
                valorBooleano(dto.getAlertaDisciplinaria()),
                categoria,
                consulta);
    }

    private Seguimiento buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del seguimiento es obligatorio");
        }

        return seguimientoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Seguimiento no encontrado con id: " + id));
    }

    private CategoriaSeguimiento obtenerCategoriaActiva(Long categoriaId) {
        if (categoriaId == null) {
            throw new BusinessException("La categoría del seguimiento es obligatoria");
        }

        return categoriaSeguimientoRepository.findByIdAndActivoTrue(categoriaId)
                .orElseThrow(() -> new BusinessException(
                        "Categoría de seguimiento no encontrada o inactiva con id: " + categoriaId));
    }

    private Consulta obtenerConsulta(Long consultaId) {
        if (consultaId == null) {
            throw new BusinessException("La consulta es obligatoria");
        }

        return consultaRepository.findById(consultaId)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + consultaId));
    }

    private UsuarioSistema obtenerAutorActual() {
        Long usuarioActualId = seguimientoAccessService.obtenerUsuarioActualId();

        return usuarioSistemaRepository.findById(usuarioActualId)
                .orElseThrow(() -> new BusinessException("Autor no encontrado con id: " + usuarioActualId));
    }

    private Boolean valorBooleano(Boolean valor) {
        return valor != null ? valor : false;
    }
}