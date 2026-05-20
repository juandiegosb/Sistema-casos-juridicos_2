package co.edu.ufps.legal_cases.business.service.perfil.estudiante;

import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.equalsIgnoreCase;
import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.mismoId;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarCodigo;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarNumeroDocumento;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTelefono;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarUsuario;

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
import co.edu.ufps.legal_cases.business.service.acceso.EstudianteAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaRegistroService;
import lombok.AllArgsConstructor;

// Este servicio maneja cambios del Estudiante en la bd
// a diferencia del query service que solo lee
@Service
@AllArgsConstructor
public class EstudianteCommandService {

    private final EstudianteRepository estudianteRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SedeRepository sedeRepository;
    private final AsesorRepository asesorRepository;
    private final UsuarioSistemaRegistroService usuarioSistemaRegistroService;
    private final EstudianteAccessService estudianteAccessService;
    private final EstudianteValidator estudianteValidator;
    private final EstudianteMapper estudianteMapper;

    @Transactional
    public EstudianteDTO crear(EstudianteDTO dto) {
        estudianteAccessService.validarPuedeGestionarEstudiantes();
        estudianteValidator.validarIdNoEnviadoEnCreacion(dto.getId());

        DatosEstudiante datos = prepararDatos(dto);

        estudianteValidator.validarDuplicadosCreacion(
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        Estudiante estudiante = new Estudiante();
        aplicarDatos(estudiante, datos);

        Estudiante estudianteGuardado = estudianteRepository.save(estudiante);

        UsuarioSistema usuarioSistema = usuarioSistemaRegistroService.crearParaEstudiante(estudianteGuardado);

        estudianteGuardado.setUsuarioSistema(usuarioSistema);

        Estudiante estudianteActualizado = estudianteRepository.save(estudianteGuardado);

        return estudianteMapper.convertirADTO(estudianteActualizado);
    }

    @Transactional
    public EstudianteDTO actualizar(Long id, EstudianteDTO dto) {
        estudianteAccessService.validarPuedeGestionarEstudiantes();
        estudianteValidator.validarIdNoCambiado(id, dto.getId());

        Estudiante existente = buscarPorId(id);
        DatosEstudiante datos = prepararDatos(dto);

        estudianteValidator.validarDuplicadosActualizacion(
                id,
                datos.documento(),
                datos.email(),
                datos.telefono(),
                datos.usuario(),
                datos.codigo());

        if (sinCambios(existente, datos)) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        aplicarDatos(existente, datos);

        return estudianteMapper.convertirADTO(estudianteRepository.save(existente));
    }

    @Transactional
    public EstudianteDTO cambiarEstado(Long id, Boolean activo) {
        estudianteAccessService.validarPuedeCambiarEstadoEstudiante();

        Estudiante estudiante = buscarPorId(id);

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(estudiante.getActivo(), activo)) {
            throw new BusinessException("El estudiante ya tiene ese estado");
        }

        estudiante.setActivo(activo);

        return estudianteMapper.convertirADTO(estudianteRepository.save(estudiante));
    }

    @Transactional
    public EstudianteDTO cambiarConciliacion(Long id, Boolean conciliacion) {
        estudianteAccessService.validarPuedeGestionarEstudiantes();

        Estudiante estudiante = buscarPorId(id);

        if (conciliacion == null) {
            throw new BusinessException("El estado de conciliación es obligatorio");
        }

        if (Objects.equals(estudiante.getConciliacion(), conciliacion)) {
            throw new BusinessException("El estudiante ya tiene ese estado de conciliación");
        }

        estudiante.setConciliacion(conciliacion);

        return estudianteMapper.convertirADTO(estudianteRepository.save(estudiante));
    }

    @Transactional
    public void eliminar(Long id) {
        estudianteAccessService.validarPuedeGestionarEstudiantes();

        Estudiante estudiante = buscarPorId(id);

        estudianteRepository.delete(estudiante);
    }

    private DatosEstudiante prepararDatos(EstudianteDTO dto) {
        String nombre = normalizarTexto(dto.getNombre());
        String documento = normalizarNumeroDocumento(dto.getDocumento());
        String email = normalizarEmail(dto.getEmail());
        String telefono = normalizarTelefono(dto.getTelefono());
        String usuario = normalizarUsuario(dto.getUsuario());
        String codigo = normalizarCodigo(dto.getCodigo());

        estudianteValidator.validarCamposObligatorios(
                nombre,
                documento,
                email,
                telefono,
                usuario,
                codigo);

        TipoDocumento tipoDocumento = obtenerTipoDocumento(dto.getTipoDocumentoId());
        Sede sede = obtenerSede(dto.getSedeId());
        Asesor asesor = obtenerAsesor(dto.getAsesorId());

        Boolean activo = dto.getActivo() != null ? dto.getActivo() : true;
        Boolean conciliacion = dto.getConciliacion() != null ? dto.getConciliacion() : false;

        return new DatosEstudiante(
                nombre,
                documento,
                email,
                telefono,
                usuario,
                codigo,
                tipoDocumento,
                sede,
                asesor,
                activo,
                conciliacion);
    }

    private void aplicarDatos(Estudiante estudiante, DatosEstudiante datos) {
        estudiante.setNombre(datos.nombre());
        estudiante.setTipoDocumento(datos.tipoDocumento());
        estudiante.setDocumento(datos.documento());
        estudiante.setEmail(datos.email());
        estudiante.setTelefono(datos.telefono());
        estudiante.setUsuario(datos.usuario());
        estudiante.setSede(datos.sede());
        estudiante.setCodigo(datos.codigo());
        estudiante.setAsesor(datos.asesor());
        estudiante.setActivo(datos.activo());
        estudiante.setConciliacion(datos.conciliacion());
    }

    private boolean sinCambios(Estudiante estudiante, DatosEstudiante datos) {
        return equalsIgnoreCase(estudiante.getNombre(), datos.nombre())
                && mismoId(estudiante.getTipoDocumento(), datos.tipoDocumento(), TipoDocumento::getId)
                && Objects.equals(estudiante.getDocumento(), datos.documento())
                && equalsIgnoreCase(estudiante.getEmail(), datos.email())
                && Objects.equals(estudiante.getTelefono(), datos.telefono())
                && equalsIgnoreCase(estudiante.getUsuario(), datos.usuario())
                && mismoId(estudiante.getSede(), datos.sede(), Sede::getId)
                && equalsIgnoreCase(estudiante.getCodigo(), datos.codigo())
                && mismoId(estudiante.getAsesor(), datos.asesor(), Asesor::getId)
                && Objects.equals(estudiante.getActivo(), datos.activo())
                && Objects.equals(estudiante.getConciliacion(), datos.conciliacion());
    }

    private Estudiante buscarPorId(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado con id: " + id));
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

    private record DatosEstudiante(
            String nombre,
            String documento,
            String email,
            String telefono,
            String usuario,
            String codigo,
            TipoDocumento tipoDocumento,
            Sede sede,
            Asesor asesor,
            Boolean activo,
            Boolean conciliacion) {
    }
}