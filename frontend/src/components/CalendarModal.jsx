"use client"

import * as React from "react"
import { Calendar as CalendarIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogTrigger,
  DialogTitle,
} from "@/components/ui/dialog"
import Calendar from "@/components/Calendar"

/**
 * Muestra un botón que abre un modal con el calendario.
 * @returns {JSX.Element} Modal de calendario.
 */
export function CalendarModal() {
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="outline" size="icon" title="Abrir Calendario">
          <CalendarIcon className="h-5 w-5" />
        </Button>
      </DialogTrigger>
      {/* 
        Ajuste de tamaño 85% del viewport dejando aprox 15% libre.
      */}
      <DialogContent className="max-w-[85vw] sm:max-w-[85vw] w-[85vw] h-[85vh] flex flex-col p-6">
        <DialogTitle className="sr-only">Calendario de Eventos</DialogTitle>
        <div className="flex-1 overflow-hidden rounded-md border mt-2">
          <Calendar />
        </div>
      </DialogContent>
    </Dialog>
  )
}
