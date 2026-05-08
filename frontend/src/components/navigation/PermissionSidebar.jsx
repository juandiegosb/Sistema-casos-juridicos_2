"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { AppSidebar } from "@/components/app-sidebar";
import { API_URL_BASE } from "@/lib/config";

const SIDEBAR_PAGES = [
  {
    title: "Inicio",
    tooltip: "Inicio",
    path: "/inicio",
    authOnly: true,
  },
  {
    title: "Nueva consulta",
    tooltip: "Nueva consulta",
    path: "/nuevaconsulta",
    requiredPermissions: ["Gestionar consultas"],
  },
  {
    title: "Consultas juridicas",
    tooltip: "Consultas juridicas",
    path: "/consultasjuridicas",
    requiredPermissions: ["Gestionar consultas"],
  },
  {
    title: "admin",
    tooltip: "Administración",
    path: "/admin",
    requiredPermissions: ["Gestionar catálogos", "Gestionar permisos"],
    match: "any",
  },
  {
    title: "Roles",
    tooltip: "Roles / Usuarios",
    path: "/roles",
    requiredPermissions: ["Gestionar usuarios"],
  },
  {
    title: "Estudiantes",
    tooltip: "Estudiantes",
    path: "/estudiantes",
    requiredPermissions: ["Gestionar usuarios"],
  },
  {
    title: "Asesores y monitores",
    tooltip: "Asesores y monitores",
    path: "/asesoresymonitores",
    requiredPermissions: ["Gestionar usuarios"],
  },
];

function tieneTodosLosPermisos(userPermissions, requiredPermissions = []) {
  return requiredPermissions.every((permission) =>
    userPermissions.includes(permission)
  );
}

function tieneAlgunPermiso(userPermissions, requiredPermissions = []) {
  return requiredPermissions.some((permission) =>
    userPermissions.includes(permission)
  );
}

function puedeVerPagina(page, user) {
  if (!user) return false;

  if (page.authOnly) {
    return true;
  }

  const userPermissions = Array.isArray(user.permisos) ? user.permisos : [];
  const requiredPermissions = Array.isArray(page.requiredPermissions)
    ? page.requiredPermissions
    : [];

  if (requiredPermissions.length === 0) {
    return true;
  }

  if (page.match === "any") {
    return tieneAlgunPermiso(userPermissions, requiredPermissions);
  }

  return tieneTodosLosPermisos(userPermissions, requiredPermissions);
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
          return;
        }

        const data = await res.json();
        setUser(data);
      } catch (error) {
        console.error("Error cargando permisos del usuario", error);
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