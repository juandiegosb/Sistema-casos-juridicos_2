"use client";

import React, { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { API_URL_BASE } from "@/lib/config";

const PERMISOS_POR_PAGINA = [

  {
    pagina: "Nueva consulta",
    ruta: "/nuevaconsulta",
    permisos: ["Gestionar consultas"],
    nota: "También carga personas, sedes, áreas, asesores, monitores y estudiantes.",
  },
  {
    pagina: "Consultas jurídicas",
    ruta: "/consultasjuridicas",
    permisos: ["Gestionar consultas"],
    nota: "Permite listar, buscar, editar y desactivar consultas",
  },
  {
    pagina: "Admin / Catálogos",
    ruta: "/admin",
    permisos: ["Gestionar catálogos"],
    nota: "Necesario para administrar Tema, Tipo y Área.",
  },
  {
    pagina: "Admin / Permisos por rol",
    ruta: "/admin",
    permisos: ["Gestionar roles", "Gestionar permisos"],
    nota: "Necesita ambos permisos porque carga roles activos y permisos activos.",
  },
  {
    pagina: "Crear usuarios",
    ruta: "/roles",
    permisos: ["Gestionar usuarios", "Gestionar catálogos"],
    nota: "permite acceder a la pagina roles",
  },
  {
    pagina: "Gestión de roles",
    ruta: "/api/roles",
    permisos: ["Gestionar roles"],
    nota: "Permiso requerido para editar, activar o desactivar roles.",
  },
  {
    pagina: "Gestión de permisos",
    ruta: "/api/permisos",
    permisos: ["Gestionar permisos"],
    nota: "Permiso requerido para activar o desactivar permisos.",
  },
];

function PermisosPorPaginaInfo() {
  return (
    <div className="rounded-xl border bg-muted/30 p-4 space-y-4">
      <div>
        <h3 className="font-semibold">Permisos necesarios por página</h3>
        <p className="text-sm text-muted-foreground">
          Usa esta guía antes de guardar los permisos del rol. Algunas páginas
          necesitan más de un permiso porque cargan datos auxiliares.
        </p>
      </div>

      <div className="grid gap-3">
        {PERMISOS_POR_PAGINA.map((item) => (
          <div
            key={`${item.pagina}-${item.ruta}`}
            className="rounded-lg border bg-background p-4"
          >
            <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <p className="font-medium">{item.pagina}</p>
                <p className="text-xs text-muted-foreground">{item.ruta}</p>
              </div>

              <div className="flex flex-wrap gap-2 pt-2 sm:pt-0">
                {item.permisos.map((permiso) => (
                  <span
                    key={permiso}
                    className="rounded-full border bg-muted px-2.5 py-1 text-xs font-medium"
                  >
                    {permiso}
                  </span>
                ))}
              </div>
            </div>

            {item.nota && (
              <p className="mt-2 text-sm text-muted-foreground">
                {item.nota}
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

export function RolePermissionsForm() {
  const [roles, setRoles] = useState([]);
  const [permisos, setPermisos] = useState([]);
  const [selectedRoleId, setSelectedRoleId] = useState("");
  const [selectedPermisoIds, setSelectedPermisoIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const selectedRole = useMemo(
    () => roles.find((rol) => String(rol.id) === String(selectedRoleId)),
    [roles, selectedRoleId]
  );

  useEffect(() => {
    const cargarDatos = async () => {
      try {
        setLoading(true);
        setError("");

        const [rolesResponse, permisosResponse] = await Promise.all([
          fetch(`${API_URL_BASE}/roles/activos`, {
            credentials: "include",
          }),
          fetch(`${API_URL_BASE}/permisos/activos`, {
            credentials: "include",
          }),
        ]);

        if (rolesResponse.status === 401 || permisosResponse.status === 401) {
          setError("La sesión expiró. Inicia sesión nuevamente.");
          return;
        }

        if (rolesResponse.status === 403 || permisosResponse.status === 403) {
          setError(
            "No tienes permisos para gestionar roles y permisos. Necesitas Gestionar roles y Gestionar permisos."
          );
          return;
        }

        if (!rolesResponse.ok || !permisosResponse.ok) {
          throw new Error("No se pudieron cargar los roles o permisos.");
        }

        const [rolesData, permisosData] = await Promise.all([
          rolesResponse.json(),
          permisosResponse.json(),
        ]);

        setRoles(Array.isArray(rolesData) ? rolesData : []);
        setPermisos(Array.isArray(permisosData) ? permisosData : []);
      } catch (err) {
        console.error(err);
        setError("Ocurrió un error cargando los datos.");
      } finally {
        setLoading(false);
      }
    };

    cargarDatos();
  }, []);

  const obtenerPermisoIdsDelRol = (rol) => {
    if (!rol) return [];

    if (Array.isArray(rol.permisoIds)) {
      return rol.permisoIds.map(Number);
    }

    if (Array.isArray(rol.permisos)) {
      return rol.permisos.map((permiso) => Number(permiso.id));
    }

    return [];
  };

  const handleRolChange = (event) => {
    const rolId = event.target.value;
    const rol = roles.find((item) => String(item.id) === rolId);

    setSelectedRoleId(rolId);
    setSelectedPermisoIds(new Set(obtenerPermisoIdsDelRol(rol)));
    setSuccess("");
    setError("");
  };

  const handlePermisoChange = (permisoId) => {
    setSelectedPermisoIds((current) => {
      const next = new Set(current);

      if (next.has(permisoId)) {
        next.delete(permisoId);
      } else {
        next.add(permisoId);
      }

      return next;
    });

    setSuccess("");
  };

  const seleccionarTodos = () => {
    setSelectedPermisoIds(
      new Set(permisos.map((permiso) => Number(permiso.id)))
    );
    setSuccess("");
  };

  const limpiarSeleccion = () => {
    setSelectedPermisoIds(new Set());
    setSuccess("");
  };

  const leerMensajeError = async (response) => {
    try {
      const data = await response.json();
      return data?.message || data?.error || "No se pudo actualizar el rol.";
    } catch {
      return "No se pudo actualizar el rol.";
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!selectedRole) {
      setError("Selecciona un rol antes de guardar.");
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccess("");

      const permisoIds = Array.from(selectedPermisoIds).map(Number);

      const response = await fetch(`${API_URL_BASE}/roles/${selectedRole.id}`, {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          id: selectedRole.id,
          nombre: selectedRole.nombre,
          descripcion: selectedRole.descripcion,
          activo: selectedRole.activo,
          permisoIds,
        }),
      });

      if (response.status === 401) {
        setError("La sesión expiró. Inicia sesión nuevamente.");
        return;
      }

      if (response.status === 403) {
        setError("No tienes permisos para actualizar roles.");
        return;
      }

      if (!response.ok) {
        setError(await leerMensajeError(response));
        return;
      }

      const rolActualizado = await response.json();

      setRoles((current) =>
        current.map((rol) =>
          rol.id === rolActualizado.id ? rolActualizado : rol
        )
      );

      setSelectedPermisoIds(new Set(obtenerPermisoIdsDelRol(rolActualizado)));
      setSuccess("Permisos del rol actualizados correctamente.");
    } catch (err) {
      console.error(err);
      setError("Ocurrió un error guardando los permisos del rol.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="text-center mt-10">
        Cargando roles y permisos...
      </div>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-6 p-6 bg-card rounded-xl border"
    >
      <div className="space-y-2">
        <label htmlFor="rolId" className="text-sm font-medium">
          Rol
        </label>

        <select
          id="rolId"
          name="rolId"
          value={selectedRoleId}
          onChange={handleRolChange}
          className="w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
        >
          <option value="">Selecciona un rol</option>

          {roles.map((rol) => (
            <option key={rol.id} value={rol.id}>
              {rol.nombre}
            </option>
          ))}
        </select>
      </div>

      <div className="space-y-3">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h3 className="font-semibold">Permisos del sistema</h3>
            <p className="text-sm text-muted-foreground">
              Los permisos marcados quedarán asignados al rol seleccionado.
            </p>
          </div>

          <div className="flex gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={seleccionarTodos}
              disabled={!selectedRole || permisos.length === 0}
            >
              Seleccionar todos
            </Button>

            <Button
              type="button"
              variant="outline"
              onClick={limpiarSeleccion}
              disabled={!selectedRole || selectedPermisoIds.size === 0}
            >
              Limpiar
            </Button>
          </div>
        </div>

        {permisos.length === 0 ? (
          <div className="rounded-md border bg-muted/30 px-4 py-6 text-center text-sm text-muted-foreground">
            No hay permisos activos registrados.
          </div>
        ) : (
          <div className="grid gap-3 md:grid-cols-2">
            {permisos.map((permiso) => {
              const permisoId = Number(permiso.id);
              const checked = selectedPermisoIds.has(permisoId);

              return (
                <label
                  key={permiso.id}
                  className={`flex cursor-pointer items-start gap-3 rounded-lg border p-4 transition ${checked ? "bg-muted" : "bg-background"
                    } ${!selectedRole ? "cursor-not-allowed opacity-60" : ""}`}
                >
                  <input
                    type="checkbox"
                    checked={checked}
                    disabled={!selectedRole}
                    onChange={() => handlePermisoChange(permisoId)}
                    className="mt-1 h-4 w-4"
                  />

                  <span className="space-y-1">
                    <span className="block font-medium">
                      {permiso.nombre}
                    </span>

                    {permiso.descripcion && (
                      <span className="block text-sm text-muted-foreground">
                        {permiso.descripcion}
                      </span>
                    )}
                  </span>
                </label>
              );
            })}
          </div>
        )}
      </div>

      <div className="flex justify-end">
        <Button type="submit" disabled={!selectedRole || saving}>
          {saving ? "Guardando..." : "Guardar permisos"}
        </Button>
      </div>
      <div>
        <h2 className="text-2xl font-bold">Permisos por rol</h2>
        <p className="text-muted-foreground">
          Selecciona un rol, marca los permisos que debe tener y guarda los
          cambios.
        </p>
      </div>

      {error && (
        <div className="rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      )}

      {success && (
        <div className="rounded-md border border-green-500/30 bg-green-500/10 px-4 py-3 text-sm text-green-700 dark:text-green-400">
          {success}
        </div>
      )}

      <PermisosPorPaginaInfo />

    </form>
  );
}