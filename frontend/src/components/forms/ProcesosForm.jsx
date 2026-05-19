"use client";

import React, { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { API_URL_BASE } from "@/lib/config";
import { PERMISOS } from "@/lib/permission";
import { tieneAlgunPermiso } from "@/lib/authz";

const PERMISOS_PROCESOS = {
  VER_PROCESOS: PERMISOS.VER_PROCESOS || "Ver procesos",
  GESTIONAR_PROCESOS: PERMISOS.GESTIONAR_PROCESOS || "Gestionar procesos",
  GESTIONAR_CONSULTAS_LEGACY: "Gestionar consultas",
};

const FORM_INICIAL = {
  id: null,
  numeroRadicado: "",
  departamentoId: "",
  consultaId: "",
  organoControlId: "",
  especialidadId: "",
  activo: true,
};

function extraerLista(data) {
  if (Array.isArray(data)) return data;
  if (!data || typeof data !== "object") return [];

  const claves = [
    "content",
    "data",
    "items",
    "rows",
    "departamentos",
    "organos",
    "organosControl",
    "especialidades",
    "consultas",
    "procesos",
    "resultado",
    "payload",
  ];

  for (const clave of claves) {
    const valor = data[clave];

    if (Array.isArray(valor)) return valor;

    if (valor && typeof valor === "object") {
      const interno = extraerLista(valor);
      if (interno.length > 0) return interno;
    }
  }

  return [];
}

async function leerRespuesta(response) {
  if (response.status === 204) return null;

  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return { mensaje: text };
  }
}

function mensajeError(payload, defecto) {
  if (!payload) return defecto;
  if (typeof payload === "string") return payload || defecto;

  return payload.mensaje || payload.message || payload.error || defecto;
}

async function apiGet(url) {
  const response = await fetch(url, { credentials: "include" });
  const payload = await leerRespuesta(response);

  if (response.status === 401) {
    const error = new Error("Sesión vencida. Inicia sesión nuevamente.");
    error.status = 401;
    throw error;
  }

  if (response.status === 403) {
    const error = new Error("No tienes permisos para consultar esta información.");
    error.status = 403;
    throw error;
  }

  if (!response.ok) {
    throw new Error(mensajeError(payload, "No se pudo consultar la información."));
  }

  return payload;
}

async function apiEnviar(url, options) {
  const response = await fetch(url, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(options?.headers || {}),
    },
    ...options,
  });

  const payload = await leerRespuesta(response);

  if (response.status === 401) {
    const error = new Error("Sesión vencida. Inicia sesión nuevamente.");
    error.status = 401;
    throw error;
  }

  if (response.status === 403) {
    const error = new Error("No tienes permisos para realizar esta acción.");
    error.status = 403;
    throw error;
  }

  if (!response.ok) {
    throw new Error(mensajeError(payload, "No se pudo procesar la solicitud."));
  }

  return payload;
}

function puedeAccederProcesos(user) {
  return tieneAlgunPermiso(user, [
    PERMISOS.ACCEDER_PROCESOS,
    PERMISOS_PROCESOS.VER_PROCESOS,
    PERMISOS_PROCESOS.GESTIONAR_PROCESOS,
  ]);
}

function puedeVerProcesos(user) {
  return tieneAlgunPermiso(user, [
    PERMISOS_PROCESOS.VER_PROCESOS,
    PERMISOS_PROCESOS.GESTIONAR_PROCESOS,
  ]);
}

function puedeGestionarProcesos(user) {
  return tieneAlgunPermiso(user, [PERMISOS_PROCESOS.GESTIONAR_PROCESOS]);
}

function puedeCargarCatalogos(user) {
  return tieneAlgunPermiso(user, [
    PERMISOS.VER_CATALOGOS,
    PERMISOS.GESTIONAR_CATALOGOS,
  ]);
}

function puedeCargarConsultas(user) {
  return tieneAlgunPermiso(user, [
    PERMISOS.VER_CONSULTAS,
    PERMISOS_PROCESOS.GESTIONAR_CONSULTAS_LEGACY,
  ]);
}

function ordenarActivosPrimero(lista) {
  return [...lista].sort((a, b) => {
    const activoA = a.activo === false ? 1 : 0;
    const activoB = b.activo === false ? 1 : 0;

    if (activoA !== activoB) return activoA - activoB;

    return String(a.nombre || a.descripcion || "").localeCompare(
      String(b.nombre || b.descripcion || ""),
      "es"
    );
  });
}

function crearMapa(lista) {
  return new Map(lista.map((item) => [Number(item.id), item]));
}

function nombreCatalogo(mapa, id, fallback) {
  const item = mapa.get(Number(id));

  return (
    item?.nombre ||
    item?.descripcion ||
    fallback ||
    (id ? `#${id}` : "Sin asignar")
  );
}

function labelCatalogo(item) {
  const nombre = item.nombre || item.descripcion || item.codigo || `#${item.id}`;
  return item.activo === false ? `${nombre} (Inactivo)` : nombre;
}

function labelConsulta(consulta) {
  const persona = consulta.persona || consulta.consultante || {};

  const nombrePersona = [
    consulta.nombre || consulta.personaNombre || persona.nombre,
    consulta.apellido || consulta.personaApellido || persona.apellido,
  ]
    .filter(Boolean)
    .join(" ");

  return [
    `#${consulta.id || consulta.consultaId}`,
    consulta.consulta || consulta.descripcion || consulta.hechos || consulta.asunto,
    nombrePersona,
    consulta.cedula || consulta.documento || persona.documento,
  ]
    .filter(Boolean)
    .join(" - ");
}

function normalizarPayload(form) {
  const numeroRadicado = String(form.numeroRadicado || "").trim();

  return {
    numeroRadicado: numeroRadicado || null,
    departamentoId: form.departamentoId ? Number(form.departamentoId) : null,
    consultaId: form.consultaId ? Number(form.consultaId) : null,
    organoControlId: form.organoControlId ? Number(form.organoControlId) : null,
    especialidadId: form.especialidadId ? Number(form.especialidadId) : null,
    activo: Boolean(form.activo),
  };
}

function procesoAForm(proceso) {
  return {
    id: proceso.id,
    numeroRadicado: proceso.numeroRadicado || "",
    departamentoId: proceso.departamentoId || "",
    consultaId: proceso.consultaId || "",
    organoControlId: proceso.organoControlId || "",
    especialidadId: proceso.especialidadId || "",
    activo: proceso.activo !== false,
  };
}

export function ProcesosForm() {
  const router = useRouter();

  const [checking, setChecking] = useState(true);
  const [cargando, setCargando] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [user, setUser] = useState(null);

  const [procesos, setProcesos] = useState([]);
  const [departamentos, setDepartamentos] = useState([]);
  const [organosControl, setOrganosControl] = useState([]);
  const [especialidades, setEspecialidades] = useState([]);
  const [consultas, setConsultas] = useState([]);

  const [busqueda, setBusqueda] = useState("");
  const [editando, setEditando] = useState(false);
  const [form, setForm] = useState(FORM_INICIAL);

  const puedeVer = puedeVerProcesos(user);
  const puedeGestionar = puedeGestionarProcesos(user);

  const mapaDepartamentos = useMemo(
    () => crearMapa(departamentos),
    [departamentos]
  );

  const mapaOrganos = useMemo(() => crearMapa(organosControl), [organosControl]);

  const mapaEspecialidades = useMemo(
    () => crearMapa(especialidades),
    [especialidades]
  );

  const mapaConsultas = useMemo(() => crearMapa(consultas), [consultas]);

  const especialidadesFiltradas = useMemo(() => {
    if (!form.organoControlId) return especialidades;

    return especialidades.filter(
      (especialidad) =>
        Number(especialidad.organoControlId) === Number(form.organoControlId)
    );
  }, [especialidades, form.organoControlId]);

  const procesosFiltrados = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    if (!texto) return procesos;

    return procesos.filter((proceso) => {
      const consulta = mapaConsultas.get(Number(proceso.consultaId));

      const valores = [
        proceso.id,
        proceso.numeroRadicado,
        nombreCatalogo(mapaDepartamentos, proceso.departamentoId),
        nombreCatalogo(mapaOrganos, proceso.organoControlId),
        nombreCatalogo(mapaEspecialidades, proceso.especialidadId),
        consulta ? labelConsulta(consulta) : proceso.consultaId,
        proceso.activo === false ? "inactivo" : "activo",
      ];

      return valores.some((valor) =>
        String(valor || "").toLowerCase().includes(texto)
      );
    });
  }, [
    busqueda,
    procesos,
    mapaConsultas,
    mapaDepartamentos,
    mapaOrganos,
    mapaEspecialidades,
  ]);

  useEffect(() => {
    verificarYCargar();
  }, []);

  function actualizarCampo(name, value) {
    setForm((prev) => ({
      ...prev,
      [name]: value,
      ...(name === "organoControlId" ? { especialidadId: "" } : {}),
    }));
  }

  async function verificarYCargar() {
    try {
      const usuarioActual = await apiGet(`${API_URL_BASE}/auth/me`);
      setUser(usuarioActual);

      if (!puedeAccederProcesos(usuarioActual)) {
        toast.error("No tienes permiso para acceder a procesos");
        router.push("/inicio");
        return;
      }

      await cargarDatos(usuarioActual);
    } catch (error) {
      console.error(error);

      if (error.status === 401) {
        router.push("/");
        return;
      }

      toast.error(error.message || "No se pudo cargar procesos");
      router.push("/inicio");
    } finally {
      setChecking(false);
    }
  }

  async function cargarDatos(usuarioActual = user) {
    try {
      setCargando(true);

      const verProcesos = puedeVerProcesos(usuarioActual);
      const catalogosPermitidos = puedeCargarCatalogos(usuarioActual);
      const consultasPermitidas = puedeCargarConsultas(usuarioActual);

      if (!verProcesos) {
        toast.error("No tienes permiso para ver procesos");
        return;
      }

      const [
        procesosRes,
        departamentosRes,
        organosRes,
        especialidadesRes,
        consultasRes,
      ] = await Promise.allSettled([
        apiGet(`${API_URL_BASE}/procesos`),
        catalogosPermitidos
          ? apiGet(`${API_URL_BASE}/departamentos`)
          : Promise.resolve([]),
        catalogosPermitidos
          ? apiGet(`${API_URL_BASE}/organos-control`)
          : Promise.resolve([]),
        catalogosPermitidos
          ? apiGet(`${API_URL_BASE}/especialidades`)
          : Promise.resolve([]),
        consultasPermitidas
          ? apiGet(`${API_URL_BASE}/consultas?page=0&size=500`)
          : Promise.resolve([]),
      ]);

      if (procesosRes.status === "fulfilled") {
        setProcesos(extraerLista(procesosRes.value));
      } else {
        throw procesosRes.reason;
      }

      if (departamentosRes.status === "fulfilled") {
        setDepartamentos(
          ordenarActivosPrimero(extraerLista(departamentosRes.value))
        );
      }

      if (organosRes.status === "fulfilled") {
        setOrganosControl(
          ordenarActivosPrimero(extraerLista(organosRes.value))
        );
      }

      if (especialidadesRes.status === "fulfilled") {
        setEspecialidades(
          ordenarActivosPrimero(extraerLista(especialidadesRes.value))
        );
      }

      if (consultasRes.status === "fulfilled") {
        setConsultas(extraerLista(consultasRes.value));
      }

      const erroresAuxiliares = [
        departamentosRes,
        organosRes,
        especialidadesRes,
        consultasRes,
      ]
        .filter((res) => res.status === "rejected")
        .map((res) => res.reason?.message)
        .filter(Boolean);

      if (erroresAuxiliares.length > 0) {
        toast.error(erroresAuxiliares[0]);
      }
    } finally {
      setCargando(false);
    }
  }

  function validarAntesDeGuardar() {
    const numeroRadicado = String(form.numeroRadicado || "").trim();

    if (!form.departamentoId) {
      toast.error("Selecciona un departamento");
      return false;
    }

    if (!form.consultaId) {
      toast.error("Selecciona una consulta");
      return false;
    }

    if (numeroRadicado && numeroRadicado.length !== 23) {
      toast.error("El número de radicado debe tener exactamente 23 caracteres");
      return false;
    }

    if (form.especialidadId && !form.organoControlId) {
      toast.error("Selecciona primero un órgano de control");
      return false;
    }

    return true;
  }

  function abrirEdicion(proceso) {
    if (!puedeGestionar) {
      toast.error("No tienes permiso para editar procesos");
      return;
    }

    setForm(procesoAForm(proceso));
    setEditando(true);
  }

  function cerrarEdicion() {
    setEditando(false);
    setForm(FORM_INICIAL);
  }

  async function guardarEdicion(event) {
    event.preventDefault();

    if (!puedeGestionar) {
      toast.error("No tienes permiso para editar procesos");
      return;
    }

    if (!validarAntesDeGuardar()) return;

    try {
      setGuardando(true);

      await apiEnviar(`${API_URL_BASE}/procesos/${form.id}`, {
        method: "PUT",
        body: JSON.stringify(normalizarPayload(form)),
      });

      toast.success("Proceso actualizado correctamente");
      cerrarEdicion();
      await cargarDatos();
    } catch (error) {
      console.error(error);

      if (error.status === 401) {
        router.push("/");
        return;
      }

      toast.error(error.message || "No se pudo actualizar el proceso");
    } finally {
      setGuardando(false);
    }
  }

  async function eliminarProceso(proceso) {
    if (!puedeGestionar) {
      toast.error("No tienes permiso para eliminar procesos");
      return;
    }

    const confirmado = window.confirm(
      `¿Seguro que deseas eliminar el proceso #${proceso.id}?`
    );

    if (!confirmado) return;

    try {
      await apiEnviar(`${API_URL_BASE}/procesos/${proceso.id}`, {
        method: "DELETE",
      });

      toast.success("Proceso eliminado correctamente");
      await cargarDatos();
    } catch (error) {
      console.error(error);

      if (error.status === 401) {
        router.push("/");
        return;
      }

      toast.error(error.message || "No se pudo eliminar el proceso");
    }
  }

  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>;
  }

  return (
    <div className="rounded-xl border bg-card p-6 shadow space-y-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-bold">Procesos</h2>
          <p className="text-sm text-muted-foreground">
            Consulta y gestiona los procesos registrados.
          </p>
        </div>

        {puedeGestionar && (
          <Button type="button" onClick={() => router.push("/nuevoproceso")}>
            Nuevo proceso
          </Button>
        )}
      </div>

      {!puedeVer ? (
        <Aviso>No tienes permiso para ver procesos.</Aviso>
      ) : (
        <>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <input
              value={busqueda}
              onChange={(event) => setBusqueda(event.target.value)}
              placeholder="Buscar por radicado, departamento, órgano o consulta..."
              className="h-9 w-full rounded-lg border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring sm:max-w-md"
            />

            <Button
              type="button"
              variant="outline"
              onClick={() => cargarDatos()}
              disabled={cargando}
            >
              {cargando ? "Actualizando..." : "Actualizar"}
            </Button>
          </div>

          <div className="overflow-x-auto rounded-lg border">
            <table className="w-full text-sm">
              <thead className="bg-muted/50 text-left">
                <tr>
                  <th className="px-3 py-2 font-medium">ID</th>
                  <th className="px-3 py-2 font-medium">Radicado</th>
                  <th className="px-3 py-2 font-medium">Departamento</th>
                  <th className="px-3 py-2 font-medium">Consulta</th>
                  <th className="px-3 py-2 font-medium">Órgano</th>
                  <th className="px-3 py-2 font-medium">Especialidad</th>
                  <th className="px-3 py-2 font-medium">Estado</th>
                  {puedeGestionar && (
                    <th className="px-3 py-2 font-medium">Acciones</th>
                  )}
                </tr>
              </thead>

              <tbody>
                {procesosFiltrados.length === 0 ? (
                  <tr>
                    <td
                      colSpan={puedeGestionar ? 8 : 7}
                      className="px-3 py-8 text-center text-muted-foreground"
                    >
                      No hay procesos para mostrar.
                    </td>
                  </tr>
                ) : (
                  procesosFiltrados.map((proceso) => {
                    const consulta = mapaConsultas.get(
                      Number(proceso.consultaId)
                    );

                    return (
                      <tr key={proceso.id} className="border-t align-top">
                        <td className="px-3 py-2">#{proceso.id}</td>

                        <td className="px-3 py-2">
                          {proceso.numeroRadicado || "Sin radicado"}
                        </td>

                        <td className="px-3 py-2">
                          {nombreCatalogo(
                            mapaDepartamentos,
                            proceso.departamentoId
                          )}
                        </td>

                        <td className="px-3 py-2 max-w-xs">
                          {consulta
                            ? labelConsulta(consulta)
                            : `Consulta #${proceso.consultaId}`}
                        </td>

                        <td className="px-3 py-2">
                          {proceso.organoControlId
                            ? nombreCatalogo(
                                mapaOrganos,
                                proceso.organoControlId
                              )
                            : "Sin órgano"}
                        </td>

                        <td className="px-3 py-2">
                          {proceso.especialidadId
                            ? nombreCatalogo(
                                mapaEspecialidades,
                                proceso.especialidadId
                              )
                            : "Sin especialidad"}
                        </td>

                        <td className="px-3 py-2">
                          <span className="rounded-full border px-2 py-0.5 text-xs">
                            {proceso.activo === false ? "Inactivo" : "Activo"}
                          </span>
                        </td>

                        {puedeGestionar && (
                          <td className="px-3 py-2">
                            <div className="flex flex-wrap gap-2">
                              <Button
                                type="button"
                                size="sm"
                                variant="outline"
                                onClick={() => abrirEdicion(proceso)}
                              >
                                Editar
                              </Button>

                              <Button
                                type="button"
                                size="sm"
                                variant="destructive"
                                onClick={() => eliminarProceso(proceso)}
                              >
                                Eliminar
                              </Button>
                            </div>
                          </td>
                        )}
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </>
      )}

      {editando && (
        <ModalEdicion
          form={form}
          actualizarCampo={actualizarCampo}
          departamentos={departamentos}
          consultas={consultas}
          organosControl={organosControl}
          especialidadesFiltradas={especialidadesFiltradas}
          onCerrar={cerrarEdicion}
          onGuardar={guardarEdicion}
          guardando={guardando}
        />
      )}
    </div>
  );
}

function ModalEdicion({
  form,
  actualizarCampo,
  departamentos,
  consultas,
  organosControl,
  especialidadesFiltradas,
  onCerrar,
  onGuardar,
  guardando,
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <form
        onSubmit={onGuardar}
        className="w-full max-w-3xl rounded-xl border bg-background p-6 shadow-lg space-y-5"
      >
        <div className="flex items-center justify-between gap-3">
          <div>
            <h3 className="text-lg font-semibold">Editar proceso #{form.id}</h3>
            <p className="text-sm text-muted-foreground">
              Actualiza la información del proceso seleccionado.
            </p>
          </div>

          <button
            type="button"
            onClick={onCerrar}
            className="text-xl text-muted-foreground hover:text-foreground"
          >
            ×
          </button>
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <CampoTexto
            label="Número de radicado"
            value={form.numeroRadicado}
            onChange={(value) => actualizarCampo("numeroRadicado", value)}
            placeholder="Opcional, exactamente 23 caracteres"
            maxLength={23}
          />

          <CampoSelect
            label="Departamento"
            value={form.departamentoId}
            onChange={(value) => actualizarCampo("departamentoId", value)}
            required
          >
            <option value="">Seleccione un departamento</option>
            {departamentos.map((departamento) => (
              <option key={departamento.id} value={departamento.id}>
                {labelCatalogo(departamento)}
              </option>
            ))}
          </CampoSelect>

          <CampoSelect
            label="Consulta"
            value={form.consultaId}
            onChange={(value) => actualizarCampo("consultaId", value)}
            required
          >
            <option value="">Seleccione una consulta</option>
            {consultas.map((consulta) => (
              <option
                key={consulta.id || consulta.consultaId}
                value={consulta.id || consulta.consultaId}
              >
                {labelConsulta(consulta)}
              </option>
            ))}
          </CampoSelect>

          <CampoSelect
            label="Órgano de control"
            value={form.organoControlId}
            onChange={(value) => actualizarCampo("organoControlId", value)}
          >
            <option value="">Sin órgano de control</option>
            {organosControl.map((organo) => (
              <option key={organo.id} value={organo.id}>
                {labelCatalogo(organo)}
              </option>
            ))}
          </CampoSelect>

          <CampoSelect
            label="Especialidad"
            value={form.especialidadId}
            onChange={(value) => actualizarCampo("especialidadId", value)}
            disabled={especialidadesFiltradas.length === 0}
          >
            <option value="">Sin especialidad</option>
            {especialidadesFiltradas.map((especialidad) => (
              <option key={especialidad.id} value={especialidad.id}>
                {labelCatalogo(especialidad)}
              </option>
            ))}
          </CampoSelect>

          <label className="flex items-center gap-2 rounded-lg border p-3 text-sm">
            <input
              type="checkbox"
              checked={form.activo}
              onChange={(event) =>
                actualizarCampo("activo", event.target.checked)
              }
            />
            Proceso activo
          </label>
        </div>

        <div className="flex justify-end gap-3">
          <Button
            type="button"
            variant="outline"
            onClick={onCerrar}
            disabled={guardando}
          >
            Cancelar
          </Button>

          <Button type="submit" disabled={guardando}>
            {guardando ? "Guardando..." : "Guardar cambios"}
          </Button>
        </div>
      </form>
    </div>
  );
}

function CampoTexto({ label, value, onChange, placeholder, maxLength }) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-sm font-medium">{label}</label>
      <input
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        maxLength={maxLength}
        className="h-9 rounded-lg border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring"
      />
    </div>
  );
}

function CampoSelect({ label, value, onChange, children, required, disabled }) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-sm font-medium">{label}</label>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        disabled={disabled}
        className="h-9 rounded-lg border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-60"
      >
        {children}
      </select>
    </div>
  );
}

function Aviso({ children }) {
  return (
    <div className="rounded-lg border bg-muted/30 p-3 text-sm text-muted-foreground">
      {children}
    </div>
  );
}