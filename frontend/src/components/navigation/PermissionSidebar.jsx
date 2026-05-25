"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { AppSidebar } from "@/components/app-sidebar";
import { API_URL_BASE } from "@/lib/config";
import { PERMISOS } from "@/lib/permission";
import {
  tieneAlgunPermiso,
  tieneTodosLosPermisos,
} from "@/lib/authz";

const SIDEBAR_PAGES = [
  {
    title: "Inicio",
    tooltip: "Inicio",
    path: "/inicio",
    requiredPermissions: [PERMISOS.ACCEDER_INICIO],
    match: "any",
  },
  {
    title: "Recepción",
    tooltip: "Recepción de personas",
    path: "/recepcion",
    requiredPermissions: [
      PERMISOS.ACCEDER_RECEPCION,
      PERMISOS.VER_PERSONAS,
    ],
    match: "any",
  },
  {
    title: "Personas",
    tooltip: "Personas",
    path: "/personas",
    requiredPermissions: [
      PERMISOS.ACCEDER_PERSONAS,
      PERMISOS.VER_PERSONAS,
    ],
    match: "any",
  },
  {
    title: "Nueva consulta",
    tooltip: "Nueva consulta",
    path: "/nuevaconsulta",
    requiredPermissions: [
      PERMISOS.ACCEDER_NUEVA_CONSULTA,
      PERMISOS.CREAR_CONSULTAS,
    ],
    match: "any",
  },
  {
    title: "Consultas jurídicas",
    tooltip: "Consultas jurídicas",
    path: "/consultasjuridicas",
    requiredPermissions: [
      PERMISOS.ACCEDER_CONSULTAS_JURIDICAS,
      PERMISOS.VER_CONSULTAS,
    ],
    match: "any",
  },
  {
    title: "Tareas",
    tooltip: "Tareas y seguimientos",
    path: "/tareas",
    requiredPermissions: [
      PERMISOS.ACCEDER_TAREAS,
      PERMISOS.VER_SEGUIMIENTOS,
    ],
    match: "any",
  },
  {
    title: "Nuevo proceso",
    tooltip: "Registrar nuevo proceso",
    path: "/nuevoproceso",
    requiredPermissions: [
      PERMISOS.GESTIONAR_PROCESOS,
    ],
    match: "any",
  },
  {
    title: "Procesos",
    tooltip: "Procesos",
    path: "/procesos",
    requiredPermissions: [
      PERMISOS.VER_PROCESOS,
    ],
    match: "any",
  },
  {
    title: "Conciliaciones",
    tooltip: "Conciliaciones",
    path: "/conciliaciones",
    requiredPermissions: [
      PERMISOS.ACCEDER_CONCILIACIONES,
      PERMISOS.VER_CONCILIACIONES,
      PERMISOS.GESTIONAR_CONCILIACIONES,
    ],
    match: "any",
  },
  {
    title: "Estudiantes",
    tooltip: "Estudiantes",
    path: "/estudiantes",
    requiredPermissions: [
      PERMISOS.ACCEDER_ESTUDIANTES,
      PERMISOS.VER_ESTUDIANTES,
    ],
    match: "any",
  },
  {
    title: "Asesores y monitores",
    tooltip: "Asesores y monitores",
    path: "/asesoresymonitores",
    requiredPermissions: [
      PERMISOS.ACCEDER_ASESORES_MONITORES,
      PERMISOS.VER_ASESORES_MONITORES,
    ],
    match: "any",
  },
  {
    title: "Roles",
    tooltip: "Roles / Usuarios",
    path: "/roles",
    requiredPermissions: [
      PERMISOS.ACCEDER_ROLES,
      PERMISOS.VER_ROLES,
    ],
    match: "any",
  },
  {
    title: "Administración",
    tooltip: "Administración",
    path: "/admin",
    requiredPermissions: [
      PERMISOS.ACCEDER_ADMINISTRACION,
    ],
    match: "all",
  },
  {
    title: "Eliminación",
    tooltip: "Registros desactivados",
    path: "/eliminacion",
    requiredPermissions: [
      PERMISOS.ACCEDER_ELIMINACION,
      PERMISOS.CAMBIAR_ESTADO_PERSONAS,
      PERMISOS.CAMBIAR_ESTADO_USUARIOS,
      PERMISOS.CAMBIAR_ESTADO_ESTUDIANTES,
      PERMISOS.CAMBIAR_ESTADO_CONSULTAS,
      PERMISOS.ARCHIVAR_CONSULTAS,
    ],
    match: "any",
  },
];

function puedeVerPagina(page, user) {
  if (!user) return false;

  const requiredPermissions = Array.isArray(page.requiredPermissions)
    ? page.requiredPermissions.filter(Boolean)
    : [];

  if (requiredPermissions.length === 0) {
    return true;
  }

  if (page.match === "any") {
    return tieneAlgunPermiso(user, requiredPermissions);
  }

  return tieneTodosLosPermisos(user, requiredPermissions);
}

function filtrarPaginasPorPermisos(pages, user) {
  return pages.filter((page) => puedeVerPagina(page, user));
}

export function PermissionSidebar() {
  const router = useRouter();

  const [user, setUser] = React.useState(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const cargarUsuario = async () => {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, {
          method: "GET",
          credentials: "include",
        });

        if (res.status === 401) {
          router.replace("/");
          return;
        }

        if (!res.ok) {
          setUser(null);
          return;
        }

        const data = await res.json();
        setUser(data);
      } catch (error) {
        console.error("Error cargando permisos del usuario", error);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    cargarUsuario();
  }, [router]);

  if (loading) {
    return <AppSidebar mainItems={[]} footerItems={[]} />;
  }

  const mainItems = filtrarPaginasPorPermisos(SIDEBAR_PAGES, user);

  return <AppSidebar mainItems={mainItems} footerItems={[]} />;
}