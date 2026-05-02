"use client"

import { useSearchParams } from "next/navigation"
import { RestablecerPasswordForm } from "@/components/auth/RestablecerPasswordForm"

export default function RestablecerPasswordPage() {
  const searchParams = useSearchParams()
  const token = searchParams.get("token")

  if (!token) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-red-500">Enlace inválido o incompleto</p>
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-background px-4">
      <RestablecerPasswordForm token={token} />
    </div>
  )
}