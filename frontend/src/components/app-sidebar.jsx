"use client"

import * as React from "react"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarSeparator,
} from "@/components/ui/sidebar"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { LayoutDashboard } from "lucide-react"
import { useRouter, usePathname } from "next/navigation"
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu"
import { API_URL_BASE } from "@/lib/config"

export function AppSidebar({ mainItems = [], footerItems = [] }) {
  const [email, setEmail] = React.useState("")
  const [name, setName] = React.useState("")
  const router = useRouter()
  const pathname = usePathname()

  // Obtener usuario desde backend (cookie JWT)
  React.useEffect(() => {
    const cargarUsuario = async () => {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, {
          method: "GET",
          credentials: "include",
        })

        if (!res.ok) return

        const user = await res.json()

        setEmail(user.username || "")
        setName(user.nombre || user.username?.split("@")[0] || "")

      } catch (error) {
        console.error("Error cargando usuario", error)
      }
    }

    cargarUsuario()
  }, [])

  function normalizePath(text) {
    return `/${String(text).toLowerCase().replace(/\s+/g, "")}`
  }

  function handleSubmit(item) {
    const path = item.path
      ? item.path.startsWith("/")
        ? item.path
        : `/${item.path}`
      : normalizePath(item.title)

    if (!path) return
    router.push(path)
  }

  const handleLogout = async () => {
    try {
      await fetch(`${API_URL_BASE}/auth/logout`, {
        method: "POST",
        credentials: "include",
      })
    } catch (error) {
      console.error("Error cerrando sesión", error)
    }

    router.replace("/") // tu login está en /
  }

  return (
    <Sidebar className="relative overflow-hidden border-r h-screen sticky top-0">

      <div className="bg-gradient-to-b from-blue-600 via-indigo-600 to-indigo-800 h-full flex flex-col">

        {/* HEADER */}
        <SidebarHeader className="p-4 flex items-center gap-3 text-sidebar-foreground">
          <div className="flex size-10 items-center justify-center rounded-lg bg-transparent">
            <img src="/logo.png" className="size-10 object-contain width={20} height={20}" />
          </div>

          <div className="flex flex-col leading-tight">
            <span className="font-semibold text-sm">
              Sistema Jurídico
            </span>
            <span className="text-xs opacity-70">
              v1.0.0
            </span>
          </div>
        </SidebarHeader>

        <SidebarSeparator />

        {/* MENU */}
        <SidebarContent className="px-2 py-4 flex-1">
          <SidebarMenu>
            {mainItems.map((item, index) => {
              const path = normalizePath(item.title)
              const isActive = pathname === path

              return (
                <SidebarMenuItem key={index}>
                  <SidebarMenuButton
                    onClick={() => handleSubmit(item)}
                    className={`
                      rounded-lg transition-all
                      ${isActive
                        ? "text-black dark:text-white font-medium"
                        : "text-sidebar-foreground hover:bg-sidebar-accent/70"
                      }
                    `}
                  >
                    <span>{item.title}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              )
            })}
          </SidebarMenu>
        </SidebarContent>

        <SidebarSeparator />

        {/* FOOTER */}
        <SidebarFooter className="p-4">
          <SidebarMenu>

            {footerItems.map((item, index) => (
              <SidebarMenuItem key={index}>
                <SidebarMenuButton
                  onClick={() => handleSubmit(item)}
                  className="rounded-lg text-sidebar-foreground hover:bg-sidebar-accent/70"
                >
                  <span>{item.title}</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
            ))}

            {/* USUARIO */}
            <SidebarMenuItem className="mt-6">
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <div className="flex items-center gap-3 p-2 rounded-lg cursor-pointer hover:bg-sidebar-accent/70">

                    <Avatar className="size-9">
                      <AvatarImage src="https://github.com/shadcn.png" />
                      <AvatarFallback>
                        {name?.charAt(0).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>

                    <div className="flex flex-col text-sm leading-tight text-sidebar-foreground">
                      <span className="font-medium">{name}</span>
                      <span className="text-xs opacity-70">
                        {email}
                      </span>
                    </div>

                  </div>
                </DropdownMenuTrigger>

                <DropdownMenuContent align="end">
                  <DropdownMenuItem onClick={handleLogout}>
                    Cerrar sesión
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </SidebarMenuItem>

          </SidebarMenu>
        </SidebarFooter>
      </div>
    </Sidebar>
  )
}