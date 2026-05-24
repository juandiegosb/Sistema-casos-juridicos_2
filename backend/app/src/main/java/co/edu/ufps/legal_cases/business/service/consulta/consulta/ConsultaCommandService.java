package co.edu.ufps.legal_cases.business.service.consulta.consulta;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaDTO;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.service.acceso.consulta.ConsultaAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import lombok.AllArgsConstructor;

// Este servicio maneja los cambios de Consulta en la BD
// a diferencia del QueryService que solo lee
@Service
@AllArgsConstructor
public class ConsultaCommandService {

    private static final EstadoConsulta ESTADO_ARCHIVADO = EstadoConsulta.ARCHIVADO;

    private final ConsultaRepository consultaRepository;
    private final ConsultaAccessService consultaAccessService;
    private final ConsultaValidator consultaValidator;
    private final ConsultaMapper consultaMapper;
    private final ConsultaEstadoService consultaEstadoService;
    private final ConsultaConstruccionService consultaConstruccionService;

    @Transactional
    public ConsultaDTO crear(ConsultaDTO dto) {
        consultaAccessService.validarPuedeCrearConsulta();
        consultaValidator.validarIdNoEnviadoEnCreacion(dto.getId());
        consultaValidator.validarCamposObligatorios(dto);
        consultaValidator.validarEstadoInicialPermitido(dto.getEstado());

        boolean puedeAsignarResponsables = consultaAccessService.usuarioPuedeAsignarResponsables();

        Consulta consulta = consultaConstruccionService.aplicarDatos(
                new Consulta(),
                dto,
                puedeAsignarResponsables);

        if (!puedeAsignarResponsables) {
            consultaConstruccionService.asignarResponsablesSegunUsuarioActual(consulta);
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
        consultaEstadoService.validarPermiteOperacionOperativa(existente);
        consultaValidator.validarCamposObligatorios(dto);
        consultaValidator.validarIdNoCambiado(existente.getId(), dto.getId());

        EstadoConsulta estadoActual = existente.getEstado();

        consultaConstruccionService.aplicarDatos(
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
        consultaEstadoService.validarCambioEstado(consulta, estado);

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
        consultaEstadoService.validarPuedeArchivar(consulta);

        consulta.setEstado(ESTADO_ARCHIVADO);
        consultaRepository.save(consulta);
    }

    @Transactional
    public ConsultaDTO archivar(Long id) {
        consultaAccessService.validarPuedeArchivarConsulta(id);

        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + id));

        consultaValidator.validarNoArchivadaParaArchivar(consulta);
        consultaEstadoService.validarPuedeArchivar(consulta);

        consulta.setEstado(ESTADO_ARCHIVADO);

        return consultaMapper.convertirADTO(consultaRepository.save(consulta));
    }
}