"use client"

import * as React from "react"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"

export function ConsultasJuridicasForm() {
  const router = useRouter()

  const [searchText, setSearchText] = React.useState("")
  const [rows, setRows] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState("")
  const [checking, setChecking] = React.useState(true)

  const API_URL = "http://localhost:8080/api"

  // Verificar sesión y permisos
  React.useEffect(() => {
    async function verificar() {
      try {
        const res = await fetch(`${API_URL}/auth/me`, {
          method: "GET",
          credentials: "include"
        })

        if (res.status === 401) {
          router.push("/")
          return
        }

        const user = await res.json()

        if (!user.permisos?.includes("Gestionar consultas")) {
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

  // evitar render mientras valida
  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>
  }

  // 🔍 Buscar consultas
  async function handleSearch(event) {
    event.preventDefault()

    setLoading(true)
    setError("")

    try {
      const res = await fetch(
        `${API_URL}/consultas?search=${searchText}`,
        {
          method: "GET",
          credentials: "include",
        }
      )

      if (res.status === 401) {
        router.push("/")
        return
      }

      if (res.status === 403) {
        router.push("/inicio")
        return
      }

      if (!res.ok) {
        throw new Error("Error al obtener datos")
      }

      const data = await res.json()
      setRows(data)

    } catch {
      setError("No se pudieron cargar las consultas")
      setRows([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <form onSubmit={handleSearch} className="grid gap-4 md:grid-cols-[1fr_auto] items-end">
        <div className="space-y-2">
          <label className="text-sm font-medium">
            Buscar consulta
          </label>
          <input
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            placeholder="Escribe tu búsqueda"
            className="w-full rounded-md border px-3 py-2 text-sm"
          />
        </div>
        <Button type="submit" disabled={loading}>
          {loading ? "Buscando..." : "Buscar"}
        </Button>
      </form>

      {error && <p className="text-red-500 text-sm">{error}</p>}

      <div className="overflow-hidden rounded-lg border bg-card">
        <table className="min-w-full">
          <thead className="bg-muted">
            <tr>
              <th className="px-4 py-3 text-left text-xs">ID</th>
              <th className="px-4 py-3 text-left text-xs">Consulta</th>
              <th className="px-4 py-3 text-left text-xs">Fecha</th>
              <th className="px-4 py-3 text-left text-xs">Nombre</th>
              <th className="px-4 py-3 text-left text-xs">Apellido</th>
              <th className="px-4 py-3 text-left text-xs">Cédula</th>
            </tr>
          </thead>

          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center py-6 text-sm">
                  {loading ? "Cargando..." : "Sin resultados"}
                </td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={row.id}>
                  <td className="px-4 py-3">{row.id}</td>
                  <td className="px-4 py-3">{row.consulta}</td>
                  <td className="px-4 py-3">{row.fecha}</td>
                  <td className="px-4 py-3">{row.nombre}</td>
                  <td className="px-4 py-3">{row.apellido}</td>
                  <td className="px-4 py-3">{row.cedula}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}