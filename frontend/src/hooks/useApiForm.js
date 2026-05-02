import { useState } from "react";
import { toast } from "sonner";

export function useApiForm({ endpoint }) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submit = async (data) => {
    setIsSubmitting(true);

    try {
      const response = await fetch(endpoint, {
        method: "POST",
        credentials: "include", // 🔥 CLAVE PARA ENVIAR LA COOKIE
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      // 🔥 Manejo de sesión
      if (response.status === 401) {
        toast.error("Sesión expirada", {
          description: "Debe iniciar sesión nuevamente",
        });

        window.location.href = "/login";
        return { success: false };
      }

      if (response.status === 403) {
        toast.error("No autorizado", {
          description: "No tiene permisos para esta acción",
        });

        return { success: false };
      }

      if (response.ok) {
        const result = await response.json();

        toast.success("Registro exitoso");

        return { success: true, data: result };
      } else {
        const errorData = await response.json();

        let errorMessage = "";

        for (const key in errorData) {
          errorMessage += `${key}: ${errorData[key]}\n`;
        }

        toast.error("Error en la operación", {
          description: errorMessage,
        });

        return { success: false, error: errorData };
      }
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