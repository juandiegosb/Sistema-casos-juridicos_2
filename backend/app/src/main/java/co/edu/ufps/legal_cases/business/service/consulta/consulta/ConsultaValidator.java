package co.edu.ufps.legal_cases.business.service.consulta.consulta;

import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.equalsIgnoreCase;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import org.springframework.stereotype.Component;

import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaDTO;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.service.acceso.ConsultaAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

// Valida reglas de negocio para la entidad Consulta
@Component
public class ConsultaValidator {

    private final ConsultaAccessService consultaAccessService;

    public ConsultaValidator(ConsultaAccessService consultaAccessService) {
        this.consultaAccessService = consultaAccessService;
    }

    public void validarIdNoEnviadoEnCreacion(Long id) {
        if (id != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    public void validarIdNoCambiado(Long idExistente, Long idDto) {
        if (idDto != null && !idDto.equals(idExistente)) {
            throw new BusinessException("No se permite cambiar el id de la consulta");
        }
    }

    public void validarCamposObligatorios(ConsultaDTO dto) {
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

    public void validarCambioEstadoPermitido(Consulta existente, ConsultaDTO dto) {
        String estadoNuevo = normalizarTexto(dto.getEstado());

        if (!equalsIgnoreCase(existente.getEstado(), estadoNuevo)) {
            consultaAccessService.validarPuedeCambiarEstadoConsulta(existente.getId());
        }
    }
}