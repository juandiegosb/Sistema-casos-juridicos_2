package co.edu.ufps.legal_cases.audit.aop.log;

import co.edu.ufps.legal_cases.audit.service.log.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspecto (AOP) que intercepta de forma transparente la ejecución de métodos marcados
 * con la anotación @Auditable. Su función es extraer el usuario autenticado del
 * contexto de seguridad y recolectar la información del evento para enviarla al
 * servicio de auditoría, sin acoplar la lógica de negocio a la de trazabilidad.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Auditable auditable, Object result) {
        String username = getUsername();
        String action = auditable.action();
        String entityName = auditable.entityName();
        
        // Intentar extraer el ID de la entidad si es posible
        String entityId = extractEntityId(result, joinPoint);
        
        String details = "Método ejecutado: " + joinPoint.getSignature().getName() + 
                         ". Argumentos: " + Arrays.toString(joinPoint.getArgs());

        auditLogService.logAction(username, action, entityName, entityId, details);
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SISTEMA";
    }

    private String extractEntityId(Object result, JoinPoint joinPoint) {
        // Lógica heurística para intentar sacar el ID de la entidad afectada.
        // Si el método retorna un objeto con getId(), lo tomamos.
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id != null) {
                    return id.toString();
                }
            } catch (Exception ignored) {
                // Si no tiene getId(), simplemente ignoramos.
            }
        }
        
        // Alternativamente, se podría buscar en los argumentos de entrada si es un update/delete.
        return null;
    }
}
