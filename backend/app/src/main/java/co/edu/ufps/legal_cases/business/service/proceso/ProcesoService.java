package co.edu.ufps.legal_cases.business.service.proceso;

import co.edu.ufps.legal_cases.business.dto.proceso.ProcesoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Departamento;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.proceso.Especialidad;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;
import co.edu.ufps.legal_cases.business.model.proceso.Proceso;
import co.edu.ufps.legal_cases.business.repository.catalogo.DepartamentoRepository;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.EspecialidadRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.OrganoControlRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.ProcesoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final ConsultaRepository consultaRepository;
    private final OrganoControlRepository organoControlRepository;
    private final EspecialidadRepository especialidadRepository;

    public ProcesoService(
            ProcesoRepository procesoRepository,
            DepartamentoRepository departamentoRepository,
            ConsultaRepository consultaRepository,
            OrganoControlRepository organoControlRepository,
            EspecialidadRepository especialidadRepository
    ) {
        this.procesoRepository = procesoRepository;
        this.departamentoRepository = departamentoRepository;
        this.consultaRepository = consultaRepository;
        this.organoControlRepository = organoControlRepository;
        this.especialidadRepository = especialidadRepository;
    }

    public List<ProcesoDTO> listar() {

        return procesoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public ProcesoDTO obtenerPorId(Long id) {

        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Proceso no encontrado con id: " + id
                        )
                );

        return convertirADTO(proceso);
    }

    public ProcesoDTO crear(ProcesoDTO dto) {

        if (
                dto.getNumeroRadicado() != null
                        && procesoRepository.existsByNumeroRadicado(
                        dto.getNumeroRadicado()
                )
        ) {

            throw new BusinessException(
                    "Ya existe un proceso con ese número de radicado"
            );
        }

        Departamento departamento = departamentoRepository
                .findById(dto.getDepartamentoId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Departamento no encontrado con id: "
                                        + dto.getDepartamentoId()
                        )
                );

        Consulta consulta = consultaRepository
                .findById(dto.getConsultaId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Consulta no encontrada con id: "
                                        + dto.getConsultaId()
                        )
                );

        OrganoControl organoControl = null;

        if (dto.getOrganoControlId() != null) {

            organoControl = organoControlRepository
                    .findById(dto.getOrganoControlId())
                    .orElseThrow(() ->
                            new BusinessException(
                                    "Órgano de control no encontrado con id: "
                                            + dto.getOrganoControlId()
                            )
                    );
        }

        Especialidad especialidad = null;

        if (dto.getEspecialidadId() != null) {

            especialidad = especialidadRepository
                    .findById(dto.getEspecialidadId())
                    .orElseThrow(() ->
                            new BusinessException(
                                    "Especialidad no encontrada con id: "
                                            + dto.getEspecialidadId()
                            )
                    );
        }

        if (
                especialidad != null
                        && organoControl != null
                        && !especialidad.getOrganoControl()
                        .getId()
                        .equals(organoControl.getId())
        ) {

            throw new BusinessException(
                    "La especialidad no pertenece al órgano de control seleccionado"
            );
        }

        Proceso proceso = new Proceso();

        proceso.setNumeroRadicado(dto.getNumeroRadicado());
        proceso.setDepartamento(departamento);
        proceso.setConsulta(consulta);
        proceso.setOrganoControl(organoControl);
        proceso.setEspecialidad(especialidad);

        proceso.setActivo(
                dto.getActivo() != null
                        ? dto.getActivo()
                        : true
        );

        Proceso guardado = procesoRepository.save(proceso);

        return convertirADTO(guardado);
    }

    public ProcesoDTO actualizar(Long id, ProcesoDTO dto) {

        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Proceso no encontrado con id: " + id
                        )
                );

        Departamento departamento = departamentoRepository
                .findById(dto.getDepartamentoId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Departamento no encontrado con id: "
                                        + dto.getDepartamentoId()
                        )
                );

        Consulta consulta = consultaRepository
                .findById(dto.getConsultaId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Consulta no encontrada con id: "
                                        + dto.getConsultaId()
                        )
                );

        OrganoControl organoControl = null;

        if (dto.getOrganoControlId() != null) {

            organoControl = organoControlRepository
                    .findById(dto.getOrganoControlId())
                    .orElseThrow(() ->
                            new BusinessException(
                                    "Órgano de control no encontrado con id: "
                                            + dto.getOrganoControlId()
                            )
                    );
        }

        Especialidad especialidad = null;

        if (dto.getEspecialidadId() != null) {

            especialidad = especialidadRepository
                    .findById(dto.getEspecialidadId())
                    .orElseThrow(() ->
                            new BusinessException(
                                    "Especialidad no encontrada con id: "
                                            + dto.getEspecialidadId()
                            )
                    );
        }

        if (
                especialidad != null
                        && organoControl != null
                        && !especialidad.getOrganoControl()
                        .getId()
                        .equals(organoControl.getId())
        ) {

            throw new BusinessException(
                    "La especialidad no pertenece al órgano de control seleccionado"
            );
        }

        boolean mismoNumeroRadicado =
                (proceso.getNumeroRadicado() == null && dto.getNumeroRadicado() == null)
                        || (
                        proceso.getNumeroRadicado() != null
                                && proceso.getNumeroRadicado()
                                .equals(dto.getNumeroRadicado())
                );

        boolean mismoDepartamento = proceso.getDepartamento()
                .getId()
                .equals(dto.getDepartamentoId());

        boolean mismaConsulta = proceso.getConsulta()
                .getId()
                .equals(dto.getConsultaId());

        boolean mismoOrganoControl =
                (
                        proceso.getOrganoControl() == null
                                && dto.getOrganoControlId() == null
                )
                        || (
                        proceso.getOrganoControl() != null
                                && proceso.getOrganoControl()
                                .getId()
                                .equals(dto.getOrganoControlId())
                );

        boolean mismaEspecialidad =
                (
                        proceso.getEspecialidad() == null
                                && dto.getEspecialidadId() == null
                )
                        || (
                        proceso.getEspecialidad() != null
                                && proceso.getEspecialidad()
                                .getId()
                                .equals(dto.getEspecialidadId())
                );

        boolean mismoActivo =
                proceso.getActivo().equals(dto.getActivo());

        if (
                mismoNumeroRadicado
                        && mismoDepartamento
                        && mismaConsulta
                        && mismoOrganoControl
                        && mismaEspecialidad
                        && mismoActivo
        ) {

            throw new BusinessException(
                    "No hay cambios para actualizar"
            );
        }

        if (
                dto.getNumeroRadicado() != null
                        && !dto.getNumeroRadicado()
                        .equals(proceso.getNumeroRadicado())
                        && procesoRepository.existsByNumeroRadicado(
                        dto.getNumeroRadicado()
                )
        ) {

            throw new BusinessException(
                    "Ya existe un proceso con ese número de radicado"
            );
        }


        proceso.setNumeroRadicado(dto.getNumeroRadicado());
        proceso.setDepartamento(departamento);
        proceso.setConsulta(consulta);
        proceso.setOrganoControl(organoControl);
        proceso.setEspecialidad(especialidad);
        proceso.setActivo(
                dto.getActivo() != null
                        ? dto.getActivo()
                        : true
        );

        Proceso actualizado = procesoRepository.save(proceso);

        return convertirADTO(actualizado);
    }

    public void eliminar(Long id) {

        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Proceso no encontrado con id: " + id
                        )
                );

        procesoRepository.delete(proceso);
    }

    private ProcesoDTO convertirADTO(Proceso proceso) {

        return new ProcesoDTO(
                proceso.getId(),
                proceso.getNumeroRadicado(),
                proceso.getDepartamento().getId(),
                proceso.getConsulta().getId(),

                proceso.getOrganoControl() != null
                        ? proceso.getOrganoControl().getId()
                        : null,

                proceso.getEspecialidad() != null
                        ? proceso.getEspecialidad().getId()
                        : null,
                proceso.getActivo()
        );
    }
}