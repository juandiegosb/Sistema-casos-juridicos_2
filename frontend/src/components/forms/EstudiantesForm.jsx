"use client";

import React, { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

export function EstudiantesForm() {
  const router = useRouter();

  const [estudiantes, setEstudiantes] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [cargando, setCargando] = useState(true);

  const API_URL = "http://localhost:8080/api";

  // Validar sesión + permisos y luego cargar datos
  useEffect(() => {
    const verificarYCargar = async () => {
      try {
        const res = await fetch(`${API_URL}/auth/me`, {
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

        // Cargar estudiantes
        const estudiantesRes = await fetch(`${API_URL}/estudiantes/activos`, {
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