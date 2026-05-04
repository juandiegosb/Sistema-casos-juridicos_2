"use client"

import React, { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { FormInput } from "./parts/FormInput"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"
import { API_URL_BASE } from "@/lib/config"
import { useApiForm } from "@/hooks/useApiForm"

export function AreaForm() {
  const router = useRouter()
  const [checking, setChecking] = useState(true)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: {
      nombre: "",
    },
  })

  const { submit, isSubmitting } = useApiForm({
    endpoint: `${API_URL_BASE}/areas`,
    onSuccess: () => {
      reset()
    },
  })

  useEffect(() => {
    async function verificar() {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, {
          method: "GET",
          credentials: "include",
        })

        if (res.status === 401) {
          router.push("/")
          return
        }

        const user = await res.json()

        if (!user.permisos?.includes("Gestionar catálogos")) {
          router.push("/inicio")
          return
        }

      } catch {
        router.push("/")
      } finally {
        setChecking(false)
      }
    }

    verificar()
  }, [router])

  const onSubmit = async (data) => {
    await submit(data)
  }

  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>
  }

  return (
    <div className="space-y-6 p-6 bg-card rounded-xl border">
      <div>
        <h2 className="text-2xl font-bold">Registro de Área</h2>
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

      <Button
        onClick={handleSubmit(onSubmit)}
        disabled={isSubmitting}
      >
        {isSubmitting ? "Guardando..." : "Guardar área"}
      </Button>
    </div>
  )
}