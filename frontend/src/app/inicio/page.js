"use client"

import * as React from "react"
import {
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { TooltipProvider } from "@/components/ui/tooltip"
import { Moon, Sun, FileText, Users, Activity, TrendingUp } from "lucide-react"
import { useTheme } from "next-themes"
import { Button } from "@/components/ui/button"
import { Toaster } from "@/components/ui/sonner"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { PermissionSidebar } from "@/components/navigation/PermissionSidebar" 

export default function Home() {
  const { setTheme } = useTheme()

  const stats = [
    {
      icon: FileText,
      label: "Casos Activos",
      value: "99",
      change: "+12%",
      color: "text-blue-600",
    },
    {
      icon: Users,
      label: "Usuarios",
      value: "99",
      change: "+5%",
      color: "text-purple-600",
    },
    {
      icon: Activity,
      label: "Consultas Hoy",
      value: "99",
      change: "+18%",
      color: "text-green-600",
    },
    {
      icon: TrendingUp,
      label: "Casos Resueltos",
      value: "99",
      change: "+8%",
      color: "text-orange-600",
    },
  ]

  const recentActivity = [
    { id: 1, title: "Nuevo caso registrado", user: "N/N", time: "Hace 66 min" },
    { id: 2, title: "Consulta actualizada", user: "N/N", time: "Hace 66 min" },
    { id: 3, title: "Usuario creado", user: "N/N", time: "Hace 66 hora" },
    { id: 4, title: "Caso cerrado", user: "N/N", time: "Hace 66 horas" },
  ]

  return (
    <TooltipProvider>
      <SidebarProvider className="min-h-screen">
        <PermissionSidebar />

        <SidebarInset className="bg-muted/30 min-h-screen">

          {/* HEADER */}
          <header className="flex h-16 items-center justify-between border-b bg-background/80 backdrop-blur px-6">
            <div className="flex items-center gap-4">
              <SidebarTrigger />
              <h2 className="font-semibold">Home</h2>
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
          <div className="p-6 lg:p-10 space-y-8">

            {/* TITULO */}
            <div>
              <h1 className="text-3xl font-bold">Bienvenido</h1>
              <p className="text-muted-foreground">
                Panel de control del consultorio jurídico
              </p>
            </div>

            {/* STATS */}
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
              {stats.map((stat) => {
                const Icon = stat.icon
                return (
                  <Card key={stat.label}>
                    <CardContent className="pt-6">
                      <div className="flex justify-between">
                        <div>
                          <p className="text-sm text-muted-foreground">
                            {stat.label}
                          </p>
                          <p className="text-3xl font-bold">{stat.value}</p>
                          <p className="text-sm text-green-600">
                            {stat.change}
                          </p>
                        </div>

                        <div className={`p-3 rounded-lg bg-muted ${stat.color}`}>
                          <Icon className="w-5 h-5" />
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                )
              })}
            </div>

            {/* GRID INFERIOR */}
            <div className="grid lg:grid-cols-2 gap-6">

              {/* ACTIVIDAD */}
              <Card>
                <CardHeader>
                  <CardTitle>Actividad Reciente</CardTitle>
                </CardHeader>

                <CardContent className="space-y-4">
                  {recentActivity.map((item) => (
                    <div key={item.id} className="flex gap-3 border-b pb-3 last:border-0">
                      <div className="w-2 h-2 bg-primary rounded-full mt-2" />
                      <div>
                        <p>{item.title}</p>
                        <p className="text-sm text-muted-foreground">
                          {item.user} • {item.time}
                        </p>
                      </div>
                    </div>
                  ))}
                </CardContent>
              </Card>

              {/* PROGRESO */}
              <Card>
                <CardHeader>
                  <CardTitle>Casos por Estado</CardTitle>
                </CardHeader>

                <CardContent className="space-y-4">

                  {[
                    { label: "En proceso", value: 50, color: "bg-blue-600" },
                    { label: "Pendientes", value: 50, color: "bg-orange-500" },
                    { label: "Resueltos", value: 50, color: "bg-green-600" },
                  ].map((item, i) => (
                    <div key={i}>
                      <div className="flex justify-between text-sm mb-1">
                        <span className="text-muted-foreground">{item.label}</span>
                        <span>{item.value}%</span>
                      </div>

                      <div className="w-full bg-muted rounded-full h-2">
                        <div
                          className={`${item.color} h-2 rounded-full`}
                          style={{ width: `${item.value}%` }}
                        />
                      </div>
                    </div>
                  ))}

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