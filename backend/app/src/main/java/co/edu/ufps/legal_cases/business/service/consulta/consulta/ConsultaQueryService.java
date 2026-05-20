package co.edu.ufps.legal_cases.business.service.consulta;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaBusquedaDTO;
import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaDTO;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.service.acceso.ConsultaAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;

// Servicio que maneja las consultas de lectura y usa el servicio de acceso para validar permisos
@Service
public class ConsultaQueryService {

    private final ConsultaRepository consultaRepository;
    private final ConsultaAccessService consultaAccessService;
    private final ConsultaMapper consultaMapper;

    public ConsultaQueryService(
            ConsultaRepository consultaRepository,
            ConsultaAccessService consultaAccessService,
            ConsultaMapper consultaMapper) {
        this.consultaRepository = consultaRepository;
        this.consultaAccessService = consultaAccessService;
        this.consultaMapper = consultaMapper;
    }

    /**
     * Busca consultas por texto libre en descripción, nombre, apellido o cédula.
     * El resultado se filtra según el alcance del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public List<ConsultaBusquedaDTO> buscarParaUsuarioActual(String search) {
        String termino = normalizarTexto(search);
        PerfilUsuarioActual perfil = consultaAccessService.obtenerPerfilActual();

        if (consultaAccessService.usuarioEsAdministrador()) {
            return consultaRepository.buscarParaAdministrador(termino)
                    .stream()
                    .map(consultaMapper::convertirABusquedaDTO)
                    .toList();
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.ESTUDIANTE) {
            return consultaRepository.buscarParaEstudiante(termino, perfil.getPerfilId())
                    .stream()
                    .map(consultaMapper::convertirABusquedaDTO)
                    .toList();
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.ASESOR) {
            return consultaRepository.buscarParaAsesor(termino, perfil.getPerfilId())
                    .stream()
                    .map(consultaMapper::convertirABusquedaDTO)
                    .toList();
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.MONITOR) {
            return consultaRepository.buscarParaMonitor(termino, perfil.getPerfilId())
                    .stream()
                    .map(consultaMapper::convertirABusquedaDTO)
                    .toList();
        }

        if (perfil.getTipoPerfil() == TipoPerfilUsuario.CONCILIADOR) {
            // Cuando esté implementado el módulo de conciliaciones, aquí se listarán
            // las consultas asociadas a las conciliaciones del conciliador.
            return List.of();
        }

        return List.of();
    }

    // Se conserva temporalmente para compatibilidad interna si alguna clase lo usa.
    @Transactional(readOnly = true)
    public List<ConsultaBusquedaDTO> buscar(String search) {
        return buscarParaUsuarioActual(search);
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> listar() {
        return consultaRepository.findAll()
                .stream()
                .map(consultaMapper::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsultaDTO obtenerPorId(Long id) {
        consultaAccessService.validarPuedeVerConsulta(id);

        Consulta consulta = consultaRepository.findByIdConPartes(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));
        consultaRepository.findByIdConContrapartes(id);

        return consultaMapper.convertirADTO(consulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaBusquedaDTO> listarArchivadas() {
        return consultaRepository.findByEstadoIgnoreCase("Archivado")
                .stream()
                .map(consultaMapper::convertirABusquedaDTO)
                .toList();
    }
}