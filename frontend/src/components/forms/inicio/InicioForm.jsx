"use client"

import * as React from "react"
import { useRouter } from "next/navigation"
import { RefreshCw, LayoutDashboard } from "lucide-react"
import { Button } from "@/components/ui/button"
import { API_URL_BASE } from "@/lib/config"
import { PERMISOS } from "@/lib/permission"
import { tienePermiso, esAdministrativo, esAsesor, esMonitor, esEstudiante } from "@/lib/authz"

// ─── helpers ──────────────────────────────────────────────────────────────────

function calcularSemestreActual() {
  const hoy = new Date()
  return { año: hoy.getFullYear(), semestre: hoy.getMonth() >= 6 ? 2 : 1 }
}

function etiquetaEstado(nombre) {
  const mapa = {
    PENDIENTE: "Pendiente",
    SENTENCIA_FAVORABLE: "Sentencia favorable",
    SENTENCIA_DESFAVORABLE: "Sentencia desfavorable",
    CONCILIADO: "Conciliado",
    ARCHIVADO: "Archivado",
    EN_TRAMITE: "En trámite",
    DESISTIDO: "Desistido",
  }
  return mapa[nombre] || nombre
}

// ─── colores chart del tema ───────────────────────────────────────────────────
const CHART_COLORS = [
  "var(--chart-1)", "var(--chart-2)", "var(--chart-3)", "var(--chart-4)", "var(--chart-5)",
]

// ─── Tarjetas de métricas con color sólido ────────────────────────────────────
const METRIC_CARDS = [
  { key: "totalConsultas",         label: "Total consultas",    cls: "bg-primary text-primary-foreground" },
  { key: "consultasFinalizadas",   label: "Finalizadas",        cls: "bg-destructive text-destructive-foreground" },
  { key: "consultasPendientes",    label: "Pendientes",         cls: "bg-[hsl(var(--chart-2))] text-primary-foreground" },
  { key: "totalPersonasAtendidas", label: "Personas atendidas", cls: "bg-[hsl(var(--chart-3))] text-primary-foreground" },
]

function MetricCard({ label, value, cls }) {
  return (
    <div className={`${cls} rounded-lg px-5 py-4 flex flex-col gap-1 min-w-0`}>
      <span className="text-xs font-semibold uppercase tracking-wide opacity-80">{label}</span>
      <span className="text-4xl font-extrabold leading-none">{value ?? "—"}</span>
    </div>
  )
}

// ─── Donut chart SVG ──────────────────────────────────────────────────────────
function DonutChart({ finalizadas, pendientes }) {
  const total = finalizadas + pendientes
  if (total === 0)
    return <p className="text-sm text-muted-foreground text-center py-8">Sin consultas en este semestre</p>

  const pctFin = Math.round((finalizadas / total) * 100)
  const r = 68, cx = 88, cy = 88
  const circ = 2 * Math.PI * r
  const dash = (finalizadas / total) * circ

  return (
    <div className="flex flex-col items-center gap-4">
      <svg width={176} height={176} viewBox="0 0 176 176">
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="var(--muted)" strokeWidth={24} />
        {finalizadas > 0 && (
          <circle cx={cx} cy={cy} r={r} fill="none"
            stroke="var(--destructive)" strokeWidth={24}
            strokeDasharray={`${dash} ${circ}`}
            transform={`rotate(-90 ${cx} ${cy})`} />
        )}
        <text x={cx} y={cy - 8} textAnchor="middle"
          style={{ fontSize: 26, fontWeight: 800, fill: "var(--destructive)" }}>
          {pctFin}%
        </text>
        <text x={cx} y={cy + 14} textAnchor="middle"
          style={{ fontSize: 11, fill: "var(--muted-foreground)" }}>
          finalizadas
        </text>
      </svg>
      <div className="flex gap-5 text-sm">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-destructive" />
          <span>Finalizadas <strong>({finalizadas})</strong></span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-muted border border-border" />
          <span>Pendientes <strong>({pendientes})</strong></span>
        </div>
      </div>
    </div>
  )
}

// ─── Bar chart SVG por área ───────────────────────────────────────────────────
function BarChartAreas({ areas }) {
  if (!areas || areas.length === 0)
    return <p className="text-sm text-muted-foreground text-center py-8">Sin datos de áreas para este período</p>

  const W = 500, H = 190
  const pad = { top: 18, right: 16, bottom: 40, left: 34 }
  const chartW = W - pad.left - pad.right
  const chartH = H - pad.top - pad.bottom
  const maxVal = Math.max(...areas.map((a) => a.cantidad), 1)
  const barW = Math.min(40, (chartW / areas.length) - 12)

  return (
    <svg width="100%" viewBox={`0 0 ${W} ${H}`} className="overflow-visible">
      {[0, 0.25, 0.5, 0.75, 1].map((f) => {
        const y = pad.top + chartH * (1 - f)
        return (
          <React.Fragment key={f}>
            <line x1={pad.left} x2={W - pad.right} y1={y} y2={y}
              stroke="var(--border)" strokeWidth={1} strokeDasharray="4 3" />
            <text x={pad.left - 5} y={y + 4} textAnchor="end"
              style={{ fontSize: 9, fill: "var(--muted-foreground)" }}>
              {Math.round(maxVal * f)}
            </text>
          </React.Fragment>
        )
      })}
      {areas.map((area, i) => {
        const slotW = chartW / areas.length
        const x = pad.left + slotW * i + (slotW - barW) / 2
        const bH = (area.cantidad / maxVal) * chartH
        const y = pad.top + chartH - bH
        const color = CHART_COLORS[i % CHART_COLORS.length]
        return (
          <React.Fragment key={area.nombre}>
            <rect x={x} y={y} width={barW} height={bH} fill={color} rx={3} />
            <text x={x + barW / 2} y={y - 5} textAnchor="middle"
              style={{ fontSize: 10, fontWeight: 700, fill: color }}>
              {area.cantidad}
            </text>
            <text x={x + barW / 2} y={H - pad.bottom + 14} textAnchor="middle"
              style={{ fontSize: 10, fill: "var(--muted-foreground)" }}>
              {area.nombre.length > 9 ? area.nombre.slice(0, 8) + "…" : area.nombre}
            </text>
          </React.Fragment>
        )
      })}
      <line x1={pad.left} x2={W - pad.right}
        y1={pad.top + chartH} y2={pad.top + chartH}
        stroke="var(--border)" strokeWidth={1} />
    </svg>
  )
}

// ─── Barras de procesos por estado ────────────────────────────────────────────
function ProcesosBarras({ procesos }) {
  if (!procesos || procesos.length === 0)
    return <p className="text-sm text-muted-foreground">Sin procesos para este período.</p>

  const maxVal = Math.max(...procesos.map((p) => p.cantidad), 1)
  return (
    <div className="space-y-4">
      {procesos.map((p, i) => {
        const pct = Math.round((p.cantidad / maxVal) * 100)
        const color = CHART_COLORS[i % CHART_COLORS.length]
        return (
          <div key={p.nombre}>
            <div className="flex justify-between text-sm mb-1.5">
              <span className="font-medium text-foreground">{etiquetaEstado(p.nombre)}</span>
              <span className="font-bold" style={{ color }}>{p.cantidad}</span>
            </div>
            <div className="w-full h-2 rounded-full bg-muted">
              <div className="h-2 rounded-full transition-all duration-500"
                style={{ width: `${pct}%`, backgroundColor: color }} />
            </div>
          </div>
        )
      })}
    </div>
  )
}

// ─── Panel wrapper ────────────────────────────────────────────────────────────
function Panel({ title, badge, children }) {
  return (
    <div className="bg-card border border-border rounded-xl shadow-sm overflow-hidden">
      {title && (
        <div className="flex justify-between items-center px-5 py-3.5 border-b border-border">
          <span className="font-bold text-sm text-card-foreground">{title}</span>
          {badge && (
            <span className="bg-destructive text-destructive-foreground text-xs font-bold px-2.5 py-0.5 rounded">
              {badge}
            </span>
          )}
        </div>
      )}
      <div className="p-5">{children}</div>
    </div>
  )
}

// ─── Skeleton ─────────────────────────────────────────────────────────────────
function Skeleton() {
  return <div className="rounded-lg bg-muted animate-pulse h-24" />
}

// ─── Componente exportado ─────────────────────────────────────────────────────
export function InicioForm() {
  const router = useRouter()
  const [user, setUser] = React.useState(null)
  const [stats, setStats] = React.useState(null)
  const [cargando, setCargando] = React.useState(true)
  const [error, setError] = React.useState("")
  const { año, semestre } = calcularSemestreActual()

  const cargarStats = React.useCallback(async (u) => {
    setCargando(true); setError("")
    try {
      let url = null
      if (tienePermiso(u, PERMISOS.VER_REPORTES) || esAdministrativo(u)) {
        url = `${API_URL_BASE}/estadisticas/${año}/semestre/${semestre}`
      } else if (tienePermiso(u, PERMISOS.VER_CONSULTAS)) {
        const pid = u.perfilId
        if (!pid) { setStats(null); return }
        if (esAsesor(u))      url = `${API_URL_BASE}/estadisticas/${año}/semestre/${semestre}/asesor/${pid}`
        else if (esMonitor(u))    url = `${API_URL_BASE}/estadisticas/${año}/semestre/${semestre}/monitor/${pid}`
        else if (esEstudiante(u)) url = `${API_URL_BASE}/estadisticas/${año}/semestre/${semestre}/estudiante/${pid}`
      }
      if (!url) { setStats(null); return }
      const res = await fetch(url, { credentials: "include" })
      if (!res.ok) { setStats(null); return }
      setStats(await res.json())
    } catch (err) {
      console.error(err); setError("No se pudieron cargar las estadísticas.")
    } finally {
      setCargando(false)
    }
  }, [año, semestre])

  React.useEffect(() => {
    async function init() {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, { credentials: "include" })
        if (res.status === 401) { router.replace("/"); return }
        if (!res.ok) { setCargando(false); return }
        const u = await res.json()
        setUser(u); await cargarStats(u)
      } catch (err) { console.error(err); setCargando(false) }
    }
    init()
  }, [router, cargarStats])

  const nombreRol = user?.tipoPerfil
    ? user.tipoPerfil.charAt(0) + user.tipoPerfil.slice(1).toLowerCase()
    : null

  const esAdmin = user
    ? tienePermiso(user, PERMISOS.VER_REPORTES) || esAdministrativo(user)
    : false

  const etiquetaSemestre = `${año}-${semestre}`

  return (
    <div className="p-6 lg:p-10 space-y-6 bg-background min-h-full">

      {/* Encabezado */}
      <div className="flex flex-wrap justify-between items-start gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <LayoutDashboard className="w-5 h-5 text-primary" />
            <h1 className="text-2xl font-extrabold text-foreground">
              Bienvenido{user?.nombre ? `, ${user.nombre}` : ""}
            </h1>
          </div>
          <p className="text-sm text-muted-foreground">
            {nombreRol ? `${nombreRol} · ` : ""}Panel de control · Semestre {etiquetaSemestre}
          </p>
        </div>
        {user && (
          <Button variant="outline" size="sm" onClick={() => cargarStats(user)} disabled={cargando} className="gap-2">
            <RefreshCw className={`w-4 h-4 ${cargando ? "animate-spin" : ""}`} />
            Actualizar
          </Button>
        )}
      </div>

      {/* Error */}
      {error && (
        <div className="rounded-lg border border-destructive/40 bg-destructive/10 px-4 py-3 text-destructive text-sm">
          {error}
        </div>
      )}

      {/* Skeleton */}
      {cargando && (
        <div className="space-y-4">
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
            {[1,2,3,4].map((i) => <Skeleton key={i} />)}
          </div>
          <Skeleton />
          <div className="grid lg:grid-cols-2 gap-4"><Skeleton /><Skeleton /></div>
        </div>
      )}

      {/* Stats reales */}
      {!cargando && stats && (
        <>
          {/* Fila 1: tarjetas de color sólido */}
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
            {METRIC_CARDS.map(({ key, label, cls }) => (
              <MetricCard key={key} label={label} value={stats[key]} cls={cls} />
            ))}
          </div>

          {/* Fila 2: solo admin ve el bar chart de áreas */}
          {esAdmin && stats.consultasPorArea && stats.consultasPorArea.length > 0 && (
            <Panel title="Consultas por área" badge={`Total ${stats.totalConsultas}`}>
              <BarChartAreas areas={stats.consultasPorArea} />
            </Panel>
          )}

          {/* Fila 3: donut + procesos */}
          <div className="grid lg:grid-cols-2 gap-4">
            <Panel
              title="Estado de consultas"
              badge={`${Math.round((stats.consultasFinalizadas / (stats.totalConsultas || 1)) * 100)}% completado`}
            >
              <DonutChart
                finalizadas={stats.consultasFinalizadas}
                pendientes={stats.consultasPendientes}
              />
            </Panel>

            <Panel title="Procesos por estado">
              <ProcesosBarras procesos={stats.procesosPorEstado} />
            </Panel>
          </div>

          <p className="text-xs text-muted-foreground text-right">
            Período: {stats.periodoInicio} → {stats.periodoFin}
          </p>
        </>
      )}

      {/* Sin datos */}
      {!cargando && !stats && !error && (
        <div className="text-center py-20 text-muted-foreground">
          <LayoutDashboard className="w-12 h-12 mx-auto mb-3 opacity-25" />
          <p className="text-sm">No hay estadísticas disponibles para el semestre actual.</p>
        </div>
      )}
    </div>
  )
}
