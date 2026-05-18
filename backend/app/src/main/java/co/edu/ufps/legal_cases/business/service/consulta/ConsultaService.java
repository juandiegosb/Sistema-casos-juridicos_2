package co.edu.ufps.legal_cases.business.service.consulta;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaBusquedaDTO;
import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Area;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.Tema;
import co.edu.ufps.legal_cases.business.model.catalogo.Tipo;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
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
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.account.UsuarioSistemaRepository;
import co.edu.ufps.legal_cases.security.service.account.PerfilUsuarioResolverService;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PersonaRepository personaRepository;
    private final SedeRepository sedeRepository;
    private final AreaRepository areaRepository;
    private final TemaRepository temaRepository;
    private final TipoRepository tipoRepository;
    private final AsesorRepository asesorRepository;
    private final MonitorRepository monitorRepository;
    private final EstudianteRepository estudianteRepository;
    private final PerfilUsuarioResolverService perfilUsuarioResolverService;
    private final UsuarioSistemaRepository usuarioSistemaRepository;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            PersonaRepository personaRepository,
            SedeRepository sedeRepository,
            AreaRepository areaRepository,
            TemaRepository temaRepository,
            TipoRepository tipoRepository,
            AsesorRepository asesorRepository,
            MonitorRepository monitorRepository,
            EstudianteRepository estudianteRepository,
            PerfilUsuarioResolverService perfilUsuarioResolverService,
            UsuarioSistemaRepository usuarioSistemaRepository) {
        this.consultaRepository = consultaRepository;
        this.personaRepository = personaRepository;
        this.sedeRepository = sedeRepository;
        this.areaRepository = areaRepository;
        this.temaRepository = temaRepository;
        this.tipoRepository = tipoRepository;
        this.asesorRepository = asesorRepository;
        this.monitorRepository = monitorRepository;
        this.estudianteRepository = estudianteRepository;
        this.perfilUsuarioResolverService = perfilUsuarioResolverService;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
    }

    /**
     * Busca consultas por texto libre en descripción, nombre, apellido o cédula.
     * Si el parámetro es null o vacío, retorna todas las consultas.
     */
    public List<ConsultaBusquedaDTO> buscar(String search) {
        String termino = normalizarTexto(search);
        return consultaRepository.buscar(termino)
                .stream()
                .map(this::convertirABusquedaDTO)
                .toList();
    }

    /**
     * Busca consultas filtrando según el rol del usuario autenticado.
     * - Estudiante: solo sus consultas
     * - Asesor: consultas de sus estudiantes
     * - Monitor: consultas donde está asignado
     * - Administrativo/Conciliador: todas
     */
    public List<ConsultaBusquedaDTO> buscarSegunRol(String search, Authentication authentication) {
        String termino = normalizarTexto(search);

        if (authentication == null) {
            return consultaRepository.buscar(termino)
                    .stream().map(this::convertirABusquedaDTO).toList();
        }

        String username = authentication.getName();
        UsuarioSistema usuario = usuarioSistemaRepository
                .findWithRolPermisosAndPerfilByUsername(username)
                .orElse(null);

        if (usuario == null) {
            return consultaRepository.buscar(termino)
                    .stream().map(this::convertirABusquedaDTO).toList();
        }

        PerfilUsuarioActual perfil = null;
        try {
            perfil = perfilUsuarioResolverService.obtenerPerfilActivoObligatorio(usuario);
        } catch (Exception e) {
            // Si no tiene perfil asociado, ve todas las consultas
        }

        Long estudianteId = null;
        Long asesorId = null;
        Long monitorId = null;

        if (perfil != null) {
            if (perfil.getTipoPerfil() == TipoPerfilUsuario.ESTUDIANTE) {
                estudianteId = perfil.getPerfilId();
            } else if (perfil.getTipoPerfil() == TipoPerfilUsuario.ASESOR) {
                asesorId = perfil.getPerfilId();
            } else if (perfil.getTipoPerfil() == TipoPerfilUsuario.MONITOR) {
                monitorId = perfil.getPerfilId();
            }
            // ADMINISTRATIVO y CONCILIADOR pasan null → ven todas
        }

        return consultaRepository.buscarFiltrado(termino, estudianteId, asesorId, monitorId)
                .stream().map(this::convertirABusquedaDTO).toList();
    }

    public List<ConsultaDTO> listar() {
        return consultaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsultaDTO obtenerPorId(Long id) {
        Consulta consulta = consultaRepository.findByIdConPartes(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));
        consultaRepository.findByIdConContrapartes(id);
        return convertirADTO(consulta);
    }

    @Transactional
    public ConsultaDTO crear(ConsultaDTO dto) {
        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
        validarCamposObligatorios(dto);
        Consulta consulta = construirDesdeDTO(new Consulta(), dto);
        return convertirADTO(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaDTO actualizar(Long id, ConsultaDTO dto) {
        Consulta existente = consultaRepository.findByIdConPartes(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));
        consultaRepository.findByIdConContrapartes(id);
        validarCamposObligatorios(dto);
        if (dto.getId() != null && !dto.getId().equals(existente.getId())) {
            throw new BusinessException("No se permite cambiar el id de la consulta");
        }
        construirDesdeDTO(existente, dto);
        return convertirADTO(consultaRepository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));
        consultaRepository.delete(consulta);
    }

    @Transactional
    public ConsultaDTO archivar(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));
        consulta.setEstado("Archivado");
        return convertirADTO(consultaRepository.save(consulta));
    }

    public List<ConsultaBusquedaDTO> listarArchivadas() {
        return consultaRepository.findByEstadoIgnoreCase("Archivado")
                .stream()
                .map(this::convertirABusquedaDTO)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Validaciones
    // -------------------------------------------------------------------------

    private void validarCamposObligatorios(ConsultaDTO dto) {
        if (dto.getFecha() == null) throw new BusinessException("La fecha es obligatoria");
        if (normalizarTexto(dto.getDescripcion()) == null) throw new BusinessException("La descripción es obligatoria");
        if (normalizarTexto(dto.getHechos()) == null) throw new BusinessException("Los hechos son obligatorios");
        if (normalizarTexto(dto.getPretensiones()) == null) throw new BusinessException("Las pretensiones son obligatorias");
        if (normalizarTexto(dto.getConceptoJuridico()) == null) throw new BusinessException("El concepto jurídico es obligatorio");
        if (normalizarTexto(dto.getTramite()) == null) throw new BusinessException("El trámite es obligatorio");
        if (normalizarTexto(dto.getEstado()) == null) throw new BusinessException("El estado es obligatorio");
        if (dto.getPersonaId() == null) throw new BusinessException("La persona es obligatoria");
        if (dto.getSedeId() == null) throw new BusinessException("La sede es obligatoria");
        if (dto.getAreaId() == null) throw new BusinessException("El área es obligatoria");
        if (dto.getTemaId() == null) throw new BusinessException("El tema es obligatorio");
    }

    // -------------------------------------------------------------------------
    // Helpers de lookup
    // -------------------------------------------------------------------------

    private Persona obtenerPersona(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Persona no encontrada con id: " + id));
    }

    private Sede obtenerSede(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Sede no encontrada con id: " + id));
    }

    private Area obtenerArea(Long id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Área no encontrada con id: " + id));
    }

    private Tema obtenerTema(Long id) {
        return temaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tema no encontrado con id: " + id));
    }

    private Tipo obtenerTipo(Long id) {
        if (id == null) return null;
        return tipoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo no encontrado con id: " + id));
    }

    private Asesor obtenerAsesor(Long id) {
        if (id == null) return null;
        return asesorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Asesor no encontrado con id: " + id));
    }

    private Monitor obtenerMonitor(Long id) {
        if (id == null) return null;
        return monitorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Monitor no encontrado con id: " + id));
    }

    private Estudiante obtenerEstudiante(Long id) {
        if (id == null) return null;
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado con id: " + id));
    }

    // -------------------------------------------------------------------------
    // Conversiones
    // -------------------------------------------------------------------------

    private Consulta construirDesdeDTO(Consulta consulta, ConsultaDTO dto) {
        consulta.setFecha(dto.getFecha());
        consulta.setDescripcion(normalizarTexto(dto.getDescripcion()));
        consulta.setHechos(normalizarTexto(dto.getHechos()));
        consulta.setPretensiones(normalizarTexto(dto.getPretensiones()));
        consulta.setConceptoJuridico(normalizarTexto(dto.getConceptoJuridico()));
        consulta.setTramite(normalizarTexto(dto.getTramite()));
        consulta.setObservaciones(normalizarTexto(dto.getObservaciones()));
        consulta.setTipoViolencia(normalizarTexto(dto.getTipoViolencia()));
        consulta.setEstado(normalizarTexto(dto.getEstado()));
        consulta.setResultado(normalizarTexto(dto.getResultado()));

        consulta.setPersona(obtenerPersona(dto.getPersonaId()));
        consulta.setSede(obtenerSede(dto.getSedeId()));
        consulta.setArea(obtenerArea(dto.getAreaId()));
        consulta.setTema(obtenerTema(dto.getTemaId()));
        consulta.setTipo(obtenerTipo(dto.getTipoId()));
        consulta.setAsesor(obtenerAsesor(dto.getAsesorId()));
        consulta.setMonitor(obtenerMonitor(dto.getMonitorId()));
        consulta.setEstudiante(obtenerEstudiante(dto.getEstudianteId()));

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

    private ConsultaDTO convertirADTO(Consulta c) {
        ConsultaDTO dto = new ConsultaDTO();
        dto.setId(c.getId());
        dto.setFecha(c.getFecha());
        dto.setDescripcion(c.getDescripcion());
        dto.setHechos(c.getHechos());
        dto.setPretensiones(c.getPretensiones());
        dto.setConceptoJuridico(c.getConceptoJuridico());
        dto.setTramite(c.getTramite());
        dto.setObservaciones(c.getObservaciones());
        dto.setTipoViolencia(c.getTipoViolencia());
        dto.setEstado(c.getEstado());
        dto.setResultado(c.getResultado());
        dto.setPersonaId(c.getPersona() != null ? c.getPersona().getId() : null);
        dto.setSedeId(c.getSede() != null ? c.getSede().getId() : null);
        dto.setAreaId(c.getArea() != null ? c.getArea().getId() : null);
        dto.setTemaId(c.getTema() != null ? c.getTema().getId() : null);
        dto.setTipoId(c.getTipo() != null ? c.getTipo().getId() : null);
        dto.setAsesorId(c.getAsesor() != null ? c.getAsesor().getId() : null);
        dto.setMonitorId(c.getMonitor() != null ? c.getMonitor().getId() : null);
        dto.setEstudianteId(c.getEstudiante() != null ? c.getEstudiante().getId() : null);
        dto.setPartesIds(
                c.getPartes() != null
                        ? c.getPartes().stream().map(Persona::getId).toList()
                        : List.of()
        );
        dto.setContrapartesIds(
                c.getContrapartes() != null
                        ? c.getContrapartes().stream().map(Persona::getId).toList()
                        : List.of()
        );
        return dto;
    }

    private ConsultaBusquedaDTO convertirABusquedaDTO(Consulta c) {
        Persona p = c.getPersona();
        return new ConsultaBusquedaDTO(
                c.getId(),
                c.getDescripcion(),
                c.getFecha(),
                p != null ? p.getNombres() : null,
                p != null ? p.getApellidos() : null,
                p != null ? p.getNumeroDocumento() : null,
                c.getEstado());
    }
}