"use client";

import React, { useState, useEffect } from "react";
import { useApiForm } from "@/hooks/useApiForm";
import { useForm } from "react-hook-form";
import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { API_URL_BASE } from "@/lib/config";

export function TipoForm() {
  const router = useRouter();
  const [checking, setChecking] = useState(true);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: {
      nombre: "",
      temaId: "",
    },
  });

  const [temas, setTemas] = useState([]);

  const { submit, isSubmitting } = useApiForm({
    endpoint: `${API_URL_BASE}/tipos`,
  });

  useEffect(() => {
    const verificarYCargar = async () => {
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

        const response = await fetch(`${API_URL_BASE}/temas`, {
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
          const temasData = await response.json();

          const temaOptions = temasData.map((tema) => ({
            value: tema.id,
            label: tema.nombre,
          }));

          setTemas(temaOptions);
        } else {
          console.error("Error al cargar temas");
        }
      } catch (error) {
        console.error("Error:", error);
        router.push("/");
      } finally {
        setChecking(false);
      }
    };

    verificarYCargar();
  }, [router]);

  const onSubmit = async (data) => {
    await submit({
      ...data,
      temaId: Number(data.temaId),
    });
  };

  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>;
  }

  return (
    <div className="space-y-6 p-6 bg-card rounded-xl border">
      <div>
        <h2 className="text-2xl font-bold">Registro de Tipo</h2>
        <p className="text-muted-foreground">
          Complete la siguiente información
        </p>
      </div>

      <FormInput
        name="nombre"
        label="Nombre del tipo"
        register={register}
        errors={errors}
        rules={{ required: "El nombre es obligatorio" }}
      />

      <FormSelect
        name="temaId"
        label="Tema"
        options={temas}
        register={register}
        errors={errors}
      />

      <Button onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
        {isSubmitting ? "Guardando..." : "Guardar tipo"}
      </Button>
    </div>
  );
}