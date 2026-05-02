"use client"

import {
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { TooltipProvider } from "@/components/ui/tooltip"
import { Moon, Sun, FileText } from "lucide-react"
import { useTheme } from "next-themes"
import { Button } from "@/components/ui/button"
import { Toaster } from "@/components/ui/sonner"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { ConsultasJuridicasForm } from "@/components/forms/ConsultasJuridicasForm"

const sections = [
  "Inicio",
  "Tareas.",
  "Recepcion",
  "Nueva consulta",
  "Consultas juridicas",
  "Nuevo Proceso.",
  "Procesos juridicos.",
  "Estudiantes",
  "Asesores y monitores",
  "Estadísticas.",
  "Formatos y modelos.",
  "Eliminacines.",
  "admin",
  "Roles",
  "",
];
export default function ConsultasJuridicasPage() {
  const { setTheme } = useTheme()

  const mainItems = sections.map((item) => ({
    title: item,
    tooltip: item,
  }))

  return (
    <TooltipProvider>
      <SidebarProvider className="min-h-screen">
        <AppSidebar mainItems={mainItems} footerItems={[]} />

        <SidebarInset className="bg-muted/30 min-h-screen">

          {/* HEADER PRO */}
          <header className="flex h-16 items-center justify-between border-b bg-background/80 backdrop-blur px-6">
            <div className="flex items-center gap-4">
              <SidebarTrigger />

              <div className="flex flex-col leading-tight">
                <span className="text-sm text-muted-foreground">
                  Panel / Consultas
                </span>
                <span className="font-semibold">
                  Consultas Jurídicas
                </span>
              </div>
            </div>

            {/* THEME */}
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="icon">
                  <Sun className="h-5 w-5 transition-all dark:scale-0 dark:-rotate-90" />
                  <Moon className="absolute h-5 w-5 scale-0 rotate-90 transition-all dark:scale-100 dark:rotate-0" />
                </Button>
              </DropdownMenuTrigger>

              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => setTheme("light")}>
                  Claro
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setTheme("dark")}>
                  Oscuro
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setTheme("system")}>
                  Sistema
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </header>

          {/* CONTENIDO */}
          <div className="p-6 lg:p-10">

            <div className="max-w-6xl mx-auto space-y-6">

              {/* TITULO CON ICONO */}
              <div className="flex items-center gap-3">
                <div className="p-3 rounded-xl bg-primary/10">
                  <FileText className="w-6 h-6 text-primary" />
                </div>

                <div>
                  <h1 className="text-3xl font-bold tracking-tight">
                    Registro de Consulta
                  </h1>
                  <p className="text-muted-foreground">
                    Diligencie la información para registrar una nueva consulta jurídica
                  </p>
                </div>
              </div>

              {/* CARD PRINCIPAL */}
              <div className="rounded-2xl border border-border bg-background shadow-sm p-6 lg:p-8">

                <ConsultasJuridicasForm />

              </div>

            </div>
          </div>
        </SidebarInset>

        <Toaster richColors position="bottom-right" />
      </SidebarProvider>
    </TooltipProvider>
  )
}