"use client";

import React, { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { useRouter } from "next/navigation";
import { API_URL_BASE } from "@/lib/config";

export function AsesoresYMonitoresForm() {
  const router = useRouter();

  const [asesores, setAsesores] = useState([]);
  const [monitores, setMonitores] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    const verificarYcargar = async () => {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, {
          method: "GET",
          credentials: "include",
        });

        if (res.status === 401) {
          router.push("/");
          return;
        }

        const usuario = await res.json();

        if (!usuario.permisos?.includes("Gestionar usuarios")) {
          router.push("/inicio");
          return;
        }

        const [asesoresRes, monitoresRes] = await Promise.all([
          fetch(`${API_URL_BASE}/asesores/activos`, { credentials: "include" }),
          fetch(`${API_URL_BASE}/monitores/activos`, { credentials: "include" }),
        ]);

        if (asesoresRes.status === 401 || monitoresRes.status === 401) {
          router.push("/");
          return;
        }

        if (asesoresRes.status === 403 || monitoresRes.status === 403) {
          router.push("/inicio");
          return;
        }

        const asesoresData = await asesoresRes.json();
        const monitoresData = await monitoresRes.json();

        setAsesores(asesoresData);
        setMonitores(monitoresData);

      } catch (err) {
        toast.error("Error cargando usuarios");
      } finally {
        setCargando(false);
      }
    };

    verificarYcargar();
  }, [router]);

  async function desactivar(id, rol) {
    if (!confirm(`¿Desactivar este ${rol.toLowerCase()}?`)) return;
    const endpoint = rol === "Asesor" ? "asesores" : "monitores";
    const res = await fetch(`${API_URL_BASE}/${endpoint}/${id}/activo?activo=false`, {
      method: "PATCH",
      credentials: "include",
    });
    if (res.ok) {
      toast.success(`${rol} desactivado`);
      if (rol === "Asesor") {
        setAsesores(prev => prev.map(a => a.id === id ? { ...a, activo: false } : a));
      } else {
        setMonitores(prev => prev.map(m => m.id === id ? { ...m, activo: false } : m));
      }
    } else {
      toast.error("Error al desactivar");
    }
  }

  if (cargando) {
    return <div className="text-center mt-10">Cargando...</div>;
  }

  const usuarios = [
    ...asesores.map(a => ({ ...a, rol: "Asesor" })),
    ...monitores.map(m => ({ ...m, rol: "Monitor" }))
  ];

  const filtrados = usuarios.filter(u =>
    `${u.nombre} ${u.documento} ${u.email}`
      .toLowerCase()
      .includes(busqueda.toLowerCase())
  );

  return (
    <div className="space-y-6">

      {/* BUSCADOR */}
      <Input
        placeholder="Buscar por nombre, documento o email..."
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
      />

      {/* TABLA */}
      <div className="overflow-x-auto border rounded-xl">
        <table className="w-full text-sm">

          <thead className="bg-muted/50">
            <tr className="text-left">
              <th className="p-3">Nombre</th>
              <th className="p-3">Documento</th>
              <th className="p-3">Email</th>
              <th className="p-3">Teléfono</th>
              <th className="p-3">Código</th>
              <th className="p-3">Rol</th>
              <th className="p-3">Estado</th>
              <th className="p-3">Acciones</th>
            </tr>
          </thead>

          <tbody>
            {filtrados.map((u) => (
              <tr
                key={`${u.rol}-${u.id}`}
                className="border-t hover:bg-muted/30 transition"
              >
                <td className="p-3 font-medium">{u.nombre}</td>
                <td className="p-3">{u.documento}</td>
                <td className="p-3">{u.email}</td>
                <td className="p-3">{u.telefono}</td>
                <td className="p-3">{u.codigo}</td>
                <td className="p-3">
                  <span className="px-2 py-1 text-xs rounded bg-primary/10 text-primary">
                    {u.rol}
                  </span>
                </td>
                <td className="p-3">
                  <span className={`text-xs px-2 py-1 rounded ${
                    u.activo
                      ? "bg-blue-100 text-blue-700"
                      : "bg-red-100 text-red-700"
                  }`}>
                    {u.activo ? "Activo" : "Inactivo"}
                  </span>
                </td>
                <td className="p-3">
                  <button
                    onClick={() => desactivar(u.id, u.rol)}
                    disabled={!u.activo}
                    className={`text-xs px-3 py-1 rounded ${
                      u.activo
                        ? "bg-red-100 text-red-700 hover:bg-red-200"
                        : "bg-gray-100 text-gray-400 cursor-not-allowed"
                    }`}
                  >
                    {u.activo ? "Desactivar" : "Inactivo"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* VACÍO */}
      {filtrados.length === 0 && (
        <p className="text-center text-muted-foreground">
          No se encontraron resultados
        </p>
      )}
    </div>
  );
}