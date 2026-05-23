package co.edu.ufps.legal_cases.business.service.consulta.consulta;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Area;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.Tema;
import co.edu.ufps.legal_cases.business.model.catalogo.Tipo;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta;
import co.edu.ufps.legal_cases.business.model.perfil.Asesor;
import co.edu.ufps.legal_cases.business.model.perfil.Estudiante;
import co.edu.ufps.legal_cases.business.model.perfil.Monitor;
import co.edu.ufps.legal_cases.business.model.persona.Persona;
import co.edu.ufps.legal_cases.business.repository.catalogo.AreaRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TemaRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoRepository;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.AsesorRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.EstudianteRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.MonitorRepository;
import co.edu.ufps.legal_cases.business.repository.persona.PersonaRepository;
import co.edu.ufps.legal_cases.business.service.acceso.consulta.ConsultaAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;
import lombok.AllArgsConstructor;

// Este servicio maneja los cambios de Consulta en la BD
// a diferencia del QueryService que solo lee
@Service
@AllArgsConstructor
public class ConsultaCommandService {

    private static final EstadoConsulta ESTADO_ARCHIVADO = EstadoConsulta.ARCHIVADO;

    private final ConsultaRepository consultaRepository;
    private final PersonaRepository personaRepository;
    private final SedeRepository sedeRepository;
    private final AreaRepository areaRepository;
    private final TemaRepository temaRepository;
    private final TipoRepository tipoRepository;
    private final AsesorRepository asesorRepository;
    private final MonitorRepository monitorRepository;
    private final EstudianteRepository estudianteRepository;
    private final ConsultaAccessService consultaAccessService;
    private final ConsultaValidator consultaValidator;
    private final ConsultaMapper consultaMapper;
    private final ConsultaEstadoService consultaEstadoReglaService;

    @Transactional
    public ConsultaDTO crear(ConsultaDTO dto) {
        consultaAccessService.validarPuedeCrearConsulta();
        consultaValidator.validarIdNoEnviadoEnCreacion(dto.getId());
        consultaValidator.validarCamposObligatorios(dto);

        Consulta consulta = construirDesdeDTO(
                new Consulta(),
                dto,
                consultaAccessService.usuarioPuedeAsignarResponsables());

        if (!consultaAccessService.usuarioPuedeAsignarResponsables()) {
            asignarResponsablesSegunUsuarioActual(consulta);
        }

        // Valida relaciones cruzadas del dominio antes de guardar.
        // Ejemplo: tema-área, tipo-tema, asesor-área y personas repetidas.
        consultaValidator.validarCoherenciaDominio(consulta);

        return consultaMapper.convertirADTO(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaDTO actualizar(Long id, ConsultaDTO dto) {
        consultaAccessService.validarPuedeEditarConsulta(id);

        Consulta existente = consultaRepository.findByIdConPartes(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));
        consultaRepository.findByIdConContrapartes(id);

        consultaValidator.validarNoArchivada(existente);
        consultaValidator.validarCamposObligatorios(dto);
        consultaValidator.validarIdNoCambiado(existente.getId(), dto.getId());

        EstadoConsulta estadoActual = existente.getEstado();

        construirDesdeDTO(
                existente,
                dto,
                consultaAccessService.usuarioPuedeAsignarResponsables());

        // Actualizar datos de la consulta no debe cambiar el estado.
        // Para eso existe cambiarEstado().
        existente.setEstado(estadoActual);

        // Valida relaciones cruzadas después de aplicar los cambios del DTO.
        consultaValidator.validarCoherenciaDominio(existente);

        return consultaMapper.convertirADTO(consultaRepository.save(existente));
    }

    @Transactional
    public ConsultaDTO cambiarEstado(Long id, EstadoConsulta estado) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));

        consultaValidator.validarNoArchivada(consulta);
        consultaValidator.validarCambioEstadoPermitido(consulta, estado);
        consultaEstadoReglaService.validarCambioEstado(consulta, estado);

        consulta.setEstado(estado);

        return consultaMapper.convertirADTO(consultaRepository.save(consulta));
    }

    // Se conserva el nombre eliminar por compatibilidad con el endpoint antiguo.
    // Para evitar pérdida de información, funciona como archivado lógico.
    @Transactional
    public void eliminar(Long id) {
        consultaAccessService.validarPuedeArchivarConsulta(id);

        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));

        consultaValidator.validarNoArchivadaParaArchivar(consulta);
        consultaEstadoReglaService.validarPuedeArchivar(consulta);

        consulta.setEstado(ESTADO_ARCHIVADO);
        consultaRepository.save(consulta);
    }

    @Transactional
    public ConsultaDTO archivar(Long id) {
        consultaAccessService.validarPuedeArchivarConsulta(id);

        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));

        consultaValidator.validarNoArchivadaParaArchivar(consulta);
        consultaEstadoReglaService.validarPuedeArchivar(consulta);

        consulta.setEstado(ESTADO_ARCHIVADO);

        return consultaMapper.convertirADTO(consultaRepository.save(consulta));
    }

    // Helpers de lookup

    private Persona obtenerPersona(Long id) {
        return personaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Persona no encontrada o inactiva con id: " + id));
    }

    private Sede obtenerSede(Long id) {
        return sedeRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Sede no encontrada o inactiva con id: " + id));
    }

    private Area obtenerArea(Long id) {
        return areaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Área no encontrada o inactiva con id: " + id));
    }

    private Tema obtenerTema(Long id) {
        return temaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Tema no encontrado o inactivo con id: " + id));
    }

    private Tipo obtenerTipo(Long id) {
        if (id == null)
            return null;

        return tipoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Tipo no encontrado o inactivo con id: " + id));
    }

    private Asesor obtenerAsesor(Long id) {
        if (id == null)
            return null;

        return asesorRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Asesor no encontrado o inactivo con id: " + id));
    }

    private Monitor obtenerMonitor(Long id) {
        if (id == null)
            return null;

        return monitorRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Monitor no encontrado o inactivo con id: " + id));
    }

    private Estudiante obtenerEstudiante(Long id) {
        if (id == null)
            return null;

        return estudianteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado o inactivo con id: " + id));
    }

    private Asesor obtenerAsesorDelEstudianteActivo(Estudiante estudiante) {
        if (estudiante == null || estudiante.getAsesor() == null) {
            throw new BusinessException("El estudiante seleccionado no tiene asesor asignado");
        }

        return asesorRepository.findByIdAndActivoTrue(estudiante.getAsesor().getId())
                .orElseThrow(() -> new BusinessException(
                        "El asesor asignado al estudiante no existe o está inactivo"));
    }

    // Construcción y asignación

    private Consulta construirDesdeDTO(
            Consulta consulta,
            ConsultaDTO dto,
            boolean puedeAsignarResponsables) {

        consulta.setFecha(dto.getFecha());
        consulta.setDescripcion(normalizarTexto(dto.getDescripcion()));
        consulta.setHechos(normalizarTexto(dto.getHechos()));
        consulta.setPretensiones(normalizarTexto(dto.getPretensiones()));
        consulta.setConceptoJuridico(normalizarTexto(dto.getConceptoJuridico()));
        consulta.setTramite(normalizarTexto(dto.getTramite()));
        consulta.setObservaciones(normalizarTexto(dto.getObservaciones()));
        consulta.setTipoViolencia(normalizarTexto(dto.getTipoViolencia()));
        consulta.setEstado(dto.getEstado());
        consulta.setResultado(normalizarTexto(dto.getResultado()));

        consulta.setPersona(obtenerPersona(dto.getPersonaId()));
        consulta.setSede(obtenerSede(dto.getSedeId()));
        consulta.setArea(obtenerArea(dto.getAreaId()));
        consulta.setTema(obtenerTema(dto.getTemaId()));
        consulta.setTipo(obtenerTipo(dto.getTipoId()));

        if (puedeAsignarResponsables) {
            Asesor asesor = obtenerAsesor(dto.getAsesorId());
            Estudiante estudiante = obtenerEstudiante(dto.getEstudianteId());

            // Si se asigna estudiante sin asesor explícito, se toma el asesor activo del
            // estudiante.
            // Esto evita consultas con estudiante asignado pero sin responsable académico.
            if (asesor == null && estudiante != null) {
                asesor = obtenerAsesorDelEstudianteActivo(estudiante);
            }

            consulta.setAsesor(asesor);
            consulta.setMonitor(obtenerMonitor(dto.getMonitorId()));
            consulta.setEstudiante(estudiante);
        }

        if (dto.getPartesIds() != null) {
            List<Persona> partes = dto.getPartesIds().stream()
                    .map(this::obtenerPersona).toList();
            consulta.getPartes().clear();
            consulta.getPartes().addAll(partes);
        } else {
            consulta.getPartes().clear();
        }

        if (dto.getContrapartesIds() != null) {
            List<Persona> contrapartes = dto.getContrapartesIds().stream()
                    .map(this::obtenerPersona).toList();
            consulta.getContrapartes().clear();
            consulta.getContrapartes().addAll(contrapartes);
        } else {
            consulta.getContrapartes().clear();
        }

        return consulta;
    }

    private void asignarResponsablesSegunUsuarioActual(Consulta consulta) {
        PerfilUsuarioActual perfil = consultaAccessService.obtenerPerfilActual();

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.ESTUDIANTE) {
            Estudiante estudiante = obtenerEstudiante(perfil.getPerfilId());
            consulta.setEstudiante(estudiante);
            consulta.setAsesor(estudiante.getAsesor());
            return;
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.ASESOR) {
            consulta.setAsesor(obtenerAsesor(perfil.getPerfilId()));
            return;
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.MONITOR) {
            consulta.setMonitor(obtenerMonitor(perfil.getPerfilId()));
        }
    }
}