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
  },
  {
    title: "Recepcion",
    tooltip: "Recepción de personas",
    path: "/recepcion",
    requiredPermissions: [PERMISOS.ACCEDER_RECEPCION],
  },
  {
    title: "Tareas",
    tooltip: "Tareas",
    path: "/tareas",
    requiredPermissions: [PERMISOS.ACCEDER_TAREAS],
  },
  {
    title: "Nueva consulta",
    tooltip: "Nueva consulta",
    path: "/nuevaconsulta",
    requiredPermissions: [PERMISOS.ACCEDER_NUEVA_CONSULTA],
  },
  {
    title: "Consultas juridicas",
    tooltip: "Consultas juridicas",
    path: "/consultasjuridicas",
    requiredPermissions: [PERMISOS.ACCEDER_CONSULTAS_JURIDICAS],
  },
  {
    title: "Personas",
    tooltip: "Personas",
    path: "/personas",
    requiredPermissions: [PERMISOS.ACCEDER_PERSONAS],
  },
  {
    title: "Administración",
    tooltip: "Administración",
    path: "/admin",
    requiredPermissions: [PERMISOS.ACCEDER_ADMINISTRACION],
  },
  {
    title: "Roles",
    tooltip: "Roles / Usuarios",
    path: "/roles",
    requiredPermissions: [PERMISOS.ACCEDER_ROLES],
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
    ? page.requiredPermissions
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