"use client"

import React, { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { FormInput } from "../parts/FormInput"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"
import { API_URL_BASE } from "@/lib/config"
import { toast } from "sonner"
import { ConfirmActionDialog } from "@/components/ui/ConfirmActionDialog"
import { PERMISOS } from "@/lib/permission"
import { tienePermiso } from "@/lib/authz"

export function AreaForm() {
  const router = useRouter()
  const [checking, setChecking] = useState(true)
  const [areas, setAreas] = useState([])
  const [editandoId, setEditandoId] = useState(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [confirmDialog, setConfirmDialog] = useState(null)
  const [confirmLoading, setConfirmLoading] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: { nombre: "" },
  })

  useEffect(() => {
    async function verificar() {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, {
          credentials: "include",
        })

        if (res.status === 401) {
          router.push("/")
          return
        }

        if (!res.ok) {
          router.push("/")
          return
        }

        const user = await res.json()

        if (!tienePermiso(user, PERMISOS.GESTIONAR_CATALOGOS)) {
          router.push("/inicio")
          return
        }

        await cargarAreas()
      } catch (error) {
        console.error(error)
        router.push("/")
      } finally {
        setChecking(false)
      }
    }

    verificar()
  }, [router])

  async function cargarAreas() {
    const res = await fetch(`${API_URL_BASE}/areas`, {
      credentials: "include",
    })

    if (res.status === 401) {
      router.push("/")
      return
    }

    if (res.status === 403) {
      router.push("/inicio")
      return
    }

    const data = await res.json()
    setAreas(Array.isArray(data) ? data : [])
  }

  function abrirEditar(area) {
    setEditandoId(area.id)
    reset({ nombre: area.nombre })
  }

  function cancelarEdicion() {
    setEditandoId(null)
    reset({ nombre: "" })
  }

  function abrirConfirmacionDesactivar(area) {
    setConfirmDialog(area)
  }

  async function confirmarDesactivarArea() {
    if (!confirmDialog?.id) return

    try {
      setConfirmLoading(true)

      const res = await fetch(
        `${API_URL_BASE}/areas/${confirmDialog.id}/activo?activo=false`,
        {
          method: "PATCH",
          credentials: "include",
        }
      )

      if (res.ok || res.status === 204) {
        toast.success("Área desactivada")
        setConfirmDialog(null)
        cargarAreas()
      } else {
        toast.error("Error al desactivar")
      }
    } catch (error) {
      console.error(error)
      toast.error("Error de conexión")
    } finally {
      setConfirmLoading(false)
    }
  }

  const onSubmit = async (data) => {
    setIsSubmitting(true)

    try {
      const url = editandoId
        ? `${API_URL_BASE}/areas/${editandoId}`
        : `${API_URL_BASE}/areas`

      const method = editandoId ? "PUT" : "POST"

      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(data),
      })

      if (res.status === 401) {
        router.push("/")
        return
      }

      if (res.status === 403) {
        router.push("/inicio")
        return
      }

      if (res.ok) {
        toast.success(editandoId ? "Área actualizada" : "Área creada")
        setEditandoId(null)
        reset({ nombre: "" })
        cargarAreas()
      } else {
        const err = await res.json()
        toast.error(err.message ?? "Error al guardar")
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  if (checking) return <div className="text-center mt-10">Cargando...</div>

  return (
    <div className="space-y-6">
      <div className="space-y-6 p-6 bg-card rounded-xl border">
        <div>
          <h2 className="text-2xl font-bold">
            {editandoId ? "Editar Área" : "Registro de Área"}
          </h2>
          <p className="text-muted-foreground">
            Complete la siguiente información
          </p>
        </div>

        <FormInput
          name="nombre"
          label="Nombre del área"
          register={register}
          errors={errors}
          rules={{ required: "El nombre es obligatorio" }}
        />

        <div className="flex gap-3">
          <Button onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
            {isSubmitting
              ? "Guardando..."
              : editandoId
              ? "Actualizar área"
              : "Guardar área"}
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
              {["ID", "Nombre", "Estado", "Acciones"].map((h) => (
                <th key={h} className="px-4 py-3 text-left text-xs font-medium">
                  {h}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {areas.length === 0 ? (
              <tr>
                <td
                  colSpan={4}
                  className="text-center py-8 text-sm text-muted-foreground"
                >
                  Sin áreas registradas.
                </td>
              </tr>
            ) : (
              areas.map((area) => (
                <tr key={area.id} className="border-t hover:bg-muted/50">
                  <td className="px-4 py-3 text-sm">{area.id}</td>
                  <td className="px-4 py-3 text-sm">{area.nombre}</td>
                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium ${
                        area.activo
                          ? "bg-green-100 text-green-600"
                          : "bg-gray-100 text-gray-500"
                      }`}
                    >
                      {area.activo ? "Activo" : "Inactivo"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => abrirEditar(area)}
                      >
                        Editar
                      </Button>

                      <Button
                        size="sm"
                        variant="destructive"
                        onClick={() => abrirConfirmacionDesactivar(area)}
                        disabled={!area.activo}
                      >
                        {area.activo ? "Desactivar" : "Inactivo"}
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
        open={Boolean(confirmDialog)}
        title="Desactivar área"
        description={`¿Deseas desactivar el área "${
          confirmDialog?.nombre || "seleccionada"
        }"? Podrás reactivarla después si es necesario.`}
        confirmText="Desactivar"
        cancelText="Cancelar"
        loading={confirmLoading}
        onClose={() => setConfirmDialog(null)}
        onConfirm={confirmarDesactivarArea}
      />
    </div>
  )
}