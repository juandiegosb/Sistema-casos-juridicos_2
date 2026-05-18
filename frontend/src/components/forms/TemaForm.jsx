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

export function TemaForm() {
  const router = useRouter();
  const [checking, setChecking] = useState(true);
  const [temas, setTemas] = useState([]);
  const [areas, setAreas] = useState([]);
  const [editandoId, setEditandoId] = useState(null);

  const [temaADesactivar, setTemaADesactivar] = useState(null);
  const [desactivando, setDesactivando] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm();

  const { submit, isSubmitting } = useApiForm({
    endpoint: `${API_URL_BASE}/temas`,
  });

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
          setAreas(
            areasData.map((area) => ({
              value: area.id.toString(),
              label: area.nombre,
            }))
          );
        }

        await cargarTemas();
      } catch (error) {
        router.push("/");
      } finally {
        setChecking(false);
      }
    };

    verificar();
  }, [router]);

  async function cargarTemas() {
    const res = await fetch(`${API_URL_BASE}/temas`, {
      credentials: "include",
    });

    const data = await res.json();
    setTemas(Array.isArray(data) ? data : []);
  }

  function abrirEditar(tema) {
    setEditandoId(tema.id);
    reset({
      nombre: tema.nombre,
      areaId: tema.areaId?.toString(),
    });
  }

  function cancelarEdicion() {
    setEditandoId(null);
    reset({ nombre: "", areaId: "" });
  }

  function abrirConfirmacionDesactivar(tema) {
    setTemaADesactivar(tema);
  }

  function cerrarConfirmacionDesactivar() {
    if (desactivando) return;
    setTemaADesactivar(null);
  }

  async function confirmarDesactivarTema() {
    if (!temaADesactivar?.id) return;

    try {
      setDesactivando(true);

      const res = await fetch(
        `${API_URL_BASE}/temas/${temaADesactivar.id}/desactivar`,
        {
          method: "PATCH",
          credentials: "include",
        }
      );

      if (res.ok || res.status === 204) {
        toast.success("Tema desactivado");
        setTemaADesactivar(null);
        cargarTemas();
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
      const res = await fetch(`${API_URL_BASE}/temas/${editandoId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(data),
      });

      if (res.ok) {
        toast.success("Tema actualizado");
        setEditandoId(null);
        reset({ nombre: "", areaId: "" });
        cargarTemas();
      } else {
        toast.error("Error al actualizar");
      }
    } else {
      await submit(data);
      reset({ nombre: "", areaId: "" });
      cargarTemas();
    }
  };

  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>;
  }

  return (
    <div className="space-y-6">
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

        <div className="flex gap-3 mt-2">
          <Button onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
            {isSubmitting
              ? "Guardando..."
              : editandoId
              ? "Actualizar tema"
              : "Guardar tema"}
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
              {["ID", "Nombre", "Área", "Estado", "Acciones"].map((h) => (
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
            {temas.length === 0 ? (
              <tr>
                <td
                  colSpan={5}
                  className="text-center py-8 text-sm text-muted-foreground"
                >
                  Sin temas registrados.
                </td>
              </tr>
            ) : (
              temas.map((tema) => (
                <tr key={tema.id} className="border-t hover:bg-muted/50">
                  <td className="px-4 py-3 text-sm">{tema.id}</td>

                  <td className="px-4 py-3 text-sm">{tema.nombre}</td>

                  <td className="px-4 py-3 text-sm">
                    {areas.find((a) => a.value === tema.areaId?.toString())
                      ?.label ?? tema.areaId}
                  </td>

                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium ${
                        tema.activo
                          ? "bg-green-100 text-green-600"
                          : "bg-gray-100 text-gray-500"
                      }`}
                    >
                      {tema.activo ? "Activo" : "Inactivo"}
                    </span>
                  </td>

                  <td className="px-4 py-3 text-sm">
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => abrirEditar(tema)}
                      >
                        Editar
                      </Button>

                      <Button
                        size="sm"
                        variant="destructive"
                        onClick={() => abrirConfirmacionDesactivar(tema)}
                        disabled={!tema.activo}
                      >
                        {tema.activo ? "Desactivar" : "Inactivo"}
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
        open={Boolean(temaADesactivar)}
        title="Desactivar tema"
        description={`¿Deseas desactivar el tema "${
          temaADesactivar?.nombre || "seleccionado"
        }"? Podrás reactivarlo después si es necesario.`}
        confirmText="Desactivar"
        cancelText="Cancelar"
        loading={desactivando}
        onClose={cerrarConfirmacionDesactivar}
        onConfirm={confirmarDesactivarTema}
      />
    </div>
  );
}