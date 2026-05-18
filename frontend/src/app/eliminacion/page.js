"use client";

import { TooltipProvider } from "@/components/ui/tooltip";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";
import { PermissionSidebar } from "@/components/navigation/PermissionSidebar";
import { EliminacionForm } from "@/components/forms/EliminacionForm";
import { RotateCcw } from "lucide-react";

export default function EliminacionPage() {
  return (
    <TooltipProvider>
      <SidebarProvider>
        <PermissionSidebar />

        <SidebarInset className="bg-muted/30 min-h-screen">
          <header className="sticky top-0 z-10 flex h-14 items-center gap-3 border-b bg-background/80 px-4 backdrop-blur">
            <SidebarTrigger />
          </header>

          <main className="p-6 space-y-6">
            <div className="flex items-center gap-3">
              <div className="flex size-11 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <RotateCcw className="size-6" />
              </div>

              <div>
                <h2 className="text-3xl font-bold">Eliminación</h2>
              </div>
            </div>

            <EliminacionForm />
          </main>
        </SidebarInset>
      </SidebarProvider>
    </TooltipProvider>
  );
}