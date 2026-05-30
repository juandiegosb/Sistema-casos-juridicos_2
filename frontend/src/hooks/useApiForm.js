import { useState } from "react";
import { toast } from "sonner";
import {
  getApiErrorDescription,
  getApiErrorTitle,
  readResponseBody,
} from "@/lib/api";

/**
 * Hook para enviar formularios a una API y manejar el estado de envío.
 * @param {{endpoint:string, method?:string, successMessage?:string}} options
 * @param {string} options.endpoint URL de la API a la que se enviarán los datos.
 * @param {string} [options.method="POST"] Método HTTP usado para el envío.
 * @param {string} [options.successMessage="Registro exitoso"] Mensaje mostrado al guardar.
 * @returns {{submit: function(Object): Promise<{success:boolean, data?:any, error?:any}>, isSubmitting:boolean}}
 */
export function useApiForm({ endpoint, method = "POST", successMessage = "Registro exitoso" }) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  /**
   * Envía los datos al endpoint configurado y retorna el resultado.
   * @param {Object} data Datos del formulario que se enviarán.
   * @returns {Promise<{success:boolean, data?:any, error?:any}>}
   */
  const submit = async (data) => {
    setIsSubmitting(true);

    try {
      const response = await fetch(endpoint, {
        method,
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      const payload = await readResponseBody(response);

      if (response.status === 401) {
        toast.error("Sesión expirada", {
          description: "Debe iniciar sesión nuevamente",
        });

        window.location.href = "/";
        return { success: false, error: payload };
      }

      if (response.status === 403) {
        toast.error("No autorizado", {
          description: "No tiene permisos para esta acción",
        });

        return { success: false, error: payload };
      }

      if (response.ok) {
        toast.success(successMessage);
        return { success: true, data: payload };
      }

      toast.error(getApiErrorTitle(payload, "Error en la operación"), {
        description: getApiErrorDescription(payload),
      });

      return { success: false, error: payload };
    } catch (error) {
      console.error("Error de red:", error);

      toast.error("Error de conexión", {
        description: "Verifique que el backend esté disponible",
      });

      return { success: false, error };
    } finally {
      setIsSubmitting(false);
    }
  };

  return {
    submit,
    isSubmitting,
  };
}
