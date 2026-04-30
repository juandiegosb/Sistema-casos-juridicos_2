"use client";

import React, { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";

export function AsesoresYMonitoresForm() {
  const [asesores, setAsesores] = useState([]);
  const [monitores, setMonitores] = useState([]);
  const [busqueda, setBusqueda] = useState("");

  const API_URL = "http://localhost:8080/api";

  // Cargar datos
  useEffect(() => {
    const cargarDatos = async () => {
      try {
        const [asesoresRes, monitoresRes] = await Promise.all([
          fetch(`${API_URL}/asesores/activos`).then(r => r.json()),
          fetch(`${API_URL}/monitores/activos`).then(r => r.json())
        ]);

        setAsesores(asesoresRes);
        setMonitores(monitoresRes);
      } catch {
        toast.error("Error cargando usuarios");
      }
    };

    cargarDatos();
  }, []);

  // Unificar con rol
  const usuarios = [
    ...asesores.map(a => ({ ...a, rol: "Asesor" })),
    ...monitores.map(m => ({ ...m, rol: "Monitor" }))
  ];

  // Filtro
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

          {/* HEADER */}
          <thead className="bg-muted/50">
            <tr className="text-left">
              <th className="p-3">Nombre</th>
              <th className="p-3">Documento</th>
              <th className="p-3">Email</th>
              <th className="p-3">Teléfono</th>
              <th className="p-3">Código</th>
              <th className="p-3">Rol</th>
            </tr>
          </thead>

          {/* BODY */}
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