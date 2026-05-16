"use client"

import * as React from "react"
import {
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { TooltipProvider } from "@/components/ui/tooltip"
import { Moon, Sun, ClipboardList } from "lucide-react"
import { useTheme } from "next-themes"
import { Button } from "@/components/ui/button"
import { Toaster } from "@/components/ui/sonner"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Card, CardContent } from "@/components/ui/card"
import { PermissionSidebar } from "@/components/navigation/PermissionSidebar"
import { SeguimientosForm } from "@/components/forms/SeguimientosForm"

export default function SeguimientosPage() {
  const { setTheme } = useTheme()

  return (
    <TooltipProvider>
      <SidebarProvider className="min-h-screen">
        <PermissionSidebar />

        <SidebarInset className="bg-muted/30 min-h-screen">
          <header className="flex h-16 items-center justify-between border-b bg-background/80 backdrop-blur px-6">
            <div className="flex items-center gap-4">
              <SidebarTrigger />

              <div className="flex flex-col leading-tight">
                <span className="text-sm text-muted-foreground">
                  Control Tareas
                </span>
                <span className="font-semibold">
                  Organizacion de tareas y seguimiento de proyectos
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
            <div className="max-w-6xl mx-auto space-y-6">
              <div className="flex items-center gap-3">
                <div className="p-3 rounded-xl bg-primary/10">
                  <ClipboardList className="w-6 h-6 text-primary" />
                </div>

                <div>
                  <h1 className="text-3xl font-bold tracking-tight">
                    Tareas
                  </h1>
                </div>
              </div>

              <Card>
                <CardContent className="p-6 lg:p-8">
                  <SeguimientosForm />
                </CardContent>
              </Card>
            </div>
          </div>
        </SidebarInset>

        <Toaster richColors position="bottom-right" />
      </SidebarProvider>
    </TooltipProvider>
  )
}