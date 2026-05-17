"use client";

import React, { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { useRouter } from "next/navigation";
import { API_URL_BASE } from "@/lib/config";

export function EstudiantesForm() {
  const router = useRouter();

  const [estudiantes, setEstudiantes] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    const verificarYCargar = async () => {
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

        // Si es asesor, traer solo sus estudiantes activos
        let url = `${API_URL_BASE}/estudiantes/activos`;
        if (usuario.tipoPerfil === "ASESOR" && usuario.perfilId) {
          url = `${API_URL_BASE}/estudiantes/activos/asesor/${usuario.perfilId}`;
        }

        const estudiantesRes = await fetch(url, {
          credentials: "include",
        });

        if (estudiantesRes.status === 401) {
          router.push("/");
          return;
        }

        if (estudiantesRes.status === 403) {
          router.push("/inicio");
          return;
        }

        const data = await estudiantesRes.json();

        if (Array.isArray(data)) {
          setEstudiantes(data);
        } else if (Array.isArray(data.content)) {
          setEstudiantes(data.content);
        } else if (Array.isArray(data.data)) {
          setEstudiantes(data.data);
        } else {
          setEstudiantes([]);
          toast.error("La API no devolvió una lista");
        }

      } catch (e) {
        console.error(e);
        toast.error("Error cargando estudiantes");
      } finally {
        setCargando(false);
      }
    };

    verificarYCargar();
  }, [router]);

  async function desactivar(id) {
    if (!confirm("¿Desactivar este estudiante?")) return;
    const res = await fetch(`${API_URL_BASE}/estudiantes/${id}/activo?activo=false`, {
      method: "PATCH",
      credentials: "include",
    });
    if (res.ok) {
      toast.success("Estudiante desactivado");
      setEstudiantes(prev => prev.map(e => e.id === id ? { ...e, activo: false } : e));
    } else {
      toast.error("Error al desactivar");
    }
  }

  if (cargando) {
    return <div className="text-center mt-10">Cargando...</div>;
  }

  const filtrados = estudiantes.filter(e =>
    `${e.nombre} ${e.documento} ${e.email} ${e.codigo}`
      .toLowerCase()
      .includes(busqueda.toLowerCase())
  );

  return (
    <div className="space-y-6">

      <Input
        placeholder="Buscar por nombre, documento, email o código..."
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
      />

      <div className="overflow-x-auto border rounded-xl">
        <table className="w-full text-sm">

          <thead className="bg-muted/50">
            <tr className="text-left">
              <th className="p-3">Nombre</th>
              <th className="p-3">Documento</th>
              <th className="p-3">Email</th>
              <th className="p-3">Teléfono</th>
              <th className="p-3">Código</th>
              <th className="p-3">Conciliación</th>
              <th className="p-3">Estado</th>
              <th className="p-3">Acciones</th>
            </tr>
          </thead>

          <tbody>
            {filtrados.map((e) => (
              <tr
                key={e.id}
                className="border-t hover:bg-muted/30 transition"
              >
                <td className="p-3 font-medium">{e.nombre}</td>
                <td className="p-3">{e.documento}</td>
                <td className="p-3">{e.email}</td>
                <td className="p-3">{e.telefono}</td>
                <td className="p-3">{e.codigo}</td>

                <td className="p-3">
                  <span className={`text-xs px-2 py-1 rounded ${
                    e.conciliacion
                      ? "bg-green-100 text-green-700"
                      : "bg-gray-100 text-gray-600"
                  }`}>
                    {e.conciliacion ? "Sí" : "No"}
                  </span>
                </td>

                <td className="p-3">
                  <span className={`text-xs px-2 py-1 rounded ${
                    e.activo
                      ? "bg-blue-100 text-blue-700"
                      : "bg-red-100 text-red-700"
                  }`}>
                    {e.activo ? "Activo" : "Inactivo"}
                  </span>
                </td>

                <td className="p-3">
                  <button
                    onClick={() => desactivar(e.id)}
                    disabled={!e.activo}
                    className={`text-xs px-3 py-1 rounded ${
                      e.activo
                        ? "bg-red-100 text-red-700 hover:bg-red-200"
                        : "bg-gray-100 text-gray-400 cursor-not-allowed"
                    }`}
                  >
                    {e.activo ? "Desactivar" : "Inactivo"}
                  </button>
                </td>

              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filtrados.length === 0 && (
        <p className="text-center text-muted-foreground">
          No se encontraron resultados
        </p>
      )}
    </div>
  );
}