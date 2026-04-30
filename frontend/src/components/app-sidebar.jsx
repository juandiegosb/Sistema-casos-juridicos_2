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

export function AppSidebar({ mainItems = [], footerItems = [] }) {
  const [email, setEmail] = React.useState("")
  const [name, setName] = React.useState("")
  const router = useRouter()
  const pathname = usePathname()

  React.useEffect(() => {
    const storedEmail = localStorage.getItem("username") || ""
    setEmail(storedEmail)

    if (storedEmail.includes("@")) {
      setName(storedEmail.split("@")[0])
    }
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

  return (
    <Sidebar className="relative overflow-hidden border-r border-sidebar-border bg-transparent [&_[data-sidebar=sidebar-inner]]:bg-transparent h-screen sticky top-0 border-r border-sidebar-border">

      <div className="bg-gradient-to-b from-blue-600 via-indigo-600 to-indigo-800">

        {/* HEADER */}
        <SidebarHeader className="p-4 flex items-center gap-3 text-sidebar-foreground">
          <div className="flex size-9 items-center justify-center rounded-lg bg-sidebar-accent">
            <LayoutDashboard className="size-5" />
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

        {/* MENÚ */}
        <SidebarContent className="px-2 py-4">
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
              <div className="flex items-center gap-3 p-2 rounded-lg">

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
            </SidebarMenuItem>

          </SidebarMenu>
        </SidebarFooter>
      </div>
    </Sidebar>
  )
}