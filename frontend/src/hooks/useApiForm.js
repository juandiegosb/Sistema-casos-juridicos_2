import { useState } from "react";
import { toast } from "sonner";
import {
  getApiErrorDescription,
  getApiErrorTitle,
  readResponseBody,
} from "@/lib/api";

export function useApiForm({ endpoint, method = "POST", successMessage = "Registro exitoso" }) {
  const [isSubmitting, setIsSubmitting] = useState(false);

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
