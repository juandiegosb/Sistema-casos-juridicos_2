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

import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/components/ui/tabs"

import { TemaForm } from "@/components/forms/TemaForm"
import { TipoForm } from "@/components/forms/TipoForm"
import { AreaForm } from "@/components/forms/AreaForm"
import { RolePermissionsForm } from "@/components/forms/RolePermissionsForm"
import { CambiarRolUsuarioForm } from "@/components/forms/CambiarRolUsuarioForm"

export default function ConfiguracionCatalogos() {
  const { setTheme } = useTheme()

  return (
    <TooltipProvider>
      <SidebarProvider>
        <PermissionSidebar />

        <SidebarInset className="bg-muted/30 min-h-screen">

          {/* HEADER */}
          <header className="flex h-16 items-center justify-between border-b bg-background/80 backdrop-blur px-6">
            <div className="flex items-center gap-4">
              <SidebarTrigger />
              <div className="flex flex-col leading-tight">
                <span className="text-sm text-muted-foreground">
                  Configuración / Catálogos
                </span>
                <span className="font-semibold">
                  Gestión de Parámetros
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

          {/* CONTENIDO */}
          <div className="p-6 lg:p-10">
            <div className="max-w-5xl mx-auto">

              {/* TITULO */}
              <div className="mb-6">
                <h1 className="text-3xl font-bold">
                  Catálogos del Sistema
                </h1>
                <p className="text-muted-foreground">
                  Administra los parámetros base del sistema jurídico.
                </p>
              </div>

              {/* CARD */}
              <div className="rounded-2xl border bg-background shadow-sm p-6 lg:p-8 relative overflow-hidden">

                {/* línea superior */}
                <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-600 via-indigo-600 to-indigo-800" />

                {/* TABS */}
                <Tabs defaultValue="tema" className="space-y-6">
                  <TabsList className="flex w-full items-end justify-start gap-1 rounded-none bg-transparent p-0 border-b overflow-x-auto">
                    <TabsTrigger
                      value="tema"
                      className="rounded-t-xl rounded-b-none border border-b-0 bg-muted/60 px-6 py-2 data-[state=active]:bg-background data-[state=active]:shadow-none whitespace-nowrap"
                    >
                      Tema
                    </TabsTrigger>
                    <TabsTrigger
                      value="tipo"
                      className="rounded-t-xl rounded-b-none border border-b-0 bg-muted/60 px-6 py-2 data-[state=active]:bg-background data-[state=active]:shadow-none whitespace-nowrap"
                    >
                      Tipo
                    </TabsTrigger>
                    <TabsTrigger
                      value="area"
                      className="rounded-t-xl rounded-b-none border border-b-0 bg-muted/60 px-6 py-2 data-[state=active]:bg-background data-[state=active]:shadow-none whitespace-nowrap"
                    >
                      Área
                    </TabsTrigger>
                    <TabsTrigger
                      value="permisos-rol"
                      className="rounded-t-xl rounded-b-none border border-b-0 bg-muted/60 px-6 py-2 data-[state=active]:bg-background data-[state=active]:shadow-none whitespace-nowrap"
                    >
                      Permisos
                    </TabsTrigger>
                    <TabsTrigger
                      value="cambiar-rol"
                      className="rounded-t-xl rounded-b-none border border-b-0 bg-muted/60 px-6 py-2 data-[state=active]:bg-background data-[state=active]:shadow-none whitespace-nowrap"
                    >
                      Cambiar Rol
                    </TabsTrigger>
                  </TabsList>

                  <TabsContent value="tema" className="pt-4">
                    <TemaForm />
                  </TabsContent>
                  <TabsContent value="tipo" className="pt-4">
                    <TipoForm />
                  </TabsContent>
                  <TabsContent value="area" className="pt-4">
                    <AreaForm />
                  </TabsContent>
                  <TabsContent value="permisos-rol" className="pt-4">
                    <RolePermissionsForm />
                  </TabsContent>
                  <TabsContent value="cambiar-rol" className="pt-4">
                    <CambiarRolUsuarioForm />
                  </TabsContent>
                </Tabs>
              </div>
            </div>
          </div>

        </SidebarInset>

        <Toaster richColors position="bottom-right" />
      </SidebarProvider>
    </TooltipProvider>
  )
}