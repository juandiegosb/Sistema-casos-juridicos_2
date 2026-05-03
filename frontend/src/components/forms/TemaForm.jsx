import React, { useState, useEffect } from "react";
import { useApiForm } from "@/hooks/useApiForm";
import { useForm } from "react-hook-form";
import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { API_URL_BASE } from "@/lib/config";

export function TemaForm() {
  const router = useRouter();
  const [checking, setChecking] = useState(true);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();

  const [areas, setAreas] = useState([]);
  const { submit, isSubmitting } = useApiForm({ endpoint: `${API_URL_BASE}/temas`})

  useEffect(() => {
    const verificar = async () => {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, {
          method: "GET",
          credentials: "include",
        });

        if (res.status === 401) {
          router.push("/");
          return;
        }

        const user = await res.json();

        if (!user.permisos?.includes("Gestionar catálogos")) {
          router.push("/inicio");
          return;
        }

        const response = await fetch(`${API_URL_BASE}/areas`, {
          credentials: "include",
        });

        if (response.status === 401) {
          router.push("/");
          return;
        }

        if (response.status === 403) {
          router.push("/inicio");
          return;
        }

        if (response.ok) {
          const areasData = await response.json();
          const areaOptions = areasData.map(area => ({
            value: area.id.toString(),
            label: area.nombre
          }));
          setAreas(areaOptions);
        }

      } catch (error) {
        router.push("/");
      } finally {
        setChecking(false);
      }
    };

    verificar();
  }, [router]);

  const onSubmit = async (data) => {
    await submit(data);
  };

  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>;
  }

  return (
    <div>
        <FormInput
            name="nombre"
            label="Nombre"
            register={register}
            errors={errors}
            rules={{ required: "El nombre es obligatorio" }}
        />
        <FormSelect
            name="areaId"
            label="Área"
            options={areas}
            register={register}
            errors={errors}
        />
        <Button className="my-2" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
            Guardar tema
        </Button>
    </div>
  )
}