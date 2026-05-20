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

const PERMISO_VER_PROCESOS = PERMISOS.VER_PROCESOS || "Ver procesos";
const PERMISO_GESTIONAR_PROCESOS =
  PERMISOS.GESTIONAR_PROCESOS || "Gestionar procesos";

const SIDEBAR_PAGES = [
  {
    title: "Inicio",
    tooltip: "Inicio",
    path: "/inicio",
    requiredPermissions: [PERMISOS.ACCEDER_INICIO],
  },
  {
    title: "Recepción",
    tooltip: "Recepción de personas",
    path: "/recepcion",
    requiredPermissions: [PERMISOS.ACCEDER_RECEPCION],
  },
  {
    title: "Personas",
    tooltip: "Personas",
    path: "/personas",
    requiredPermissions: [PERMISOS.ACCEDER_PERSONAS],
  },
  {
    title: "Nueva consulta",
    tooltip: "Nueva consulta",
    path: "/nuevaconsulta",
    requiredPermissions: [PERMISOS.ACCEDER_NUEVA_CONSULTA],
  },
  {
    title: "Consultas jurídicas",
    tooltip: "Consultas jurídicas",
    path: "/consultasjuridicas",
    requiredPermissions: [PERMISOS.ACCEDER_CONSULTAS_JURIDICAS],
  },
  {
    title: "Tareas",
    tooltip: "Tareas y seguimientos",
    path: "/tareas",
    requiredPermissions: [PERMISOS.ACCEDER_TAREAS],
  },
  {
    title: "Nuevo proceso",
    tooltip: "Registrar nuevo proceso",
    path: "/nuevoproceso",
    requiredPermissions: [
      PERMISOS.ACCEDER_PROCESOS,
      PERMISO_GESTIONAR_PROCESOS,
    ],
    match: "all",
  },
  {
    title: "Procesos",
    tooltip: "Procesos",
    path: "/procesos",
    requiredPermissions: [
      PERMISOS.ACCEDER_PROCESOS,
      PERMISO_VER_PROCESOS,
    ],
    match: "all",
  },
  {
    title: "Conciliaciones",
    tooltip: "Conciliaciones",
    path: "/conciliaciones",
    requiredPermissions: [PERMISOS.ACCEDER_CONCILIACIONES],
  },
  {
    title: "Estudiantes",
    tooltip: "Estudiantes",
    path: "/estudiantes",
    requiredPermissions: [PERMISOS.ACCEDER_ESTUDIANTES],
  },
  {
    title: "Asesores y monitores",
    tooltip: "Asesores y monitores",
    path: "/asesoresymonitores",
    requiredPermissions: [PERMISOS.ACCEDER_ASESORES_MONITORES],
  },
  {
    title: "Roles",
    tooltip: "Roles / Usuarios",
    path: "/roles",
    requiredPermissions: [PERMISOS.ACCEDER_ROLES],
  },
  {
    title: "Administración",
    tooltip: "Administración",
    path: "/admin",
    requiredPermissions: [PERMISOS.ACCEDER_ADMINISTRACION],
  },
  {
    title: "Eliminación",
    tooltip: "Registros desactivados",
    path: "/eliminacion",
    requiredPermissions: [
      PERMISOS.CAMBIAR_ESTADO_PERSONAS,
      PERMISOS.CAMBIAR_ESTADO_USUARIOS,
      PERMISOS.CAMBIAR_ESTADO_ESTUDIANTES,
      PERMISOS.CAMBIAR_ESTADO_CONSULTAS,
      PERMISOS.ARCHIVAR_CONSULTAS,
    ],
    match: "all",
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