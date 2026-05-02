"use client"

import React, { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { useForm } from "react-hook-form"
import { FormInput } from "../forms/parts/FormInput"
import { Button } from "@/components/ui/button"
import { Scale } from "lucide-react"

export function LoginForm() {
  const router = useRouter()
  const [errorMessage, setErrorMessage] = useState("")
  const [checkingSession, setCheckingSession] = useState(true)

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm()

  const REQUIRED = "Campo obligatorio"

  // Verificar sesión al cargar
  useEffect(() => {
    async function verificarSesion() {
      try {
        const res = await fetch("http://localhost:8080/api/auth/me", {
          method: "GET",
          credentials: "include"
        })

        if (res.ok) {
          router.push("/inicio")
          return
        }
      } catch (err) {
        // ignorar error
      } finally {
        setCheckingSession(false)
      }
    }

    verificarSesion()
  }, [router])

  if (checkingSession) {
    return <div className="text-center mt-10">Cargando...</div>
  }

  // Login
  const handleSubmitForm = async (data) => {
    setErrorMessage("")

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          username: data.username,
          password: data.password
        })
      })

      const result = await response.json()

      if (!response.ok) {
        const errorMsg =
          result?.mensaje ||
          result?.message ||
          "Usuario o contraseña incorrectos"

        throw new Error(errorMsg)
      }


      router.push("/inicio")

    } catch (error) {
      setErrorMessage(error.message || "Error al iniciar sesión")
    }
  }

  return (
    <div className="w-full max-w-md">
      <div className="backdrop-blur-xl bg-card/80 border border-border rounded-2xl shadow-2xl p-8">

        <div className="mb-6 text-center space-y-2">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-primary/10">
            <Scale className="h-7 w-7 text-primary" />
          </div>

          <h1 className="text-2xl font-semibold">
            Consultorio Jurídico
          </h1>

          <p className="text-sm text-muted-foreground">
            Accede al sistema
          </p>
        </div>

        <form
          onSubmit={handleSubmit(handleSubmitForm)}
          className="space-y-4"
        >
          <FormInput
            name="username"
            label="Correo electrónico"
            register={register}
            errors={errors}
            rules={{ required: REQUIRED }}
          />

          <FormInput
            name="password"
            label="Contraseña"
            type="password"
            register={register}
            errors={errors}
            rules={{ required: REQUIRED }}
          />

          {errorMessage && (
            <div className="text-sm text-destructive">
              {errorMessage}
            </div>
          )}

          <Button
            type="submit"
            className="w-full"
            disabled={false}
          >
            Acceder
          </Button>

          {/* 🔥 Recuperar contraseña */}
          <div className="text-center mt-2">
            <button
              type="button"
              onClick={() => router.push("/recuperar-password")}
              className="text-sm text-primary hover:underline"
            >
              ¿Olvidaste tu contraseña?
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}