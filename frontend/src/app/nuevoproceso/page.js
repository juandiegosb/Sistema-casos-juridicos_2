"use client"

import {
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { PermissionSidebar } from "@/components/navigation/PermissionSidebar"
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

import { NuevoProcesoForm } from "@/components/forms/NuevoProcesosForm"

export default function NuevoProcesoPage() {
  const { setTheme } = useTheme()

  return (
    <TooltipProvider>
      <SidebarProvider>
        <PermissionSidebar />

        <SidebarInset className="bg-muted/30 min-h-screen">
          <header className="flex h-16 items-center justify-between border-b bg-background/80 backdrop-blur px-6">
            <div className="flex items-center gap-4">
              <SidebarTrigger />
              <div className="flex flex-col leading-tight">
                <span className="text-sm text-muted-foreground">
                  Procesos / Nuevo proceso
                </span>
                <span className="font-semibold">
                  Registro de proceso
                </span>
              </div>
            </div>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="icon">
                  <Sun className="h-5 w-5 dark:scale-0" />
                  <Moon className="absolute h-5 w-5 scale-0 dark:scale-100" />
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

          <div className="p-6 lg:p-10">
            <div className="max-w-5xl mx-auto">
              <div className="mb-6">
                <h1 className="text-3xl font-bold">
                  Nuevo Proceso
                </h1>
                <p className="text-muted-foreground">
                  Registra un proceso asociado a una consulta jurídica.
                </p>
              </div>

              <div className="rounded-2xl border bg-background shadow-sm p-6 lg:p-8 relative overflow-hidden">
                <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-600 via-indigo-600 to-indigo-800" />

                <NuevoProcesoForm />
              </div>
            </div>
          </div>
        </SidebarInset>

        <Toaster richColors position="bottom-right" />
      </SidebarProvider>
    </TooltipProvider>
  )
}