"use client";

import React, { useState, useEffect } from "react";
import { useApiForm } from "@/hooks/useApiForm";
import { useForm } from "react-hook-form";
import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { API_URL_BASE } from "@/lib/config";
import { toast } from "sonner";
import { ConfirmActionDialog } from "@/components/ui/ConfirmActionDialog";
import { PERMISOS } from "@/lib/permission";
import { tienePermiso } from "@/lib/authz";

export function TipoForm() {
  const router = useRouter();
  const [checking, setChecking] = useState(true);
  const [temas, setTemas] = useState([]);
  const [tipos, setTipos] = useState([]);
  const [editandoId, setEditandoId] = useState(null);

  const [tipoADesactivar, setTipoADesactivar] = useState(null);
  const [desactivando, setDesactivando] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: { nombre: "", temaId: "" },
  });

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

        if (!res.ok) {
          router.push("/");
          return;
        }

        const user = await res.json();

        if (!tienePermiso(user, PERMISOS.GESTIONAR_CATALOGOS)) {
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

          setTemas(
            Array.isArray(temasData)
              ? temasData.map((tema) => ({
                  value: tema.id,
                  label: tema.nombre,
                }))
              : []
          );
        }

        await cargarTipos();
      } catch (error) {
        console.error("Error:", error);
        router.push("/");
      } finally {
        setChecking(false);
      }
    };

    verificarYCargar();
  }, [router]);

  async function cargarTipos() {
    const res = await fetch(`${API_URL_BASE}/tipos`, {
      credentials: "include",
    });

    const data = await res.json();
    setTipos(Array.isArray(data) ? data : []);
  }

  function abrirEditar(tipo) {
    setEditandoId(tipo.id);
    reset({
      nombre: tipo.nombre,
      temaId: tipo.temaId?.toString(),
    });
  }

  function cancelarEdicion() {
    setEditandoId(null);
    reset({ nombre: "", temaId: "" });
  }

  function abrirConfirmacionDesactivar(tipo) {
    setTipoADesactivar(tipo);
  }

  function cerrarConfirmacionDesactivar() {
    if (desactivando) return;
    setTipoADesactivar(null);
  }

  async function confirmarDesactivarTipo() {
    if (!tipoADesactivar?.id) return;

    try {
      setDesactivando(true);

      const res = await fetch(
        `${API_URL_BASE}/tipos/${tipoADesactivar.id}/desactivar`,
        {
          method: "PATCH",
          credentials: "include",
        }
      );

      if (res.ok || res.status === 204) {
        toast.success("Tipo desactivado");
        setTipoADesactivar(null);
        cargarTipos();
      } else {
        toast.error("Error al desactivar");
      }
    } catch (error) {
      console.error(error);
      toast.error("Error de conexión");
    } finally {
      setDesactivando(false);
    }
  }

  const onSubmit = async (data) => {
    if (editandoId) {
      const res = await fetch(`${API_URL_BASE}/tipos/${editandoId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ ...data, temaId: Number(data.temaId) }),
      });

      if (res.ok) {
        toast.success("Tipo actualizado");
        setEditandoId(null);
        reset({ nombre: "", temaId: "" });
        cargarTipos();
      } else {
        toast.error("Error al actualizar");
      }
    } else {
      await submit({ ...data, temaId: Number(data.temaId) });
      reset({ nombre: "", temaId: "" });
      cargarTipos();
    }
  };

  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="space-y-6 p-6 bg-card rounded-xl border">
        <div>
          <h2 className="text-2xl font-bold">
            {editandoId ? "Editar Tipo" : "Registro de Tipo"}
          </h2>
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

        <div className="flex gap-3">
          <Button onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
            {isSubmitting
              ? "Guardando..."
              : editandoId
              ? "Actualizar tipo"
              : "Guardar tipo"}
          </Button>

          {editandoId && (
            <Button variant="outline" type="button" onClick={cancelarEdicion}>
              Cancelar
            </Button>
          )}
        </div>
      </div>

      <div className="overflow-hidden rounded-lg border bg-card">
        <table className="min-w-full">
          <thead className="bg-muted">
            <tr>
              {["ID", "Nombre", "Tema", "Estado", "Acciones"].map((h) => (
                <th
                  key={h}
                  className="px-4 py-3 text-left text-xs font-medium"
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {tipos.length === 0 ? (
              <tr>
                <td
                  colSpan={5}
                  className="text-center py-8 text-sm text-muted-foreground"
                >
                  Sin tipos registrados.
                </td>
              </tr>
            ) : (
              tipos.map((tipo) => (
                <tr key={tipo.id} className="border-t hover:bg-muted/50">
                  <td className="px-4 py-3 text-sm">{tipo.id}</td>

                  <td className="px-4 py-3 text-sm">{tipo.nombre}</td>

                  <td className="px-4 py-3 text-sm">
                    {temas.find(
                      (t) => Number(t.value) === Number(tipo.temaId)
                    )?.label ?? tipo.temaId}
                  </td>

                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium ${
                        tipo.activo
                          ? "bg-green-100 text-green-600"
                          : "bg-gray-100 text-gray-500"
                      }`}
                    >
                      {tipo.activo ? "Activo" : "Inactivo"}
                    </span>
                  </td>

                  <td className="px-4 py-3 text-sm">
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => abrirEditar(tipo)}
                      >
                        Editar
                      </Button>

                      <Button
                        size="sm"
                        variant="destructive"
                        onClick={() => abrirConfirmacionDesactivar(tipo)}
                        disabled={!tipo.activo}
                      >
                        {tipo.activo ? "Desactivar" : "Inactivo"}
                      </Button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <ConfirmActionDialog
        open={Boolean(tipoADesactivar)}
        title="Desactivar tipo"
        description={`¿Deseas desactivar el tipo "${
          tipoADesactivar?.nombre || "seleccionado"
        }"? Podrás reactivarlo después si es necesario.`}
        confirmText="Desactivar"
        cancelText="Cancelar"
        loading={desactivando}
        onClose={cerrarConfirmacionDesactivar}
        onConfirm={confirmarDesactivarTipo}
      />
    </div>
  );
}