package co.edu.ufps.legal_cases.business.service.seguimiento;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.account.UsuarioSistemaRepository;
import lombok.RequiredArgsConstructor;

import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.equalsIgnoreCase;
import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.mismoId;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

@Service
@RequiredArgsConstructor
public class SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final CategoriaSeguimientoRepository categoriaSeguimientoRepository;
    private final ConsultaRepository consultaRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarPorConsulta(Long consultaId) {
        obtenerConsulta(consultaId);

        return seguimientoRepository.findByConsulta_IdOrderByFechaCreacionDesc(consultaId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarPorAutor(Long autorId) {
        if (autorId == null) {
            throw new BusinessException("El id del autor es obligatorio");
        }

        return seguimientoRepository.findByAutor_IdOrderByFechaCreacionDesc(autorId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarAlertasDisciplinarias() {
        return seguimientoRepository.findByAlertaDisciplinariaTrueOrderByFechaCreacionDesc()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDTO> listarPorFechaEntrega(LocalDate fechaEntrega) {
        if (fechaEntrega == null) {
            throw new BusinessException("La fecha de entrega es obligatoria");
        }

        return seguimientoRepository.findByFechaEntregaOrderByFechaCreacionDesc(fechaEntrega)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeguimientoResponseDTO obtenerPorId(Long id) {
        return convertirAResponseDTO(buscarPorId(id));
    }

    @Transactional
    public SeguimientoResponseDTO crear(SeguimientoRequestDTO dto, String autorUsername) {
        validarCreacion(dto);

        DatosSeguimiento datos = prepararDatos(dto);
        UsuarioSistema autor = obtenerAutor(autorUsername);

        Seguimiento seguimiento = new Seguimiento();
        aplicarDatos(seguimiento, datos);
        seguimiento.setAutor(autor);

        Seguimiento seguimientoGuardado = seguimientoRepository.save(seguimiento);

        // Fase posterior:
        // Si seguimientoGuardado.getNotificarPartes() es true,
        // se podrá invocar aquí un servicio de notificaciones por correo.
        //
        // Si seguimientoGuardado.getAlertaDisciplinaria() es true,
        // se podrá conectar con un módulo de alertas disciplinarias.

        return convertirAResponseDTO(seguimientoGuardado);
    }

    @Transactional
    public SeguimientoResponseDTO actualizar(Long id, SeguimientoRequestDTO dto) {
        Seguimiento seguimiento = buscarPorId(id);

        validarActualizacion(id, dto);

        DatosSeguimiento datos = prepararDatos(dto);

        if (sinCambios(seguimiento, datos)) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        aplicarDatos(seguimiento, datos);

        return convertirAResponseDTO(seguimientoRepository.save(seguimiento));
    }

    @Transactional
    public void eliminar(Long id) {
        Seguimiento seguimiento = buscarPorId(id);
        seguimientoRepository.delete(seguimiento);
    }

    private void validarCreacion(SeguimientoRequestDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, SeguimientoRequestDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null && !Objects.equals(dto.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id del seguimiento");
        }
    }

    private void validarDtoObligatorio(SeguimientoRequestDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos del seguimiento son obligatorios");
        }
    }

    private DatosSeguimiento prepararDatos(SeguimientoRequestDTO dto) {
        String descripcion = normalizarTexto(dto.getDescripcion());

        validarDatosSeguimiento(
                descripcion,
                dto.getFechaEntrega(),
                dto.getDiasNotificacion(),
                dto.getNotificarPartes(),
                dto.getNotificarEstudiante()
        );

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
                consulta
        );
    }

    private void validarDatosSeguimiento(
            String descripcion,
            LocalDate fechaEntrega,
            Integer diasNotificacion,
            Boolean notificarPartes,
            Boolean notificarEstudiante) {

        if (descripcion == null || descripcion.isBlank()) {
            throw new BusinessException("La descripción del seguimiento es obligatoria");
        }

        if (descripcion.length() > 200) {
            throw new BusinessException("La descripción del seguimiento no puede superar 200 caracteres");
        }

        if (fechaEntrega != null && fechaEntrega.isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de entrega no puede ser anterior a la fecha actual");
        }

        if (diasNotificacion != null && diasNotificacion < 0) {
            throw new BusinessException("Los días de notificación no pueden ser negativos");
        }

        if (diasNotificacion != null && fechaEntrega == null) {
            throw new BusinessException("No se pueden definir días de notificación sin fecha de entrega");
        }

        if (Boolean.TRUE.equals(notificarEstudiante) && !Boolean.TRUE.equals(notificarPartes)) {
            throw new BusinessException("No se puede notificar al estudiante si no se notifican las partes");
        }
    }

    private void aplicarDatos(Seguimiento seguimiento, DatosSeguimiento datos) {
        seguimiento.setDescripcion(datos.descripcion());
        seguimiento.setFechaEntrega(datos.fechaEntrega());
        seguimiento.setDiasNotificacion(datos.diasNotificacion());
        seguimiento.setNotificarPartes(datos.notificarPartes());
        seguimiento.setNotificarEstudiante(datos.notificarEstudiante());
        seguimiento.setAlertaDisciplinaria(datos.alertaDisciplinaria());
        seguimiento.setCategoriaSeguimiento(datos.categoria());
        seguimiento.setConsulta(datos.consulta());
    }

    private boolean sinCambios(Seguimiento seguimiento, DatosSeguimiento datos) {
        return equalsIgnoreCase(seguimiento.getDescripcion(), datos.descripcion())
                && Objects.equals(seguimiento.getFechaEntrega(), datos.fechaEntrega())
                && Objects.equals(seguimiento.getDiasNotificacion(), datos.diasNotificacion())
                && Objects.equals(seguimiento.getNotificarPartes(), datos.notificarPartes())
                && Objects.equals(seguimiento.getNotificarEstudiante(), datos.notificarEstudiante())
                && Objects.equals(seguimiento.getAlertaDisciplinaria(), datos.alertaDisciplinaria())
                && mismoId(seguimiento.getCategoriaSeguimiento(), datos.categoria(), CategoriaSeguimiento::getId)
                && mismoId(seguimiento.getConsulta(), datos.consulta(), Consulta::getId);
    }

    private Seguimiento buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del seguimiento es obligatorio");
        }

        return seguimientoRepository.findById(id)
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

    private UsuarioSistema obtenerAutor(String autorUsername) {
        String username = normalizarEmail(autorUsername);

        if (username == null) {
            throw new BusinessException("No se pudo identificar el autor del seguimiento");
        }

        return usuarioSistemaRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BusinessException("Autor no encontrado: " + username));
    }

    private Boolean valorBooleano(Boolean valor) {
        return valor != null ? valor : false;
    }

    private SeguimientoResponseDTO convertirAResponseDTO(Seguimiento seguimiento) {
        SeguimientoResponseDTO dto = new SeguimientoResponseDTO();

        dto.setId(seguimiento.getId());
        dto.setDescripcion(seguimiento.getDescripcion());
        dto.setFechaEntrega(seguimiento.getFechaEntrega());
        dto.setDiasNotificacion(seguimiento.getDiasNotificacion());
        dto.setNotificarPartes(seguimiento.getNotificarPartes());
        dto.setNotificarEstudiante(seguimiento.getNotificarEstudiante());
        dto.setAlertaDisciplinaria(seguimiento.getAlertaDisciplinaria());

        dto.setCategoriaSeguimientoId(seguimiento.getCategoriaSeguimiento().getId());
        dto.setCategoriaSeguimientoNombre(seguimiento.getCategoriaSeguimiento().getNombre());

        dto.setConsultaId(seguimiento.getConsulta().getId());

        dto.setAutorId(seguimiento.getAutor().getId());
        dto.setAutorUsername(seguimiento.getAutor().getUsername());

        dto.setFechaCreacion(seguimiento.getFechaCreacion());

        return dto;
    }

    private record DatosSeguimiento(
            String descripcion,
            LocalDate fechaEntrega,
            Integer diasNotificacion,
            Boolean notificarPartes,
            Boolean notificarEstudiante,
            Boolean alertaDisciplinaria,
            CategoriaSeguimiento categoria,
            Consulta consulta
    ) {
    }
}