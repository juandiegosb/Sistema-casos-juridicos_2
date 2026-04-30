"use client"

import React, { useState } from "react"
import { useRouter } from "next/navigation"
import { useForm } from "react-hook-form"
import { useApiForm } from "@/hooks/useApiForm"
import { FormInput } from "../forms/parts/FormInput"
import { Button } from "@/components/ui/button"
import { Scale } from "lucide-react"

export function LoginForm() {
  const router = useRouter()
  const [errorMessage, setErrorMessage] = useState("")

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm()

  const { submit, isSubmitting } = useApiForm({
    endpoint: "http://localhost:8080/api/auth/login"
  })

  const REQUIRED = "Campo obligatorio"

  const handleSubmitForm = async (data) => {
    setErrorMessage("")

    try {
      const response = await submit({
        username: data.username,
        password: data.password
      })

      // 🔥 Si el login falló (status 400, 401, etc.)
      if (!response.success) {
        // Extraer el mensaje del error del backend
        const errorMsg = response.error?.mensaje || 
                         response.error?.message || 
                         response.error?.error ||
                         "Usuario o contraseña incorrectos"
        
        throw new Error(errorMsg)
      }

      // ✅ éxito → guardar datos
      const result = response.data
      localStorage.setItem("usuarioId", result.usuarioId)
      localStorage.setItem("username", result.username)
      localStorage.setItem("rol", result.rolNombre)
      localStorage.setItem("perfil", result.tipoPerfil)
      localStorage.setItem("permisos", JSON.stringify(result.permisos))

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
            disabled={isSubmitting}
          >
            {isSubmitting ? "Ingresando..." : "Acceder"}
          </Button>
        </form>
      </div>
    </div>
  )
}