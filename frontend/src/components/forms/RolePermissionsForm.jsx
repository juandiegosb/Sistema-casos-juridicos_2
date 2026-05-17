"use client";

import React, { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { API_URL_BASE } from "@/lib/config";

const PAGINAS = [
  {
    title: "Inicio",
    path: "/inicio",
    authOnly: true,
    permisoAcceso: "Acceder inicio",
    permisosFallback: [],
  },
  {
    title: "Tareas",
    path: "/tareas",
    permisoAcceso: "Acceder tareas",
    permisosFallback: [],
    allowedRoles: ["Asesor", "Administrador", "Estudiante"],
  },
  {
    title: "Nueva consulta",
    path: "/nuevaconsulta",
    permisoAcceso: "Acceder nueva consulta",
    permisosFallback: ["Gestionar consultas"],
  },
  {
    title: "Consultas jurídicas",
    path: "/consultasjuridicas",
    permisoAcceso: "Acceder consultas jurídicas",
    permisosFallback: ["Gestionar consultas"],
  },
  {
    title: "Administración",
    path: "/admin",
    permisoAcceso: "Acceder administración",
    permisosFallback: ["Gestionar catálogos", "Gestionar permisos"],
    match: "any",
  },
  {
    title: "Recepción",
    path: "/recepcion",
    permisoAcceso: "Acceder recepción",
    permisosFallback: [],
  },
  {
    title: "Roles",
    path: "/roles",
    permisoAcceso: "Acceder roles",
    permisosFallback: ["Gestionar usuarios"],
  },
  {
    title: "Estudiantes",
    path: "/estudiantes",
    permisoAcceso: "Acceder estudiantes",
    permisosFallback: ["Gestionar usuarios"],
  },
  {
    title: "Asesores y monitores",
    path: "/asesoresymonitores",
    permisoAcceso: "Acceder asesores y monitores",
    permisosFallback: ["Gestionar usuarios"],
  },
];

function normalizar(value) {
  return String(value || "").trim().toUpperCase();
}

function nombrePermiso(permiso) {
  if (typeof permiso === "string") return permiso;

  return (
    permiso?.nombre ||
    permiso?.nombrePermiso ||
    permiso?.descripcion ||
    permiso?.permiso ||
    ""
  );
}

function idPermiso(permiso) {
  return permiso?.id ?? permiso?.permisoId;
}

function nombreRol(rol) {
  return rol?.nombre || rol?.rolNombre || rol?.name || "";
}

function obtenerPermisosUsuario(user) {
  if (!Array.isArray(user?.permisos)) return [];

  return user.permisos.map(nombrePermiso).filter(Boolean);
}

function tieneTodos(userPermissions, requiredPermissions = []) {
  const permisos = userPermissions.map(normalizar);

  return requiredPermissions.every((permission) =>
    permisos.includes(normalizar(permission))
  );
}

function tieneAlguno(userPermissions, requiredPermissions = []) {
  const permisos = userPermissions.map(normalizar);

  return requiredPermissions.some((permission) =>
    permisos.includes(normalizar(permission))
  );
}

function tieneRolPermitido(user, allowedRoles = []) {
  if (!Array.isArray(allowedRoles) || allowedRoles.length === 0) return true;

  const rolActual = normalizar(user?.rolNombre);
  const roles = allowedRoles.map(normalizar);

  return roles.includes(rolActual);
}

function crearMapaPermisos(permisos) {
  const map = new Map();

  permisos.forEach((permiso) => {
    map.set(normalizar(nombrePermiso(permiso)), permiso);
  });

  return map;
}

function permisosPagina(page, permisosMap) {
  const permisoAccesoExiste = permisosMap.has(normalizar(page.permisoAcceso));

  if (permisoAccesoExiste) {
    return [page.permisoAcceso];
  }

  return page.permisosFallback || [];
}

function puedeVerPagina(page, user, permisosMap) {
  if (!user) return false;

  if (!tieneRolPermitido(user, page.allowedRoles)) {
    return false;
  }

  const requiredPermissions = permisosPagina(page, permisosMap);

  if (page.authOnly && requiredPermissions.length === 0) {
    return true;
  }

  if (requiredPermissions.length === 0) {
    return true;
  }

  const userPermissions = obtenerPermisosUsuario(user);

  if (page.match === "any") {
    return tieneAlguno(userPermissions, requiredPermissions);
  }

  return tieneTodos(userPermissions, requiredPermissions);
}

function paginaMarcada(page, permisosRol, permisosMap) {
  const requiredPermissions = permisosPagina(page, permisosMap);

  if (requiredPermissions.length === 0) {
    return false;
  }

  if (page.match === "any") {
    return tieneAlguno(permisosRol, requiredPermissions);
  }

  return tieneTodos(permisosRol, requiredPermissions);
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

export function RolePermissionsForm() {
  const [me, setMe] = useState(null);
  const [roles, setRoles] = useState([]);
  const [permisos, setPermisos] = useState([]);
  const [rolId, setRolId] = useState("");
  const [permisosActualesRol, setPermisosActualesRol] = useState([]);
  const [paginasSeleccionadas, setPaginasSeleccionadas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingRol, setLoadingRol] = useState(false);
  const [saving, setSaving] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const permisosMap = useMemo(() => crearMapaPermisos(permisos), [permisos]);

  const paginasVisibles = useMemo(() => {
    return PAGINAS.filter((page) => puedeVerPagina(page, me, permisosMap));
  }, [me, permisosMap]);

  useEffect(() => {
    cargarDatosIniciales();
  }, []);

  useEffect(() => {
    if (rolId) {
      cargarRol(rolId);
    } else {
      setPermisosActualesRol([]);
      setPaginasSeleccionadas([]);
    }
  }, [rolId, permisos]);

  async function cargarDatosIniciales() {
    try {
      setLoading(true);
      setError("");
      setMensaje("");

      const [meRes, rolesRes, permisosRes] = await Promise.all([
        fetch(`${API_URL_BASE}/auth/me`, {
          method: "GET",
          credentials: "include",
        }),
        fetch(`${API_URL_BASE}/roles/activos`, {
          credentials: "include",
        }),
        fetch(`${API_URL_BASE}/permisos/activos`, {
          credentials: "include",
        }),
      ]);

      if (meRes.status === 401) {
        throw new Error("La sesión expiró");
      }

      if (!meRes.ok) {
        throw new Error("No se pudo cargar el usuario actual");
      }

      if (!rolesRes.ok) {
        throw new Error("No se pudieron cargar los roles");
      }

      if (!permisosRes.ok) {
        throw new Error("No se pudieron cargar los permisos");
      }

      const meData = await meRes.json();
      const rolesData = await rolesRes.json();
      const permisosData = await permisosRes.json();

      setMe(meData);
      setRoles(Array.isArray(rolesData) ? rolesData : []);
      setPermisos(Array.isArray(permisosData) ? permisosData : []);
    } catch (err) {
      console.error(err);
      setError(err.message || "Error cargando datos");
    } finally {
      setLoading(false);
    }
  }

  async function cargarRol(id) {
    try {
      setLoadingRol(true);
      setError("");
      setMensaje("");

      const res = await fetch(`${API_URL_BASE}/roles/${id}`, {
        credentials: "include",
      });

      if (!res.ok) {
        throw new Error("No se pudo cargar el rol");
      }

      const rol = await res.json();
      const permisosRol = Array.isArray(rol?.permisos) ? rol.permisos : [];
      const nombresRol = permisosRol.map(nombrePermiso).filter(Boolean);

      const paginas = paginasVisibles
        .filter((page) => paginaMarcada(page, nombresRol, permisosMap))
        .map((page) => page.path);

      setPermisosActualesRol(permisosRol);
      setPaginasSeleccionadas(paginas);
    } catch (err) {
      console.error(err);
      setPermisosActualesRol([]);
      setPaginasSeleccionadas([]);
      setError(err.message || "Error cargando rol");
    } finally {
      setLoadingRol(false);
    }
  }

  function togglePagina(path) {
    setError("");
    setMensaje("");

    setPaginasSeleccionadas((prev) => {
      if (prev.includes(path)) {
        return prev.filter((item) => item !== path);
      }

      return [...prev, path];
    });
  }

  function calcularPermisoIdsObjetivo() {
    const nombres = new Set();

    paginasSeleccionadas.forEach((path) => {
      const page = PAGINAS.find((item) => item.path === path);

      if (!page) return;

      permisosPagina(page, permisosMap).forEach((permiso) => {
        nombres.add(normalizar(permiso));
      });
    });

    return permisos
      .filter((permiso) => nombres.has(normalizar(nombrePermiso(permiso))))
      .map(idPermiso)
      .filter((id) => id !== null && id !== undefined);
  }

  function calcularPermisoIdsGestionados() {
    const nombres = new Set();

    PAGINAS.forEach((page) => {
      permisosPagina(page, permisosMap).forEach((permiso) => {
        nombres.add(normalizar(permiso));
      });
    });

    return permisos
      .filter((permiso) => nombres.has(normalizar(nombrePermiso(permiso))))
      .map(idPermiso)
      .filter((id) => id !== null && id !== undefined);
  }

  async function guardar() {
    if (!rolId) {
      setError("Selecciona un rol");
      return;
    }

    try {
      setSaving(true);
      setError("");
      setMensaje("");

      const actuales = new Set(
        permisosActualesRol
          .map(idPermiso)
          .filter((id) => id !== null && id !== undefined)
      );

      const objetivo = new Set(calcularPermisoIdsObjetivo());
      const gestionados = new Set(calcularPermisoIdsGestionados());

      const agregar = [...objetivo].filter((id) => !actuales.has(id));
      const quitar = [...actuales].filter(
        (id) => gestionados.has(id) && !objetivo.has(id)
      );

      for (const permisoId of agregar) {
        const res = await fetch(
          `${API_URL_BASE}/roles/${rolId}/permisos/${permisoId}`,
          {
            method: "PATCH",
            credentials: "include",
          }
        );

        if (!res.ok) {
          const data = await leerRespuesta(res);
          throw new Error(data?.mensaje || "No se pudo agregar un permiso");
        }
      }

      for (const permisoId of quitar) {
        const res = await fetch(
          `${API_URL_BASE}/roles/${rolId}/permisos/${permisoId}`,
          {
            method: "DELETE",
            credentials: "include",
          }
        );

        if (!res.ok) {
          const data = await leerRespuesta(res);
          throw new Error(data?.mensaje || "No se pudo quitar un permiso");
        }
      }

      setMensaje("Permisos actualizados correctamente");
      await cargarRol(rolId);
    } catch (err) {
      console.error(err);
      setError(err.message || "Error guardando permisos");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <div className="py-10 text-center">Cargando permisos...</div>;
  }

  return (
    <div className="space-y-6 rounded-xl border bg-card p-6">
      <div>
        <h2 className="text-2xl font-bold">Permisos por página</h2>
        <p className="text-sm text-muted-foreground">
          Selecciona un rol y marca las páginas que podrá ver.
        </p>
      </div>

      {error && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      )}

      {mensaje && (
        <div className="rounded-lg border border-primary/30 bg-primary/10 px-4 py-3 text-sm text-primary">
          {mensaje}
        </div>
      )}

      <div className="flex flex-col gap-1.5">
        <label className="text-sm font-medium">Rol</label>
        <select
          value={rolId}
          onChange={(event) => setRolId(event.target.value)}
          className="h-10 rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
        >
          <option value="">Selecciona un rol</option>
          {roles.map((rol) => (
            <option key={rol.id} value={rol.id}>
              {nombreRol(rol)}
            </option>
          ))}
        </select>
      </div>

      {rolId && (
        <div className="space-y-4">
          {loadingRol ? (
            <div className="rounded-lg border bg-muted/30 p-6 text-center text-sm text-muted-foreground">
              Cargando páginas del rol...
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {paginasVisibles.map((page) => {
                const selected = paginasSeleccionadas.includes(page.path);

                return (
                  <button
                    key={page.path}
                    type="button"
                    onClick={() => togglePagina(page.path)}
                    className={`rounded-lg border px-4 py-3 text-sm font-medium transition ${
                      selected
                        ? "border-primary bg-primary text-primary-foreground"
                        : "bg-background hover:bg-muted"
                    }`}
                  >
                    {page.title}
                  </button>
                );
              })}
            </div>
          )}

          <div className="flex justify-end">
            <Button type="button" onClick={guardar} disabled={saving || loadingRol}>
              {saving ? "Guardando..." : "Guardar"}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}