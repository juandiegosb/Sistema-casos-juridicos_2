"use client"

import { SidebarProvider, SidebarInset, SidebarTrigger } from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { TooltipProvider } from "@/components/ui/tooltip"
import { Toaster } from "@/components/ui/sonner"
import { Moon, Sun } from "lucide-react"
import { useTheme } from "next-themes"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { ConciliadorForm } from "@/components/forms/ConciliadorForm"

export default function NuevoConciliador() {
  const { setTheme } = useTheme()
  const router = useRouter()

  
  

  const handleSuccess = () => {
    router.push("/conciliadores")
  }

  return (
    <TooltipProvider>
      <SidebarProvider>
        <PermissionSidebar />

        <SidebarInset className="bg-muted/30 min-h-screen">

          {/*HEADER MODERNO */}
          <header className="flex h-16 items-center justify-between border-b bg-background/80 backdrop-blur px-6">

            <div className="flex items-center gap-4">
              <SidebarTrigger />

              <div className="flex flex-col leading-tight">
                <span className="text-sm text-muted-foreground">
                  Conciliadores / Gestión
                </span>
                <span className="font-semibold">
                  Nuevo Conciliador
                </span>
              </div>
            </div>

            {/* Tema */}
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

          {/*CONTENIDO */}
          <div className="p-6 lg:p-10">

            <div className="max-w-5xl mx-auto">


              {/*CARD PRINCIPAL */}
              <div className="rounded-2xl border border-border bg-background shadow-sm p-6 lg:p-8 relative overflow-hidden">

                {/*detalle de color sutil */}
                <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-600 via-indigo-500 to-purple-600" />

                <ConciliadorForm onSuccess={handleSuccess} />

              </div>

            </div>
          </div>

        </SidebarInset>

        <Toaster richColors position="bottom-right" />
      </SidebarProvider>
    </TooltipProvider>
  )
}