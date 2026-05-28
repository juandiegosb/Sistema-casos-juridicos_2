"use client";

import { Scale } from "lucide-react";
import { ConciliacionesForm } from "@/components/forms/conciliacion/ConciliacionesForm";

export default function ConciliacionesPage() {
  return (
    <div className="p-6 lg:p-10">
      <div className="mx-auto max-w-7xl space-y-6">
        <div className="flex items-center gap-3">
          <div className="rounded-xl bg-primary/10 p-3">
            <Scale className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Conciliaciones</h1>
            <p className="text-muted-foreground">
              Consulta, crea y gestiona conciliaciones según permisos y alcance del usuario.
            </p>
          </div>
        </div>

        <ConciliacionesForm />
      </div>
    </div>
  );
}
