package co.edu.ufps.legal_cases.business.model.seguimiento;

public enum TipoNotificacionSeguimiento {

    // Para persona principal, partes y contrapartes
    PARTES,

    ESTUDIANTE,

    // Para notificar a administrativos
    ALERTA_DISCIPLINARIA,

    // Recordar a la persona que creo el seguimiento
    RECORDATORIO_AUTOR
}