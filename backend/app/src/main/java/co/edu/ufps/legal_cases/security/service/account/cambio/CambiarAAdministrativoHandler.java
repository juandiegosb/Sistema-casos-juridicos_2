package co.edu.ufps.legal_cases.security.service.account.cambio;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.model.perfil.Administrativo;
import co.edu.ufps.legal_cases.business.repository.perfil.AdministrativoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAAdministrativoDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.DatosPerfilCambioNormalizados;
import co.edu.ufps.legal_cases.security.dto.account.cambio.ResultadoCambioPerfil;
import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;

// Esto es un servicio concreto para:
// crear, actualizar o reactivar el perfil administrativo de un usuario que esta cambiando de rol
@Component
@Transactional
public class CambiarAAdministrativoHandler
        implements PerfilCambioHandler<CambiarPerfilAAdministrativoDTO> {

    private final AdministrativoRepository administrativoRepository;
    private final PerfilCambioDatosService perfilCambioDatosService;

    public CambiarAAdministrativoHandler(
            AdministrativoRepository administrativoRepository,
            PerfilCambioDatosService perfilCambioDatosService) {
        this.administrativoRepository = administrativoRepository;
        this.perfilCambioDatosService = perfilCambioDatosService;
    }

    @Override
    public TipoPerfilUsuario getTipoPerfil() {
        return TipoPerfilUsuario.ADMINISTRATIVO;
    }

    @Override
    public Class<CambiarPerfilAAdministrativoDTO> getDtoClass() {
        return CambiarPerfilAAdministrativoDTO.class;
    }

    @Override
    public ResultadoCambioPerfil crearOActualizarPerfil(
            UsuarioSistema usuarioSistema,
            CambiarPerfilAAdministrativoDTO dto) {
 
        DatosPerfilCambioNormalizados datos =
                perfilCambioDatosService.normalizarDatosBasicos(
                        dto,
                        usuarioSistema,
                        false
                );

        // Ya estan normalizados pero necesitamos traer los objetos del catalogo
        TipoDocumento tipoDocumento =
                perfilCambioDatosService.obtenerTipoDocumentoOpcional(dto.getTipoDocumentoId());

        Sede sede =
                perfilCambioDatosService.obtenerSedeOpcional(dto.getSedeId());

        // Aqui o se obtiene el que ya existe o se crea uno
        Administrativo administrativo = administrativoRepository
                .findByUsuarioSistema_Id(usuarioSistema.getId())
                .orElseGet(Administrativo::new);

        Long idActual = administrativo.getId();

        // Aqui se valida para el caso de que ya exista o sea nuevvo
        validarDuplicados(idActual, datos);

        administrativo.setUsuarioSistema(usuarioSistema);   // La id del mismo usuario del sistema (es la misma porque cambio de rol, no de cuenta en usuario del sistema)
        administrativo.setNombre(datos.getNombre());
        administrativo.setTipoDocumento(tipoDocumento);
        administrativo.setDocumento(datos.getDocumento());
        administrativo.setEmail(datos.getEmail());
        administrativo.setTelefono(datos.getTelefono());
        administrativo.setUsuario(datos.getUsuario());
        administrativo.setCodigo(datos.getCodigo());
        administrativo.setSede(sede);
        administrativo.setActivo(true);
        administrativo.setDirectora(dto.getDirectora() != null ? dto.getDirectora() : false);

        Administrativo administrativoGuardado = administrativoRepository.save(administrativo);

        // Luego de guardado envia solo lo necesario para decir que este perfil es ahora el activo
        return new ResultadoCambioPerfil(
                administrativoGuardado.getId(),
                TipoPerfilUsuario.ADMINISTRATIVO
        );
    }

    private void validarDuplicados(Long idActual, DatosPerfilCambioNormalizados datos) {
        if (idActual == null) {
            validarDuplicadosCreacion(datos);
            return;
        }

        validarDuplicadosActualizacion(idActual, datos);
    }

    // Aqui es estricto con los duplicados (porque no se pueden cruzar con una existente)
    private void validarDuplicadosCreacion(DatosPerfilCambioNormalizados datos) {
        if (datos.getDocumento() != null
                && administrativoRepository.existsByDocumento(datos.getDocumento())) {
            throw new BusinessException("Ya existe un administrativo con ese documento");
        }

        if (administrativoRepository.existsByEmailIgnoreCase(datos.getEmail())) {
            throw new BusinessException("Ya existe un administrativo con ese email");
        }

        if (administrativoRepository.existsByTelefono(datos.getTelefono())) {
            throw new BusinessException("Ya existe un administrativo con ese teléfono");
        }

        if (administrativoRepository.existsByUsuarioIgnoreCase(datos.getUsuario())) {
            throw new BusinessException("Ya existe un administrativo con ese usuario");
        }

        if (administrativoRepository.existsByCodigoIgnoreCase(datos.getCodigo())) {
            throw new BusinessException("Ya existe un administrativo con ese código");
        }
    }

    // Aqui como se usa en cada metodo el "AndIdNot" es para revisar los demas menos en el que esta
    // asi solo verifica para los demas pero pasa si mismo permite actualizar sin problema
    private void validarDuplicadosActualizacion(Long idActual, DatosPerfilCambioNormalizados datos) {
        if (datos.getDocumento() != null
                && administrativoRepository.existsByDocumentoAndIdNot(datos.getDocumento(), idActual)) {
            throw new BusinessException("Ya existe un administrativo con ese documento");
        }

        if (administrativoRepository.existsByEmailIgnoreCaseAndIdNot(datos.getEmail(), idActual)) {
            throw new BusinessException("Ya existe un administrativo con ese email");
        }

        if (administrativoRepository.existsByTelefonoAndIdNot(datos.getTelefono(), idActual)) {
            throw new BusinessException("Ya existe un administrativo con ese teléfono");
        }

        if (administrativoRepository.existsByUsuarioIgnoreCaseAndIdNot(datos.getUsuario(), idActual)) {
            throw new BusinessException("Ya existe un administrativo con ese usuario");
        }

        if (administrativoRepository.existsByCodigoIgnoreCaseAndIdNot(datos.getCodigo(), idActual)) {
            throw new BusinessException("Ya existe un administrativo con ese código");
        }
    }
}