"use client"

import { useRouter } from "next/navigation"
import { ConciliadorForm } from "@/components/forms/ConciliadorForm"

export default function NuevoConciliador() {
  const router = useRouter()

  const handleSuccess = () => {
    router.push("/conciliadores")
  }

  return (
    <div className="p-6 lg:p-10">
      <div className="max-w-5xl mx-auto">
        <div className="rounded-2xl border border-border bg-background shadow-sm p-6 lg:p-8 relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-600 via-indigo-500 to-purple-600" />
          <ConciliadorForm onSuccess={handleSuccess} />
        </div>
      </div>
    </div>
  )
}