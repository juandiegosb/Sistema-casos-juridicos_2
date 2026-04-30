
"use client";

import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { FormCheckbox } from "./parts/FormCheckbox";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";

export function UsuarioSistemaForm() {
  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors }
  } = useForm();

  const [mounted, setMounted] = useState(false);
  const [tiposDocumento, setTiposDocumento] = useState([]);
  const [sedes, setSedes] = useState([]);
  const [areas, setAreas] = useState([]);
  const [asesores, setAsesores] = useState([]);

  const API_URL = "http://localhost:8080/api";
  const REQUIRED = "Campo obligatorio";

  // 🔥 Evita hydration error
  useEffect(() => {
    setMounted(true);
  }, []);

  // 🔥 Cargar tipos documento
  useEffect(() => {
    const cargarTipos = async () => {
      try {
        const res = await fetch(`${API_URL}/tipos-documento`);
        const data = await res.json();

        setTiposDocumento(
          data.map(t => ({
            value: t.id,
            label: t.displayName
          }))
        );
      } catch {
        toast.error("Error cargando tipos de documento");
      }
    };

    cargarTipos();
  }, []);

  // 🔥 Cargar datos base
  useEffect(() => {
    const cargar = async () => {
      try {
        const [sedesData, areasData, asesoresData] = await Promise.all([
          fetch(`${API_URL}/sedes`).then(r => r.json()),
          fetch(`${API_URL}/areas`).then(r => r.json()),
          fetch(`${API_URL}/asesores`).then(r => r.json())
        ]);

        setSedes(sedesData.map(s => ({ value: s.id, label: s.nombre })));
        setAreas(areasData.map(a => ({ value: a.id, label: a.nombre })));
        setAsesores(
          asesoresData.map(a => ({
            value: a.id,
            label: `${a.nombre} - ${a.documento}`
          }))
        );
      } catch {
        toast.error("Error cargando datos");
      }
    };

    cargar();
  }, []);

  if (!mounted) return null;

  const rol = watch("rol") || "";

  const roles = [
    { value: "administrativos", label: "Administrativo" },
    { value: "asesores", label: "Asesor" },
    { value: "conciliadores", label: "Conciliador" },
    { value: "estudiantes", label: "Estudiante" },
    { value: "monitores", label: "Monitor" }
  ];

  const normalizePayload = (data) => {
    const payload = {
      ...data,
      activo: true,
      tipoDocumentoId: Number(data.tipoDocumentoId),
      sedeId: Number(data.sedeId)
    };

    if (data.areaId) payload.areaId = Number(data.areaId);
    if (data.asesorId) payload.asesorId = Number(data.asesorId);

    return payload;
  };

  // 🔥 Submit
  const onSubmit = async (data) => {
    try {
      const endpoint = `${API_URL}/${rol}`;
      const tipoDocumentoSeleccionado = tiposDocumento.find(
        (tipo) => tipo.value === Number(data.tipoDocumentoId)
      );

      if (!tipoDocumentoSeleccionado) {
        toast.error("Seleccione un tipo de documento válido");
        return;
      }

      const payload = normalizePayload(data);

      const res = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (!res.ok) throw new Error();

      toast.success("Usuario creado correctamente");
      reset();
    } catch {
      toast.error("Error al crear usuario");
    }
  };

  // 🔹 Campos comunes
  const camposBase = (
    <>
      <FormInput name="nombre" label="Nombre" register={register} errors={errors} rules={{ required: REQUIRED }} />

      {tiposDocumento.length > 0 ? (
        <div className="flex flex-col gap-1.5 w-full">
          <label htmlFor="tipoDocumentoId" className="text-sm font-medium leading-none">
            Tipo de Documento
          </label>
          <select
            id="tipoDocumentoId"
            defaultValue=""
            {...register("tipoDocumentoId", {
              required: REQUIRED,
              valueAsNumber: true
            })}
            className={`flex h-8 w-full rounded-lg border px-2.5 py-1 ${errors?.tipoDocumentoId ? "border-red-500" : ""
              }`}
          >
            <option value="">Seleccione una opción</option>
            {tiposDocumento.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          {errors?.tipoDocumentoId && (
            <p className="text-xs text-red-500">{errors.tipoDocumentoId?.message}</p>
          )}
        </div>
      ) : null}

      <FormInput name="documento" label="Documento" register={register} errors={errors} rules={{ required: REQUIRED }} />

      <FormInput name="email" label="Email" register={register} errors={errors} rules={{ required: REQUIRED }} />

      <FormInput name="telefono" label="Teléfono" register={register} errors={errors} rules={{ required: REQUIRED }} />

      <FormInput name="usuario" label="Usuario" register={register} errors={errors} rules={{ required: REQUIRED }} />

      <FormInput name="codigo" label="Código" register={register} errors={errors} rules={{ required: REQUIRED }} />

      {sedes.length > 0 && (
        <FormSelect
          name="sedeId"
          label="Sede"
          options={sedes}
          register={register}
          errors={errors}
          rules={{ required: REQUIRED, valueAsNumber: true }}
        />
      )}
    </>
  );

  // 🔥 Campos por rol
  const renderCamposRol = () => {
    switch (rol) {
      case "asesores":
        return (
          <FormSelect
            name="areaId"
            label="Área"
            options={areas}
            register={register}
            errors={errors}
            rules={{ required: REQUIRED, valueAsNumber: true }}
          />
        );

      case "estudiantes":
        return (
          <>
            <FormSelect
              name="asesorId"
              label="Asesor"
              options={asesores}
              register={register}
              errors={errors}
              rules={{ required: REQUIRED, valueAsNumber: true }}
            />

            <FormCheckbox name="conciliacion" label="¿Conciliación?" register={register} />
          </>
        );

      case "administrativos":
        return (
          <FormCheckbox name="directora" label="¿Directora?" register={register} />
        );

      case "conciliadores":
        return (
          <FormSelect
            name="tipoConciliador"
            label="Tipo"
            options={[
              { value: "INTERNO", label: "Interno" },
              { value: "EXTERNO", label: "Externo" }
            ]}
            register={register}
            errors={errors}
            rules={{ required: REQUIRED }}
          />
        );

      default:
        return null;
    }
  };

  return (
    <div className="p-6 space-y-6 bg-card rounded-xl border shadow">
      <h2 className="text-xl font-bold">Crear Usuario</h2>

      <FormSelect
        name="rol"
        label="Tipo de Usuario"
        options={roles}
        register={register}
        errors={errors}
        rules={{ required: REQUIRED }}
      />

      {rol && (
        <>
          <Separator />

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {camposBase}
            {renderCamposRol()}
          </div>

          <div className="flex justify-end gap-4">
            <Button variant="outline" onClick={() => reset()}>
              Limpiar
            </Button>

            <Button onClick={handleSubmit(onSubmit)}>
              Crear Usuario
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
