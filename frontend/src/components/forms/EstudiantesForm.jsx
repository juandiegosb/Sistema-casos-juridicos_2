"use client";

import React, { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";

export function EstudiantesForm() {
  const [estudiantes, setEstudiantes] = useState([]);
  const [busqueda, setBusqueda] = useState("");

  const API_URL = "http://localhost:8080/api";

  // Cargar estudiantes activos
  useEffect(() => {
  const cargarDatos = async () => {
    try {
      const res = await fetch(`${API_URL}/estudiantes/activos`);
      const data = await res.json();

      console.log("RESPUESTA BACK:", data);

      // 🔥 Asegurar que siempre sea array
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
    }
  };

  cargarDatos();
}, []);

  // Filtro
  const filtrados = estudiantes.filter(e =>
    `${e.nombre} ${e.documento} ${e.email} ${e.codigo}`
      .toLowerCase()
      .includes(busqueda.toLowerCase())
  );

  return (
    <div className="space-y-6">

      {/* BUSCADOR */}
      <Input
        placeholder="Buscar por nombre, documento, email o código..."
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
      />

      {/* TABLA */}
      <div className="overflow-x-auto border rounded-xl">
        <table className="w-full text-sm">

          {/* HEADER */}
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

          {/* BODY */}
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

                {/* Conciliación */}
                <td className="p-3">
                  <span className={`text-xs px-2 py-1 rounded ${
                    e.conciliacion
                      ? "bg-green-100 text-green-700"
                      : "bg-gray-100 text-gray-600"
                  }`}>
                    {e.conciliacion ? "Sí" : "No"}
                  </span>
                </td>

                {/* Activo */}
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

      {/* VACÍO */}
      {filtrados.length === 0 && (
        <p className="text-center text-muted-foreground">
          No se encontraron resultados
        </p>
      )}
    </div>
  );
}