"use client";

import React, { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { API_URL_BASE } from "@/lib/config";
import { RotateCcw, RefreshCw } from "lucide-react";

const SECCIONES = [
  {
    id: "personas",
    titulo: "Personas",
    endpoint: "/personas",
    tipo: "persona",
    reactivar: "activo",
  },
  {
    id: "consultas",
    titulo: "Consultas",
    endpoint: "/consultas",
    tipo: "consulta",
    reactivar: "consulta",
  },
  {
    id: "usuarios",
    titulo: "Usuarios del sistema",
    endpoint: "/usuarios-sistema",
    tipo: "usuario",
    reactivar: "activo",
  },
  {
    id: "administrativos",
    titulo: "Administrativos",
    endpoint: "/administrativos",
    tipo: "perfil",
    reactivar: "activo",
  },
  {
    id: "asesores",
    titulo: "Asesores",
    endpoint: "/asesores",
    tipo: "perfil",
    reactivar: "activo",
  },
  {
    id: "estudiantes",
    titulo: "Estudiantes",
    endpoint: "/estudiantes",
    tipo: "perfil",
    reactivar: "activo",
  },
  {
    id: "monitores",
    titulo: "Monitores",
    endpoint: "/monitores",
    tipo: "perfil",
    reactivar: "activo",
  },
  {
    id: "conciliadores",
    titulo: "Conciliadores",
    endpoint: "/conciliadores",
    tipo: "perfil",
    reactivar: "activo",
  },
];

function normalizar(value) {
  return String(value || "").trim().toUpperCase();
}

function estaInactivo(item) {
  return item?.activo === false || normalizar(item?.estado) === "INACTIVO";
}

function estaArchivadaConsulta(item) {
  return (
    normalizar(item?.estado) === "ARCHIVADO" ||
    normalizar(item?.estado) === "ARCHIVADA" ||
    item?.activo === false
  );
}

function nombrePersona(item) {
  return (
    item?.nombre ||
    item?.nombreCompleto ||
    [item?.nombres, item?.apellidos].filter(Boolean).join(" ") ||
    item?.username ||
    item?.usuario ||
    "Sin nombre"
  );
}

function documentoPersona(item) {
  return item?.documento || item?.numeroDocumento || item?.cedula || "N/A";
}

function textoConsulta(item) {
  return (
    item?.consulta ||
    item?.descripcion ||
    item?.resumen ||
    item?.tramite ||
    `Consulta #${item?.id}`
  );
}

function detalleItem(item, tipo) {
  if (tipo === "consulta") {
    return item?.fecha || item?.estado || "Consulta archivada";
  }

  if (tipo === "usuario") {
    return item?.rolNombre || item?.tipoPerfil || "Usuario desactivado";
  }

  if (tipo === "persona") {
    return item?.tipoUsuario || item?.correo || item?.telefono || "Persona desactivada";
  }

  return item?.codigo || item?.usuario || item?.telefono || "Perfil desactivado";
}

async function leerRespuesta(response) {
  const text = await response.text();

  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return { mensaje: text };
  }
}

export function EliminacionForm() {
  const [seccionActiva, setSeccionActiva] = useState("personas");
  const [data, setData] = useState({});
  const [loading, setLoading] = useState(true);
  const [reactivando, setReactivando] = useState("");
  const [busqueda, setBusqueda] = useState("");

  const seccion = useMemo(() => {
    return SECCIONES.find((item) => item.id === seccionActiva);
  }, [seccionActiva]);

  const itemsActuales = useMemo(() => {
    const lista = data[seccionActiva] || [];
    const q = busqueda.trim().toLowerCase();

    const filtradosPorEstado = lista.filter((item) => {
      if (seccion?.tipo === "consulta") {
        return estaArchivadaConsulta(item);
      }

      return estaInactivo(item);
    });

    if (!q) return filtradosPorEstado;

    return filtradosPorEstado.filter((item) =>
      [
        item?.id,
        nombrePersona(item),
        documentoPersona(item),
        textoConsulta(item),
        detalleItem(item, seccion?.tipo),
        item?.estado,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(q)
    );
  }, [data, seccionActiva, busqueda, seccion]);

  useEffect(() => {
    cargarTodo();
  }, []);

  async function cargarTodo() {
    try {
      setLoading(true);

      const resultados = {};

      await Promise.all(
        SECCIONES.map(async (item) => {
          try {
            const res = await fetch(`${API_URL_BASE}${item.endpoint}`, {
              credentials: "include",
            });

            if (res.status === 401) {
              toast.error("La sesión expiró");
              resultados[item.id] = [];
              return;
            }

            if (res.status === 403) {
              resultados[item.id] = [];
              return;
            }

            if (!res.ok) {
              resultados[item.id] = [];
              return;
            }

            const json = await res.json();
            resultados[item.id] = Array.isArray(json) ? json : [];
          } catch (error) {
            console.error(`Error cargando ${item.titulo}`, error);
            resultados[item.id] = [];
          }
        })
      );

      setData(resultados);
    } finally {
      setLoading(false);
    }
  }

  async function reactivarItem(item) {
    if (!seccion) return;

    const confirmar = window.confirm(
      `¿Seguro que deseas reactivar "${nombrePersona(item)}"?`
    );

    if (!confirmar) return;

    try {
      setReactivando(`${seccion.id}-${item.id}`);

      if (seccion.reactivar === "consulta") {
        await reactivarConsulta(item);
      } else {
        await reactivarActivo(seccion.endpoint, item.id);
      }

      toast.success("Registro reactivado correctamente");
      await cargarTodo();
    } catch (error) {
      console.error(error);
      toast.error(error.message || "No se pudo reactivar el registro");
    } finally {
      setReactivando("");
    }
  }

  async function reactivarActivo(endpoint, id) {
    const res = await fetch(`${API_URL_BASE}${endpoint}/${id}/activo?activo=true`, {
      method: "PATCH",
      credentials: "include",
    });

    const data = await leerRespuesta(res);

    if (!res.ok) {
      throw new Error(
        data?.mensaje || data?.message || "No se pudo cambiar el estado"
      );
    }
  }

  async function reactivarConsulta(item) {
    const detalleRes = await fetch(`${API_URL_BASE}/consultas/${item.id}`, {
      credentials: "include",
    });

    const detalle = await leerRespuesta(detalleRes);

    if (!detalleRes.ok) {
      throw new Error(
        detalle?.mensaje || detalle?.message || "No se pudo cargar la consulta"
      );
    }

    const payload = {
      ...detalle,
      estado: "Activo",
      personaId: detalle.personaId ? Number(detalle.personaId) : null,
      sedeId: detalle.sedeId ? Number(detalle.sedeId) : null,
      areaId: detalle.areaId ? Number(detalle.areaId) : null,
      temaId: detalle.temaId ? Number(detalle.temaId) : null,
      tipoId: detalle.tipoId ? Number(detalle.tipoId) : null,
      asesorId: detalle.asesorId ? Number(detalle.asesorId) : null,
      monitorId: detalle.monitorId ? Number(detalle.monitorId) : null,
      estudianteId: detalle.estudianteId ? Number(detalle.estudianteId) : null,
      partesIds: Array.isArray(detalle.partesIds) ? detalle.partesIds : [],
      contrapartesIds: Array.isArray(detalle.contrapartesIds)
        ? detalle.contrapartesIds
        : [],
    };

    const res = await fetch(`${API_URL_BASE}/consultas/${item.id}`, {
      method: "PUT",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    const data = await leerRespuesta(res);

    if (!res.ok) {
      throw new Error(
        data?.mensaje || data?.message || "No se pudo reactivar la consulta"
      );
    }
  }

  const totalSeccion = itemsActuales.length;

  return (
    <div className="space-y-5">
      <div className="rounded-xl border bg-card p-5 space-y-4">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <h3 className="text-xl font-bold">Registros desactivados</h3>
            <p className="text-sm text-muted-foreground">
              Selecciona una sección para ver registros desactivados o archivados.
            </p>
          </div>

          <Button type="button" variant="outline" onClick={cargarTodo}>
            <RefreshCw className="mr-2 size-4" />
            Actualizar
          </Button>
        </div>

        <div className="grid grid-cols-2 gap-2 md:grid-cols-4">
          {SECCIONES.map((item) => {
            const total = (data[item.id] || []).filter((row) =>
              item.tipo === "consulta" ? estaArchivadaConsulta(row) : estaInactivo(row)
            ).length;

            return (
              <button
                key={item.id}
                type="button"
                onClick={() => {
                  setSeccionActiva(item.id);
                  setBusqueda("");
                }}
                className={`rounded-lg border px-3 py-2 text-sm font-medium transition ${
                  seccionActiva === item.id
                    ? "border-primary bg-primary text-primary-foreground"
                    : "bg-background hover:bg-muted"
                }`}
              >
                {item.titulo}
                <span className="ml-2 rounded-full bg-background/20 px-2 py-0.5 text-xs">
                  {total}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      <div className="rounded-xl border bg-card p-5 space-y-4">
        <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
          <div>
            <h3 className="text-lg font-bold">{seccion?.titulo}</h3>
            <p className="text-sm text-muted-foreground">
              {totalSeccion} registro(s) para recuperar.
            </p>
          </div>

          <div className="w-full md:max-w-sm">
            <label className="text-sm font-medium">Buscar</label>
            <input
              value={busqueda}
              onChange={(event) => setBusqueda(event.target.value)}
              placeholder="ID, nombre, documento o estado..."
              className="mt-1 h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>
        </div>

        <div className="overflow-hidden rounded-lg border">
          <div className="max-h-[560px] overflow-auto">
            <table className="w-full text-sm">
              <thead className="sticky top-0 bg-muted">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">ID</th>
                  <th className="px-4 py-3 text-left font-medium">Nombre</th>
                  <th className="px-4 py-3 text-left font-medium">
                    {seccion?.tipo === "consulta" ? "Consulta" : "Documento / Usuario"}
                  </th>
                  <th className="px-4 py-3 text-left font-medium">Detalle</th>
                  <th className="px-4 py-3 text-right font-medium">Acción</th>
                </tr>
              </thead>

              <tbody>
                {loading ? (
                  <tr>
                    <td
                      colSpan={5}
                      className="px-4 py-8 text-center text-muted-foreground"
                    >
                      Cargando registros...
                    </td>
                  </tr>
                ) : itemsActuales.length === 0 ? (
                  <tr>
                    <td
                      colSpan={5}
                      className="px-4 py-8 text-center text-muted-foreground"
                    >
                      No hay registros desactivados en esta sección.
                    </td>
                  </tr>
                ) : (
                  itemsActuales.map((item) => {
                    const key = `${seccion.id}-${item.id}`;
                    const isLoading = reactivando === key;

                    return (
                      <tr
                        key={key}
                        className="border-t transition hover:bg-muted/40"
                      >
                        <td className="px-4 py-3">{item.id}</td>

                        <td className="px-4 py-3">
                          <div className="font-medium">
                            {seccion?.tipo === "consulta"
                              ? item.nombre || nombrePersona(item)
                              : nombrePersona(item)}
                          </div>
                          <div className="text-xs text-muted-foreground">
                            Estado: {item.estado || (item.activo === false ? "Inactivo" : "N/A")}
                          </div>
                        </td>

                        <td className="px-4 py-3">
                          {seccion?.tipo === "consulta"
                            ? textoConsulta(item)
                            : documentoPersona(item)}
                        </td>

                        <td className="px-4 py-3">
                          {detalleItem(item, seccion?.tipo)}
                        </td>

                        <td className="px-4 py-3">
                          <div className="flex justify-end">
                            <Button
                              type="button"
                              size="sm"
                              onClick={() => reactivarItem(item)}
                              disabled={isLoading}
                            >
                              <RotateCcw className="mr-1 size-4" />
                              {isLoading ? "Reactivando..." : "Reactivar"}
                            </Button>
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}