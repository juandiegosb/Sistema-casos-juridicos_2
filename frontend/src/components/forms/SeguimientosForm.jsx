"use client"

import React, { useEffect, useMemo, useState } from "react"
import { useRouter } from "next/navigation"
import { useForm } from "react-hook-form"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { API_URL_BASE, FILE_STORAGE_API_URL_BASE } from "@/lib/config"
import { FormFileUpload } from "./parts/FormFileUpload"

const FORM_TAREA_INICIAL = {
  categoriaId: "",
  descripcion: "",
  fechaEntrega: "",
  alertaDisciplinaria: false,
}

const FORM_RESPUESTA_INICIAL = {
  descripcion: "",
  archivos: [],
}

function normalizar(value) {
  return String(value || "").trim().toUpperCase()
}

function esAdministrador(user) {
  return normalizar(user?.rolNombre) === "ADMINISTRADOR"
}

function esAsesor(user) {
  return normalizar(user?.rolNombre) === "ASESOR"
}

function esMonitor(user) {
  return normalizar(user?.rolNombre) === "MONITOR"
}

function esEstudiante(user) {
  return normalizar(user?.rolNombre) === "ESTUDIANTE"
}

function puedeCrearTarea(user) {
  return esAdministrador(user) || esAsesor(user)
}

function puedeEditarTarea(user) {
  return esAdministrador(user) || esAsesor(user) || esMonitor(user)
}

function puedeResponderTarea(user) {
  return esEstudiante(user)
}

function labelConsulta(consulta) {
  return [
    `#${consulta.id}`,
    consulta.consulta,
    consulta.nombre || consulta.apellido
      ? `${consulta.nombre || ""} ${consulta.apellido || ""}`.trim()
      : "",
    consulta.cedula,
  ]
    .filter(Boolean)
    .join(" - ")
}

function obtenerTextoTarea(item) {
  return (
    item.descripcion ||
    item.observacion ||
    item.detalle ||
    item.comentario ||
    "Sin descripción"
  )
}

function obtenerCategoriaTarea(item) {
  return (
    item.categoriaNombre ||
    item.categoriaSeguimientoNombre ||
    item.categoria?.nombre ||
    item.categoriaSeguimiento?.nombre ||
    item.categoria ||
    "Sin categoría"
  )
}

function obtenerCategoriaIdTarea(item) {
  return (
    item.categoriaSeguimientoId ||
    item.categoriaId ||
    item.categoria?.id ||
    item.categoriaSeguimiento?.id ||
    ""
  )
}

function obtenerAutorTarea(item) {
  return (
    item.autorNombre ||
    item.autorUsername ||
    item.autor ||
    item.username ||
    "Sin autor"
  )
}

function obtenerFechaTarea(item) {
  return (
    item.fechaCreacion ||
    item.fechaRegistro ||
    item.createdAt ||
    item.fecha ||
    ""
  )
}

function fechaHoyISO() {
  return new Date().toISOString().slice(0, 10)
}

function esRespuestaDeTarea(item) {
  return String(item.descripcion || "").startsWith("Respuesta a tarea #")
}

function obtenerTareaPadreId(item) {
  const descripcion = String(item.descripcion || "")
  const match = descripcion.match(/Respuesta a tarea #(\d+):/)
  return match ? Number(match[1]) : null
}

function pathRespuesta(tareaId, respuestaId) {
  return `tareas-${tareaId}-respuestas-${respuestaId}`
}

async function leerRespuesta(response) {
  const text = await response.text()

  if (!text) {
    return null
  }

  try {
    return JSON.parse(text)
  } catch {
    return { mensaje: text }
  }
}

export function SeguimientosForm() {
  const router = useRouter()
  const FILES_API = FILE_STORAGE_API_URL_BASE || API_URL_BASE

  const [user, setUser] = useState(null)
  const [consultas, setConsultas] = useState([])
  const [categorias, setCategorias] = useState([])
  const [tareas, setTareas] = useState([])
  const [consultaSeleccionada, setConsultaSeleccionada] = useState(null)
  const [busquedaLocal, setBusquedaLocal] = useState("")
  const [paso, setPaso] = useState(1)
  const [formTarea, setFormTarea] = useState(FORM_TAREA_INICIAL)
  const [tareaEditando, setTareaEditando] = useState(null)
  const [tareaRespuesta, setTareaRespuesta] = useState(null)
  const [loading, setLoading] = useState(true)
  const [loadingTareas, setLoadingTareas] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const [subiendoRespuesta, setSubiendoRespuesta] = useState(false)
  const [archivosPorRespuesta, setArchivosPorRespuesta] = useState({})
  const [cargandoArchivosRespuesta, setCargandoArchivosRespuesta] = useState({})
  const [tareaAEliminar, setTareaAEliminar] = useState(null)
  const [eliminando, setEliminando] = useState(false)

  const {
    register,
    setValue,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: FORM_RESPUESTA_INICIAL,
  })

  const archivosRespuesta = watch("archivos")

  const puedeCrear = useMemo(() => puedeCrearTarea(user), [user])
  const puedeEditar = useMemo(() => puedeEditarTarea(user), [user])
  const puedeResponder = useMemo(() => puedeResponderTarea(user), [user])

  const consultasFiltradas = useMemo(() => {
    const texto = busquedaLocal.trim().toLowerCase()

    if (!texto) {
      return consultas
    }

    return consultas.filter((consulta) =>
      [
        consulta.id,
        consulta.consulta,
        consulta.fecha,
        consulta.nombre,
        consulta.apellido,
        consulta.cedula,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(texto)
    )
  }, [consultas, busquedaLocal])

  useEffect(() => {
    cargarInicial()
  }, [])

  async function cargarInicial() {
    try {
      setLoading(true)

      const meRes = await fetch(`${API_URL_BASE}/auth/me`, {
        credentials: "include",
      })

      if (meRes.status === 401) {
        router.push("/")
        return
      }

      if (!meRes.ok) {
        toast.error("No se pudo verificar la sesión")
        return
      }

      const meData = await meRes.json()
      setUser(meData)

      const [categoriasRes, consultasRes] = await Promise.all([
        fetch(`${API_URL_BASE}/seguimientos/categorias/activas`, {
          credentials: "include",
        }),
        fetch(`${API_URL_BASE}/consultas?search=`, {
          credentials: "include",
        }),
      ])

      if (categoriasRes.status === 401 || consultasRes.status === 401) {
        router.push("/")
        return
      }

      if (categoriasRes.status === 403 || consultasRes.status === 403) {
        toast.error("No tienes permisos para consultar esta información")
        return
      }

      const categoriasData = categoriasRes.ok ? await categoriasRes.json() : []
      const consultasData = consultasRes.ok ? await consultasRes.json() : []

      setCategorias(Array.isArray(categoriasData) ? categoriasData : [])
      setConsultas(Array.isArray(consultasData) ? consultasData : [])
    } catch (error) {
      console.error(error)
      toast.error("Error cargando datos")
    } finally {
      setLoading(false)
    }
  }

  async function cargarTareasPorConsulta(consultaId) {
    try {
      setLoadingTareas(true)

      const res = await fetch(`${API_URL_BASE}/seguimientos/consulta/${consultaId}`, {
        credentials: "include",
      })

      if (res.status === 401) {
        router.push("/")
        return
      }

      if (res.status === 403) {
        toast.error("No tienes permisos para consultar las tareas")
        return
      }

      const data = await res.json()
      const tareasData = Array.isArray(data) ? data : []

      setTareas(tareasData)

      tareasData.forEach((item) => {
        if (esRespuestaDeTarea(item)) {
          const tareaPadreId = obtenerTareaPadreId(item)

          if (tareaPadreId) {
            cargarArchivosRespuesta(tareaPadreId, item.id)
          }
        }
      })
    } catch (error) {
      console.error(error)
      toast.error("Error cargando tareas")
    } finally {
      setLoadingTareas(false)
    }
  }

  async function seleccionarConsulta(consulta) {
    setConsultaSeleccionada(consulta)
    setPaso(2)
    setTareaEditando(null)
    setTareaRespuesta(null)
    setTareaAEliminar(null)
    setFormTarea(FORM_TAREA_INICIAL)
    reset(FORM_RESPUESTA_INICIAL)
    await cargarTareasPorConsulta(consulta.id)
  }

  function volverAConsultas() {
    setPaso(1)
    setConsultaSeleccionada(null)
    setTareas([])
    setTareaEditando(null)
    setTareaRespuesta(null)
    setTareaAEliminar(null)
    setFormTarea(FORM_TAREA_INICIAL)
    reset(FORM_RESPUESTA_INICIAL)
  }

  function handleTareaChange(event) {
    const { name, value, type, checked } = event.target

    setFormTarea((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }))
  }

  function limpiarFormTarea() {
    setFormTarea(FORM_TAREA_INICIAL)
    setTareaEditando(null)
  }

  function editarTarea(tarea) {
    setTareaEditando(tarea)
    setFormTarea({
      categoriaId: obtenerCategoriaIdTarea(tarea),
      descripcion: obtenerTextoTarea(tarea),
      fechaEntrega: tarea.fechaEntrega || "",
      alertaDisciplinaria: Boolean(tarea.alertaDisciplinaria),
    })
  }

  async function guardarTarea(event) {
    event.preventDefault()

    if (!puedeCrear && !tareaEditando) {
      toast.error("Solo administradores y asesores pueden crear tareas")
      return
    }

    if (!puedeEditar && tareaEditando) {
      toast.error("No tienes permisos para editar tareas")
      return
    }

    if (!consultaSeleccionada) {
      toast.error("Selecciona una consulta")
      return
    }

    if (!formTarea.categoriaId) {
      toast.error("Selecciona una categoría")
      return
    }

    if (!formTarea.descripcion.trim()) {
      toast.error("La descripción es obligatoria")
      return
    }

    try {
      setGuardando(true)

      const payload = {
        consultaId: Number(consultaSeleccionada.id),
        categoriaSeguimientoId: Number(formTarea.categoriaId),
        descripcion: formTarea.descripcion,
        fechaEntrega: formTarea.fechaEntrega || null,
        alertaDisciplinaria: Boolean(formTarea.alertaDisciplinaria),
      }

      const url = tareaEditando
        ? `${API_URL_BASE}/seguimientos/${tareaEditando.id}`
        : `${API_URL_BASE}/seguimientos`

      const method = tareaEditando ? "PUT" : "POST"

      const res = await fetch(url, {
        method,
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      })

      const data = await leerRespuesta(res)

      if (res.status === 401) {
        router.push("/")
        return
      }

      if (res.status === 403) {
        toast.error("No tienes permisos para guardar esta tarea")
        return
      }

      if (!res.ok) {
        throw new Error(data?.mensaje || data?.message || "No se pudo guardar la tarea")
      }

      toast.success(tareaEditando ? "Tarea actualizada" : "Tarea creada")
      limpiarFormTarea()
      await cargarTareasPorConsulta(consultaSeleccionada.id)
    } catch (error) {
      console.error(error)
      toast.error(error.message || "Error guardando tarea")
    } finally {
      setGuardando(false)
    }
  }

  function pedirConfirmacionEliminar(tarea) {
    if (!puedeEditar) {
      toast.error("No tienes permisos para eliminar tareas")
      return
    }

    setTareaAEliminar(tarea)
  }

  async function confirmarEliminarTarea() {
    if (!tareaAEliminar) return

    try {
      setEliminando(true)

      const res = await fetch(`${API_URL_BASE}/seguimientos/${tareaAEliminar.id}`, {
        method: "DELETE",
        credentials: "include",
      })

      if (res.status === 401) {
        router.push("/")
        return
      }

      if (res.status === 403) {
        toast.error("No tienes permisos para eliminar esta tarea")
        return
      }

      if (!res.ok && res.status !== 204) {
        const data = await leerRespuesta(res)
        throw new Error(data?.mensaje || "No se pudo eliminar")
      }

      toast.success("Tarea eliminada correctamente")
      setTareaAEliminar(null)

      if (consultaSeleccionada?.id) {
        await cargarTareasPorConsulta(consultaSeleccionada.id)
      }
    } catch (error) {
      console.error(error)
      toast.error(error.message || "Error eliminando tarea")
    } finally {
      setEliminando(false)
    }
  }

  function abrirRespuesta(tarea) {
    setTareaRespuesta(tarea)
    reset(FORM_RESPUESTA_INICIAL)
  }

  async function subirDocumentosRespuesta(tareaId, respuestaId, archivos) {
    if (!archivos || archivos.length === 0) {
      return
    }

    const formData = new FormData()

    archivos.forEach((file) => {
      formData.append("files", file)
    })

    formData.append("path", pathRespuesta(tareaId, respuestaId))

    const res = await fetch(`${FILES_API}/files/upload-multiple`, {
      method: "POST",
      credentials: "include",
      body: formData,
    })

    if (!res.ok) {
      throw new Error("La respuesta se guardó, pero ocurrió un error subiendo los documentos")
    }
  }

  async function cargarArchivosRespuesta(tareaId, respuestaId) {
    const path = pathRespuesta(tareaId, respuestaId)

    try {
      setCargandoArchivosRespuesta((prev) => ({
        ...prev,
        [respuestaId]: true,
      }))

      const res = await fetch(
        `${FILES_API}/files/list/${encodeURIComponent(path)}`,
        {
          credentials: "include",
        }
      )

      if (!res.ok) {
        setArchivosPorRespuesta((prev) => ({
          ...prev,
          [respuestaId]: [],
        }))
        return
      }

      const data = await res.json()

      setArchivosPorRespuesta((prev) => ({
        ...prev,
        [respuestaId]: Array.isArray(data) ? data : [],
      }))
    } catch (error) {
      console.error("Error cargando archivos de respuesta:", error)
      toast.error("No se pudieron cargar los archivos")

      setArchivosPorRespuesta((prev) => ({
        ...prev,
        [respuestaId]: [],
      }))
    } finally {
      setCargandoArchivosRespuesta((prev) => ({
        ...prev,
        [respuestaId]: false,
      }))
    }
  }

  async function descargarArchivoRespuesta(tareaId, respuestaId, fileName) {
    const path = pathRespuesta(tareaId, respuestaId)

    try {
      const res = await fetch(
        `${FILES_API}/files/download/${encodeURIComponent(path)}/${encodeURIComponent(fileName)}`,
        {
          method: "GET",
          credentials: "include",
        }
      )

      if (!res.ok) {
        throw new Error("No se pudo descargar el archivo")
      }

      const blob = await res.blob()
      const url = window.URL.createObjectURL(blob)

      const a = document.createElement("a")
      a.href = url
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      a.remove()

      window.URL.revokeObjectURL(url)
    } catch (error) {
      console.error("Error descargando archivo:", error)
      toast.error(error.message || "Error descargando archivo")
    }
  }

  async function guardarRespuesta(data) {
    if (!puedeResponder) {
      toast.error("Solo los estudiantes pueden responder tareas")
      return
    }

    if (!consultaSeleccionada || !tareaRespuesta) {
      toast.error("Selecciona una tarea")
      return
    }

    if (!data.descripcion?.trim()) {
      toast.error("Escribe la respuesta")
      return
    }

    try {
      setSubiendoRespuesta(true)

      const categoriaSeguimientoId = obtenerCategoriaIdTarea(tareaRespuesta)

      if (!categoriaSeguimientoId) {
        toast.error("No se pudo identificar la categoría de la tarea")
        return
      }

      const payload = {
        consultaId: Number(consultaSeleccionada.id),
        categoriaSeguimientoId: Number(categoriaSeguimientoId),
        descripcion: `Respuesta a tarea #${tareaRespuesta.id}: ${data.descripcion}`,
        fechaEntrega: fechaHoyISO(),
        alertaDisciplinaria: false,
      }

      const res = await fetch(`${API_URL_BASE}/seguimientos`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      })

      const result = await leerRespuesta(res)

      if (res.status === 401) {
        router.push("/")
        return
      }

      if (res.status === 403) {
        toast.error("No tienes permisos para responder esta tarea")
        return
      }

      if (!res.ok) {
        throw new Error(result?.mensaje || result?.message || "No se pudo guardar la respuesta")
      }

      const archivos = Array.isArray(data.archivos) ? data.archivos : []

      await subirDocumentosRespuesta(tareaRespuesta.id, result?.id || Date.now(), archivos)

      toast.success("Respuesta enviada correctamente")
      setTareaRespuesta(null)
      reset(FORM_RESPUESTA_INICIAL)
      await cargarTareasPorConsulta(consultaSeleccionada.id)
    } catch (error) {
      console.error(error)
      toast.error(error.message || "Error enviando respuesta")
    } finally {
      setSubiendoRespuesta(false)
    }
  }

  if (loading) {
    return <div className="text-center py-10">Cargando tareas...</div>
  }

  return (
    <div className="space-y-6">
      {paso === 1 && (
        <div className="rounded-xl border bg-card p-5 space-y-4">
          <div>
            <h2 className="text-xl font-bold">Seleccionar consulta</h2>
            <p className="text-sm text-muted-foreground">
              Selecciona una consulta para ver o gestionar sus tareas. La tabla muestra solo las consultas permitidas para tu usuario.
            </p>
          </div>

          <input
            value={busquedaLocal}
            onChange={(event) => setBusquedaLocal(event.target.value)}
            placeholder="Buscar por ID, descripción, nombre, apellido, cédula o fecha..."
            className="w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
          />

          <div className="overflow-hidden rounded-lg border">
            <table className="w-full text-sm">
              <thead className="bg-muted">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">ID</th>
                  <th className="px-4 py-3 text-left font-medium">Consulta</th>
                  <th className="px-4 py-3 text-left font-medium">Fecha</th>
                  <th className="px-4 py-3 text-left font-medium">Persona</th>
                  <th className="px-4 py-3 text-left font-medium">Cédula</th>
                  <th className="px-4 py-3 text-right font-medium">Acción</th>
                </tr>
              </thead>

              <tbody>
                {consultasFiltradas.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-4 py-8 text-center text-muted-foreground">
                      No hay consultas para mostrar.
                    </td>
                  </tr>
                ) : (
                  consultasFiltradas.map((consulta) => (
                    <tr
                      key={consulta.id}
                      onClick={() => seleccionarConsulta(consulta)}
                      className="border-t transition hover:bg-muted/40 cursor-pointer"
                    >
                      <td className="px-4 py-3">{consulta.id}</td>
                      <td className="px-4 py-3 max-w-[360px] truncate" title={consulta.consulta}>
                        {consulta.consulta || "Sin descripción"}
                      </td>
                      <td className="px-4 py-3">{consulta.fecha || "Sin fecha"}</td>
                      <td className="px-4 py-3">
                        {[consulta.nombre, consulta.apellido].filter(Boolean).join(" ") || "N/A"}
                      </td>
                      <td className="px-4 py-3">{consulta.cedula || "N/A"}</td>
                      <td className="px-4 py-3 text-right">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={(event) => {
                            event.stopPropagation()
                            seleccionarConsulta(consulta)
                          }}
                        >
                          Abrir tareas
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {paso === 2 && (
        <div className="space-y-6">
          <div className="rounded-xl border bg-card p-5 space-y-3">
            <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
              <div>
                <h2 className="text-xl font-bold">Tareas de la consulta</h2>
                <p className="text-sm text-muted-foreground">
                  {labelConsulta(consultaSeleccionada)}
                </p>
              </div>

              <Button type="button" variant="outline" onClick={volverAConsultas}>
                Cambiar consulta
              </Button>
            </div>
          </div>

          {(puedeCrear || tareaEditando) && (
            <form onSubmit={guardarTarea} className="rounded-xl border bg-card p-5 space-y-4">
              <div>
                <h3 className="font-semibold">
                  {tareaEditando ? "Editar tarea" : "Crear tarea"}
                </h3>
                <p className="text-sm text-muted-foreground">
                  {tareaEditando
                    ? "Actualiza la información de la tarea seleccionada."
                    : "Registra una tarea para la consulta seleccionada."}
                </p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-medium">Categoría</label>
                  <select
                    name="categoriaId"
                    value={formTarea.categoriaId}
                    onChange={handleTareaChange}
                    className="w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  >
                    <option value="">Seleccione una categoría</option>
                    {categorias.map((categoria) => (
                      <option key={categoria.id} value={categoria.id}>
                        {categoria.nombre}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-medium">Fecha de entrega</label>
                  <input
                    type="date"
                    name="fechaEntrega"
                    value={formTarea.fechaEntrega}
                    onChange={handleTareaChange}
                    className="w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium">Descripción</label>
                <textarea
                  name="descripcion"
                  value={formTarea.descripcion}
                  onChange={handleTareaChange}
                  rows={4}
                  placeholder="Describe la tarea, compromiso o actividad a realizar..."
                  className="w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
                />
              </div>

              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  name="alertaDisciplinaria"
                  checked={formTarea.alertaDisciplinaria}
                  onChange={handleTareaChange}
                  className="h-4 w-4"
                />
                Marcar como alerta disciplinaria
              </label>

              <div className="flex justify-end gap-3">
                {tareaEditando && (
                  <Button type="button" variant="outline" onClick={limpiarFormTarea}>
                    Cancelar edición
                  </Button>
                )}

                <Button type="submit" disabled={guardando}>
                  {guardando
                    ? "Guardando..."
                    : tareaEditando
                      ? "Actualizar tarea"
                      : "Crear tarea"}
                </Button>
              </div>
            </form>
          )}

          <div className="rounded-xl border bg-card p-5 space-y-4">
            <div className="flex items-center justify-between gap-4">
              <div>
                <h3 className="font-semibold">Tareas existentes</h3>
                <p className="text-sm text-muted-foreground">
                  Revisa las tareas asociadas a esta consulta.
                </p>
              </div>

              <Button
                type="button"
                variant="outline"
                onClick={() => cargarTareasPorConsulta(consultaSeleccionada.id)}
                disabled={loadingTareas}
              >
                Actualizar
              </Button>
            </div>

            {loadingTareas ? (
              <div className="rounded-lg border bg-muted/30 p-6 text-center text-sm text-muted-foreground">
                Cargando tareas...
              </div>
            ) : tareas.length === 0 ? (
              <div className="rounded-lg border bg-muted/30 p-6 text-center text-sm text-muted-foreground">
                No hay tareas registradas para esta consulta.
              </div>
            ) : (
              <div className="space-y-3">
                {tareas.map((tarea) => {
                  const esRespuesta = esRespuestaDeTarea(tarea)
                  const tareaPadreId = obtenerTareaPadreId(tarea)

                  return (
                    <div key={tarea.id} className="rounded-lg border bg-background p-4 space-y-3">
                      <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                        <div className="space-y-2">
                          <div className="flex flex-wrap items-center gap-2">
                            <span className="rounded-full border bg-muted px-2.5 py-1 text-xs font-medium">
                              {obtenerCategoriaTarea(tarea)}
                            </span>

                            {esRespuesta && (
                              <span className="rounded-full border border-primary/30 bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary">
                                Respuesta
                              </span>
                            )}

                            {tarea.alertaDisciplinaria && (
                              <span className="rounded-full border border-destructive/30 bg-destructive/10 px-2.5 py-1 text-xs font-medium text-destructive">
                                Alerta disciplinaria
                              </span>
                            )}
                          </div>

                          <p className="text-sm whitespace-pre-line">
                            {obtenerTextoTarea(tarea)}
                          </p>
                        </div>

                        <div className="flex flex-wrap gap-2">
                          {puedeEditar && (
                            <>
                              <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={() => editarTarea(tarea)}
                              >
                                Editar
                              </Button>

                              <Button
                                type="button"
                                variant="destructive"
                                size="sm"
                                onClick={() => pedirConfirmacionEliminar(tarea)}
                              >
                                Eliminar
                              </Button>
                            </>
                          )}

                          {puedeResponder && !esRespuesta && (
                            <Button
                              type="button"
                              size="sm"
                              onClick={() => abrirRespuesta(tarea)}
                            >
                              Responder
                            </Button>
                          )}
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs text-muted-foreground">
                        <div>
                          <span className="font-medium text-foreground">Autor: </span>
                          {obtenerAutorTarea(tarea)}
                        </div>

                        <div>
                          <span className="font-medium text-foreground">Registro: </span>
                          {obtenerFechaTarea(tarea) || "Sin fecha"}
                        </div>

                        <div>
                          <span className="font-medium text-foreground">
                            {esRespuesta ? "Fecha de entrega: " : "Entrega programada: "}
                          </span>
                          {tarea.fechaEntrega || "Sin fecha"}
                        </div>
                      </div>

                      {esRespuesta && (
                        <div className="rounded-lg border bg-muted/30 p-3 space-y-2">
                          <div className="flex items-center justify-between gap-3">
                            <p className="text-sm font-medium">Documentos entregados</p>

                            {tareaPadreId && (
                              <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={() => cargarArchivosRespuesta(tareaPadreId, tarea.id)}
                              >
                                Ver archivos
                              </Button>
                            )}
                          </div>

                          {cargandoArchivosRespuesta[tarea.id] ? (
                            <p className="text-sm text-muted-foreground">
                              Cargando archivos...
                            </p>
                          ) : archivosPorRespuesta[tarea.id]?.length > 0 ? (
                            <ul className="space-y-2">
                              {archivosPorRespuesta[tarea.id].map((fileName) => (
                                <li
                                  key={fileName}
                                  className="flex items-center justify-between gap-3 rounded-md border bg-background px-3 py-2 text-sm"
                                >
                                  <span className="truncate">{fileName}</span>

                                  <Button
                                    type="button"
                                    variant="outline"
                                    size="sm"
                                    onClick={() =>
                                      descargarArchivoRespuesta(
                                        tareaPadreId,
                                        tarea.id,
                                        fileName
                                      )
                                    }
                                  >
                                    Descargar
                                  </Button>
                                </li>
                              ))}
                            </ul>
                          ) : (
                            <p className="text-sm text-muted-foreground">
                              No hay documentos entregados.
                            </p>
                          )}
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            )}
          </div>

          {puedeResponder && tareaRespuesta && (
            <form
              onSubmit={handleSubmit(guardarRespuesta)}
              className="rounded-xl border bg-card p-5 space-y-4"
            >
              <div>
                <h3 className="font-semibold">Responder tarea</h3>
                <p className="text-sm text-muted-foreground">
                  Tarea #{tareaRespuesta.id}: {obtenerTextoTarea(tareaRespuesta)}
                </p>
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium">Respuesta</label>
                <textarea
                  {...register("descripcion")}
                  rows={4}
                  placeholder="Escribe tu respuesta..."
                  className="w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
                />
              </div>

              <FormFileUpload
                name="archivos"
                label="Documentos de soporte"
                multiple={true}
                setValue={setValue}
                value={archivosRespuesta}
                errors={errors}
              />

              <div className="flex justify-end gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setTareaRespuesta(null)
                    reset(FORM_RESPUESTA_INICIAL)
                  }}
                  disabled={subiendoRespuesta}
                >
                  Cancelar
                </Button>

                <Button type="submit" disabled={subiendoRespuesta}>
                  {subiendoRespuesta ? "Enviando..." : "Enviar respuesta"}
                </Button>
              </div>
            </form>
          )}
        </div>
      )}

      {tareaAEliminar && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
          <div className="w-full max-w-md rounded-xl border bg-background p-6 shadow-xl space-y-4">
            <div>
              <h3 className="text-lg font-semibold">
                Eliminar tarea
              </h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Esta acción no se puede deshacer. La tarea será eliminada permanentemente.
              </p>
            </div>

            <div className="rounded-lg border bg-muted/30 p-3 text-sm">
              <p className="font-medium">
                {obtenerCategoriaTarea(tareaAEliminar)}
              </p>
              <p className="mt-1 line-clamp-3 text-muted-foreground">
                {obtenerTextoTarea(tareaAEliminar)}
              </p>
            </div>

            <div className="flex justify-end gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={() => setTareaAEliminar(null)}
                disabled={eliminando}
              >
                Cancelar
              </Button>

              <Button
                type="button"
                variant="destructive"
                onClick={confirmarEliminarTarea}
                disabled={eliminando}
              >
                {eliminando ? "Eliminando..." : "Sí, eliminar"}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}