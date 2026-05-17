"use client"

import React, { useState, useEffect } from 'react'
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { toast } from "sonner"

/**
 * Componente Calendar
 * 
 * Un calendario interactivo basado en FullCalendar para visualizar eventos.
 * Los eventos son de solo lectura y se cargan mediante un efecto simulado.
 */
import { API_URL_BASE } from '@/lib/config'

export default function Calendar() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true)
        // 1. Obtener la información del usuario actual
        const userResponse = await fetch(`${API_URL_BASE}/auth/me`, {
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include'
        })

        if (!userResponse.ok) throw new Error('No se pudo obtener la información del usuario')
        
        const userData = await userResponse.json()
        
        // El user tiene un perfilId que usaremos como autorId para los seguimientos
        const autorId = userData.perfilId

        if (!autorId) {
          setEvents([])
          return
        }

        // 2. Obtener los seguimientos del autor
        const seguimientosResponse = await fetch(`${API_URL_BASE}/seguimientos/autor/${autorId}`, {
          credentials: 'include'
        })
        
        if (!seguimientosResponse.ok) throw new Error('No se pudo obtener los seguimientos')
        
        const seguimientos = await seguimientosResponse.json()

        // 3. Mapear seguimientos a eventos de FullCalendar
        const mappedEvents = seguimientos.map(seg => ({
          id: seg.id.toString(),
          title: seg.descripcion || seg.categoriaSeguimientoNombre || 'Seguimiento',
          start: seg.fechaEntrega, // Formato esperado: YYYY-MM-DD
          allDay: true,
          // Color basado en la categoría o por defecto
          classNames: [
            seg.categoriaSeguimientoNombre?.toLowerCase().includes('audiencia') 
              ? 'bg-primary text-primary-foreground border-primary' 
              : 'bg-secondary text-secondary-foreground border-secondary'
          ],
          extendedProps: {
            ...seg
          }
        }))

        setEvents(mappedEvents)
      } catch (error) {
        console.error('Error cargando eventos:', error)
        toast.error("Error al cargar eventos", {
          description: "No se pudieron obtener los seguimientos del calendario."
        })
      } finally {
        setLoading(false)
      }
    }

    fetchEvents()
  }, [])

  return (
    <Card className="w-full border-border bg-card shadow-sm">
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
        <CardTitle className="text-xl font-semibold text-foreground">
          Calendario de Actividades
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="full-calendar-wrapper relative">
          {loading && (
            <div className="absolute inset-0 z-10 flex items-center justify-center bg-background/50 backdrop-blur-[1px]">
              <div className="flex flex-col items-center gap-2">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
                <p className="text-sm font-medium text-muted-foreground">Cargando seguimientos...</p>
              </div>
            </div>
          )}
          <FullCalendar
            plugins={[dayGridPlugin]}
            initialView="dayGridMonth"
            events={events}
            locale="es"
            headerToolbar={{
              left: 'prev,next today',
              center: 'title',
              right: ''
            }}
            buttonText={{
              today: 'Hoy'
            }}
            height="auto"
            aspectRatio={1.6}
            editable={false}
            selectable={false}
            dayMaxEvents={true}
            eventDisplay="block"
          />
        </div>
      </CardContent>

      <style jsx global>{`
        /* Reset de bordes y fondos para integración con shadcn */
        .full-calendar-wrapper .fc {
          --fc-border-color: var(--border);
          --fc-daygrid-event-dot-width: 8px;
          --fc-today-bg-color: var(--accent);
          background-color: transparent;
          color: var(--foreground);
        }

        /* Toolbar y Títulos */
        .full-calendar-wrapper .fc-toolbar {
          display: grid !important;
          grid-template-columns: 1fr auto 1fr !important;
          gap: 1rem;
          @apply items-center mb-6 !important;
        }

        .full-calendar-wrapper .fc-toolbar-chunk:nth-child(1) {
          @apply flex justify-start;
        }

        .full-calendar-wrapper .fc-toolbar-chunk:nth-child(2) {
          @apply flex justify-center min-w-[200px];
        }

        .full-calendar-wrapper .fc-toolbar-chunk:nth-child(3) {
          @apply flex justify-end;
        }

        .full-calendar-wrapper .fc-toolbar-title {
          @apply text-xl font-bold text-foreground m-0 text-center !important;
          white-space: nowrap;
        }

        .full-calendar-wrapper .fc-button {
          @apply bg-secondary text-secondary-foreground border-border hover:bg-secondary/80 font-medium px-4 py-2 h-9 transition-colors !important;
          background-image: none !important;
          box-shadow: none !important;
          text-transform: capitalize !important;
        }

        .full-calendar-wrapper .fc-button-primary:not(:disabled).fc-button-active,
        .full-calendar-wrapper .fc-button-primary:not(:disabled):active {
          @apply bg-primary text-primary-foreground !important;
        }

        /* Cabecera de días (Solución al blanco sobre blanco) */
        .full-calendar-wrapper .fc-col-header-cell {
          @apply py-3 bg-muted/50 font-semibold text-muted-foreground border-border;
        }

        .full-calendar-wrapper .fc-col-header-cell-cushion {
          @apply text-sm;
        }

        /* Celdas del calendario */
        .full-calendar-wrapper .fc-daygrid-day {
          @apply border-border transition-colors;
        }

        .full-calendar-wrapper .fc-daygrid-day:hover {
          @apply bg-muted/30;
        }

        .full-calendar-wrapper .fc-day-today {
          @apply bg-accent/20 !important;
        }

        .full-calendar-wrapper .fc-daygrid-day-number {
          @apply p-2 text-sm text-muted-foreground font-medium;
        }

        /* Eventos */
        .full-calendar-wrapper .fc-event {
          @apply rounded-md border px-2 py-0.5 text-xs font-medium shadow-sm transition-all cursor-default !important;
        }

        .full-calendar-wrapper .fc-daygrid-event {
          @apply my-0.5 mx-1 !important;
        }

        /* Ajustes específicos para modo oscuro */
        .dark .full-calendar-wrapper .fc-theme-standard td,
        .dark .full-calendar-wrapper .fc-theme-standard th {
          border-color: var(--border);
        }

        .dark .full-calendar-wrapper .fc-daygrid-day-number,
        .dark .full-calendar-wrapper .fc-col-header-cell-cushion {
          color: var(--muted-foreground);
        }

        .dark .full-calendar-wrapper .fc-toolbar-title {
          color: var(--foreground);
        }
      `}</style>
    </Card>
  )
}
