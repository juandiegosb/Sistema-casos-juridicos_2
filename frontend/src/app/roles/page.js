"use client"

import {
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { TooltipProvider } from "@/components/ui/tooltip"
import { Moon, Sun } from "lucide-react"
import { useTheme } from "next-themes"
import { Button } from "@/components/ui/button"
import { Toaster } from "@/components/ui/sonner"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { UsuarioSistemaForm } from "@/components/forms/UsuarioSistemaForm"

const sections = [
    "Inicio",
    "Tareas💀",
    "Recepcion",
    "Nueva consulta💀",
    "Consultas juridicas",
    "Nuevo Proceso💀",
    "Procesos juridicos💀",
    "Estudiantes💀",
    "Asesores y monitores💀",
    "Estadísticas💀",
    "Formatos y modelos💀",
    "Eliminacines💀",
    "admin",
    "Roles",
    "",
  ]

export default function UsuarioSistemaPage() {
  const { setTheme } = useTheme()

  const mainItems = sections.map((item) => ({
    title: item,
    tooltip: item,
  }))

  return (
    <TooltipProvider>
      <SidebarProvider>
        <AppSidebar mainItems={mainItems} footerItems={[]} />

        {/* FONDO SUAVE GLOBAL */}
        <SidebarInset className="bg-muted/30 min-h-screen">

          {/* HEADER PRO */}
          <header className="flex h-16 items-center justify-between border-b bg-background/80 backdrop-blur px-6">

            <div className="flex items-center gap-4">
              <SidebarTrigger />

              <div className="flex flex-col leading-tight">
                <span className="text-sm text-muted-foreground">
                  Sistema / Usuarios
                </span>
                <span className="font-semibold">
                  Crear Usuario del Sistema
                </span>
              </div>
            </div>

            {/* TEMA */}
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

            <div className="max-w-5xl mx-auto">

              {/*TITULO */}
              <div className="mb-6 space-y-1">
                <h1 className="text-3xl font-bold tracking-tight">
                  Crear Usuario
                </h1>
                <p className="text-muted-foreground">
                  Registra un nuevo usuario del sistema y asigna sus credenciales de acceso.
                </p>
              </div>

              {/* CARD PRINCIPAL */}
              <div className="
                rounded-2xl
                border
                border-border
                bg-background
                shadow-sm
                p-6
                lg:p-8
                relative
                overflow-hidden
              ">

                {/* DETALLE VISUAL SUPERIOR */}
                <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-600 via-indigo-600 to-indigo-800" />

                <UsuarioSistemaForm />

              </div>
            </div>
          </div>

        </SidebarInset>

        <Toaster richColors position="bottom-right" />
      </SidebarProvider>
    </TooltipProvider>
  )
}