"use client"

import React, { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { FormInput } from "./parts/FormInput"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"
import { API_URL_BASE } from "@/lib/config"

export function AreaForm() {
  const router = useRouter()

  const [checking, setChecking] = useState(true)
  const [error, setError] = useState("")

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

  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>
  }

  const onSubmit = async (data) => {
    setError("")

    try {
      const res = await fetch(`${API_URL_BASE}/areas`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
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

      const result = await res.json()

      if (!res.ok) {
        throw new Error(
          result?.mensaje ||
          result?.message ||
          "Error al guardar el área"
        )
      }

      reset()
      alert("Área creada correctamente")

    } catch (err) {
      setError(err.message)
    }
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

      {error && (
        <div className="text-sm text-destructive">{error}</div>
      )}

      <Button onClick={handleSubmit(onSubmit)}>
        Guardar área
      </Button>
    </div>
  )
}