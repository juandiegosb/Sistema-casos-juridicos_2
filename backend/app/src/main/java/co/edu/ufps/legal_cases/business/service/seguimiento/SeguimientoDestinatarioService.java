package co.edu.ufps.legal_cases.business.service.seguimiento;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.seguimiento.DatosNotificacionSeguimientoDTO;
import co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoDestinatarioDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.TipoNotificacionSeguimiento;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.AdministrativoRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.AsesorRepository;
import co.edu.ufps.legal_cases.business.repository.perfil.MonitorRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeguimientoDestinatarioService {

    private final SeguimientoRepository seguimientoRepository;
    private final ConsultaRepository consultaRepository;
    private final AdministrativoRepository administrativoRepository;
    private final AsesorRepository asesorRepository;
    private final MonitorRepository monitorRepository;

    public List<SeguimientoDestinatarioDTO> obtenerDestinatarios(
            Long seguimientoId,
            TipoNotificacionSeguimiento tipoNotificacion) {

        validarTipoNotificacion(tipoNotificacion);

        // Obtiene los datos del seguimiento
        DatosNotificacionSeguimientoDTO datos = obtenerDatosNotificacion(seguimientoId);

        List<SeguimientoDestinatarioDTO> candidatos = switch (tipoNotificacion) {
            case PARTES -> obtenerDestinatariosPartes(datos.getConsultaId());
            case ESTUDIANTE -> obtenerDestinatarioEstudiante(datos.getConsultaId());
            case ALERTA_DISCIPLINARIA -> administrativoRepository.findDestinatariosActivos();
            case RECORDATORIO_AUTOR -> obtenerDestinatarioAutor(datos);
        };

        // Al final luego de obtener los candidatos a notificar (destinararios) se validan
        return limpiarDestinatarios(candidatos);
    }

    private DatosNotificacionSeguimientoDTO obtenerDatosNotificacion(Long seguimientoId) {
        if (seguimientoId == null) {
            throw new BusinessException("El id del seguimiento es obligatorio");
        }

        // Trae los datos del seguimiento para el objeto que se creo en el metodo publico
        return seguimientoRepository.findDatosNotificacionById(seguimientoId)
                .orElseThrow(() -> new BusinessException("Seguimiento no encontrado con id: " + seguimientoId));
    }

    private List<SeguimientoDestinatarioDTO> obtenerDestinatariosPartes(Long consultaId) {
        List<SeguimientoDestinatarioDTO> destinatarios = new ArrayList<>();

        destinatarios.addAll(consultaRepository.findDestinatarioPersonaPrincipalByConsultaId(consultaId));
        destinatarios.addAll(consultaRepository.findDestinatariosPartesByConsultaId(consultaId));
        destinatarios.addAll(consultaRepository.findDestinatariosContrapartesByConsultaId(consultaId));

        return destinatarios;
    }

    private List<SeguimientoDestinatarioDTO> obtenerDestinatarioEstudiante(Long consultaId) {
        return consultaRepository.findDestinatarioEstudianteByConsultaId(consultaId)
                .map(List::of)
                .orElseGet(List::of);
    }

    private List<SeguimientoDestinatarioDTO> obtenerDestinatarioAutor(DatosNotificacionSeguimientoDTO datos) {
        if (datos.getAutorUsuarioSistemaId() == null) {
            return List.of();
        }

        // Aqui se tiene que ver si el que hizo el seguimiento es asesor o monitor
        return asesorRepository.findDestinatarioByUsuarioSistemaId(datos.getAutorUsuarioSistemaId())
                .or(() -> monitorRepository.findDestinatarioByUsuarioSistemaId(datos.getAutorUsuarioSistemaId()))
                .map(List::of)
                .orElseGet(() -> List.of(new SeguimientoDestinatarioDTO(
                        datos.getAutorEmail(),
                        datos.getAutorEmail()
                )));
    }

    private List<SeguimientoDestinatarioDTO> limpiarDestinatarios(
            List<SeguimientoDestinatarioDTO> candidatos) {

        Map<String, SeguimientoDestinatarioDTO> destinatariosPorEmail = new LinkedHashMap<>();

        // De la lista de destinararios que se tiene se depura para retornar la lista final
        for (SeguimientoDestinatarioDTO candidato : candidatos) {
            agregarSiEsValido(destinatariosPorEmail, candidato);
        }

        return new ArrayList<>(destinatariosPorEmail.values());
    }

    // Aqui se validan los datos del destinatario y se agrega al mapa
    private void agregarSiEsValido(
            Map<String, SeguimientoDestinatarioDTO> destinatariosPorEmail,
            SeguimientoDestinatarioDTO candidato) {

        if (candidato == null) {
            return;
        }

        String email = normalizarEmail(candidato.getEmail());

        if (email == null) {
            return;
        }

        String nombre = candidato.getNombre();

        if (nombre == null || nombre.isBlank()) {
            nombre = email;
        }

        destinatariosPorEmail.putIfAbsent(
                email,
                new SeguimientoDestinatarioDTO(email, nombre)
        );
    }

    private void validarTipoNotificacion(TipoNotificacionSeguimiento tipoNotificacion) {
        if (tipoNotificacion == null) {
            throw new BusinessException("El tipo de notificación es obligatorio");
        }
    }
}