"use client";

import { TooltipProvider } from "@/components/ui/tooltip";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { PermissionSidebar } from "@/components/navigation/PermissionSidebar";
import { PersonasForm } from "@/components/forms/PersonasForm";
import { Users } from "lucide-react";

export default function PersonasPage() {
  return (
    <TooltipProvider>
      <SidebarProvider>
        <PermissionSidebar />

        <SidebarInset className="bg-muted/30 min-h-screen">
          <header className="sticky top-0 z-10 flex h-14 items-center gap-3 border-b bg-background/80 px-4 backdrop-blur">
            <SidebarTrigger />
            <div>
              <p className="text-xs text-muted-foreground">
                Panel / Personas
              </p>
              <h1 className="text-sm font-semibold">
                Gestión de personas
              </h1>
            </div>
          </header>

          <main className="p-6 space-y-6">
            <div className="flex items-center gap-3">
              <div className="flex size-11 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <Users className="size-6" />
              </div>

              <div>
                <h2 className="text-3xl font-bold">Personas</h2>
                <p className="text-muted-foreground">
                  Consulta, edita y administra las personas registradas desde recepción.
                </p>
              </div>
            </div>

            <PersonasForm />
          </main>
        </SidebarInset>
      </SidebarProvider>
    </TooltipProvider>
  );
}