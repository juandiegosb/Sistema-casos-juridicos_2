"use client"

import * as React from "react"
import { Button } from "@/components/ui/button"
import { Search, FileText } from "lucide-react"

export function ConsultasJuridicasForm() {
  const [searchText, setSearchText] = React.useState("")
  const [rows, setRows] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState("")

  async function handleSearch(event) {
    event.preventDefault()

    setLoading(true)
    setError("")

    try {
      const token = localStorage.getItem("token")

      const res = await fetch(
        `http://localhost:8080/consultas?search=${searchText}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      )

      if (!res.ok) throw new Error()

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

      {/* 🔍 BUSCADOR */}
      <form
        onSubmit={handleSearch}
        className="flex flex-col md:flex-row gap-4 md:items-end bg-muted/40 p-4 rounded-xl border"
      >
        <div className="flex-1 space-y-2">
          <label className="text-sm font-medium text-muted-foreground">
            Buscar consulta
          </label>

          <div className="relative">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-muted-foreground" />
            <input
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              placeholder="Ej: Derecho laboral, contrato..."
              className="
                w-full rounded-md border bg-background
                pl-9 pr-3 py-2 text-sm
                focus:outline-none focus:ring-2 focus:ring-primary/40
              "
            />
          </div>
        </div>

        <Button
          type="submit"
          disabled={loading}
          className="gap-2"
        >
          <Search className="w-4 h-4" />
          {loading ? "Buscando..." : "Buscar"}
        </Button>
      </form>

      {/* ❌ ERROR */}
      {error && (
        <div className="text-sm text-red-500 bg-red-500/10 border border-red-500/20 p-3 rounded-lg">
          {error}
        </div>
      )}

      {/* 📊 TABLA */}
      <div className="overflow-hidden rounded-xl border bg-card shadow-sm">

        {/* HEADER COLOR */}
        <div className="flex items-center gap-2 px-4 py-3 border-b bg-muted/50">
          <FileText className="w-5 h-5 text-primary" />
          <h3 className="font-semibold">Resultados de Consultas</h3>
        </div>

        <table className="min-w-full text-sm">

          <thead className="bg-muted/70 text-muted-foreground">
            <tr>
              <th className="px-4 py-3 text-left">ID</th>
              <th className="px-4 py-3 text-left">Consulta</th>
              <th className="px-4 py-3 text-left">Fecha</th>
              <th className="px-4 py-3 text-left">Nombre</th>
              <th className="px-4 py-3 text-left">Apellido</th>
              <th className="px-4 py-3 text-left">Cédula</th>
            </tr>
          </thead>

          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center py-10 text-muted-foreground">
                  {loading ? "Cargando..." : "Sin resultados"}
                </td>
              </tr>
            ) : (
              rows.map((row, index) => (
                <tr
                  key={row.id}
                  className="
                    border-t transition-colors
                    hover:bg-muted/50
                  "
                >
                  <td className="px-4 py-3 font-medium text-primary">
                    #{row.id}
                  </td>

                  <td className="px-4 py-3 max-w-xs truncate">
                    {row.consulta}
                  </td>

                  <td className="px-4 py-3 text-muted-foreground">
                    {row.fecha}
                  </td>

                  <td className="px-4 py-3">{row.nombre}</td>

                  <td className="px-4 py-3">{row.apellido}</td>

                  <td className="px-4 py-3">
                    <span className="px-2 py-1 rounded-md bg-muted text-xs">
                      {row.cedula}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}