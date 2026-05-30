"use client";

import React, { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { API_URL_BASE } from "@/lib/config";
import { PERMISOS } from "@/lib/permission";
import { tienePermiso } from "@/lib/authz";
import { RefreshCw, Download, FileBarChart2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";

function calcularSemestreActual() {
  const hoy = new Date();
  return { año: hoy.getFullYear(), semestre: hoy.getMonth() >= 6 ? 2 : 1 };
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
  };
  return mapa[nombre] || nombre;
}

// Solo variables del tema azul — sin rojo
const METRIC_CARDS = [
  { key: "totalConsultas",         label: "Total consultas",    cls: "bg-primary text-primary-foreground" },
  { key: "consultasFinalizadas",   label: "Finalizadas",        cls: "bg-chart-1 text-primary-foreground" },
  { key: "consultasPendientes",    label: "Pendientes",         cls: "bg-chart-2 text-primary-foreground" },
  { key: "totalPersonasAtendidas", label: "Personas atendidas", cls: "bg-secondary text-secondary-foreground" },
];

const CHART_COLORS = [
  "var(--chart-1)", "var(--chart-2)", "var(--chart-3)", "var(--chart-4)", "var(--chart-5)",
];

function MetricCard({ label, value, cls }) {
  return (
    <div className={`${cls} rounded-lg px-4 py-3 flex flex-col gap-0.5 min-w-0`}>
      <span className="text-[10px] font-semibold uppercase tracking-wider opacity-75">{label}</span>
      <span className="text-3xl font-extrabold leading-tight">{value ?? "—"}</span>
    </div>
  );
}

function BarChartAreas({ areas }) {
  if (!areas || areas.length === 0)
    return <p className="text-xs text-muted-foreground text-center py-4">Sin datos de áreas</p>;

  const W = 480, H = 150;
  const pad = { top: 14, right: 12, bottom: 32, left: 28 };
  const chartW = W - pad.left - pad.right;
  const chartH = H - pad.top - pad.bottom;
  const maxVal = Math.max(...areas.map((a) => a.cantidad), 1);
  const barW = Math.min(38, (chartW / areas.length) - 10);

  return (
    <svg width="100%" viewBox={`0 0 ${W} ${H}`} className="overflow-visible">
      {[0, 0.5, 1].map((f) => {
        const y = pad.top + chartH * (1 - f);
        return (
          <React.Fragment key={f}>
            <line x1={pad.left} x2={W - pad.right} y1={y} y2={y}
              stroke="var(--border)" strokeWidth={1} strokeDasharray="4 3" />
            <text x={pad.left - 4} y={y + 3} textAnchor="end"
              style={{ fontSize: 8, fill: "var(--muted-foreground)" }}>
              {Math.round(maxVal * f)}
            </text>
          </React.Fragment>
        );
      })}

      {areas.map((area, i) => {
        const slotW = chartW / areas.length;
        const x = pad.left + slotW * i + (slotW - barW) / 2;
        const bH = Math.max((area.cantidad / maxVal) * chartH, 2);
        const y = pad.top + chartH - bH;
        const color = CHART_COLORS[i % CHART_COLORS.length];
        return (
          <React.Fragment key={area.nombre}>
            <rect x={x} y={y} width={barW} height={bH} fill={color} rx={3} />
            <text x={x + barW / 2} y={y - 4} textAnchor="middle"
              style={{ fontSize: 9, fontWeight: 700, fill: color }}>
              {area.cantidad}
            </text>
            <text x={x + barW / 2} y={H - pad.bottom + 12} textAnchor="middle"
              style={{ fontSize: 9, fill: "var(--muted-foreground)" }}>
              {area.nombre.length > 9 ? area.nombre.slice(0, 8) + "…" : area.nombre}
            </text>
          </React.Fragment>
        );
      })}
      <line x1={pad.left} x2={W - pad.right}
        y1={pad.top + chartH} y2={pad.top + chartH}
        stroke="var(--border)" strokeWidth={1} />
    </svg>
  );
}

function DonutChart({ finalizadas, pendientes }) {
  const total = finalizadas + pendientes;
  if (total === 0)
    return <p className="text-xs text-muted-foreground text-center py-4">Sin consultas</p>;

  const pctFin = Math.round((finalizadas / total) * 100);
  const r = 52, cx = 66, cy = 66;
  const circ = 2 * Math.PI * r;
  const dash = (finalizadas / total) * circ;

  return (
    <div className="flex items-center gap-4">
      <svg width={132} height={132} viewBox="0 0 132 132" className="shrink-0">
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="var(--accent)" strokeWidth={20} />
        {finalizadas > 0 && (
          <circle cx={cx} cy={cy} r={r} fill="none"
            stroke="var(--primary)" strokeWidth={20}
            strokeDasharray={`${dash} ${circ}`}
            transform={`rotate(-90 ${cx} ${cy})`} />
        )}
        <text x={cx} y={cy - 6} textAnchor="middle"
          style={{ fontSize: 20, fontWeight: 800, fill: "var(--primary)" }}>
          {pctFin}%
        </text>
        <text x={cx} y={cy + 12} textAnchor="middle"
          style={{ fontSize: 9, fill: "var(--muted-foreground)" }}>
          finalizadas
        </text>
      </svg>

      <div className="flex flex-col gap-2 text-xs">
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-primary shrink-0" />
          <span className="text-foreground">Finalizadas</span>
          <strong className="ml-auto pl-3">{finalizadas}</strong>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-accent border border-border shrink-0" />
          <span className="text-foreground">Pendientes</span>
          <strong className="ml-auto pl-3">{pendientes}</strong>
        </div>
      </div>
    </div>
  );
}

function ProcesosBarras({ procesos }) {
  if (!procesos || procesos.length === 0)
    return <p className="text-xs text-muted-foreground">Sin procesos registrados.</p>;

  const maxVal = Math.max(...procesos.map((p) => p.cantidad), 1);
  return (
    <div className="space-y-2.5">
      {procesos.map((p, i) => {
        const pct = Math.round((p.cantidad / maxVal) * 100);
        const color = CHART_COLORS[i % CHART_COLORS.length];
        return (
          <div key={p.nombre}>
            <div className="flex justify-between text-xs mb-1">
              <span className="font-medium text-foreground">{etiquetaEstado(p.nombre)}</span>
              <span className="font-bold" style={{ color }}>{p.cantidad}</span>
            </div>
            <div className="w-full h-1.5 rounded-full bg-accent">
              <div className="h-1.5 rounded-full transition-all duration-500"
                style={{ width: `${pct}%`, backgroundColor: color }} />
            </div>
          </div>
        );
      })}
    </div>
  );
}

function Panel({ title, badge, children, className = "" }) {
  return (
    <div className={`bg-card border border-border rounded-xl overflow-hidden ${className}`}>
      {title && (
        <div className="flex justify-between items-center px-4 py-2.5 border-b border-border">
          <span className="font-bold text-xs text-card-foreground uppercase tracking-wide">{title}</span>
          {badge && (
            <span className="bg-primary text-primary-foreground text-[10px] font-bold px-2 py-0.5 rounded">
              {badge}
            </span>
          )}
        </div>
      )}
      <div className="p-4">{children}</div>
    </div>
  );
}

function Skeleton({ className = "h-20" }) {
  return <div className={`rounded-xl bg-muted animate-pulse ${className}`} />;
}

export function EstadisticasForm() {
  const router = useRouter();
  const [checking, setChecking] = useState(true);
  const [semestres, setSemestres] = useState([]);
  const [semSeleccionado, setSemSeleccionado] = useState(null);
  const [stats, setStats] = useState(null);
  const [cargando, setCargando] = useState(false);
  const [descargando, setDescargando] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function init() {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, { credentials: "include" });
        if (res.status === 401) { router.replace("/"); return; }
        const user = await res.json();
        if (!tienePermiso(user, PERMISOS.VER_REPORTES)) { router.replace("/inicio"); return; }

        const resSem = await fetch(`${API_URL_BASE}/estadisticas/semestres`, { credentials: "include" });
        if (resSem.ok) {
          const lista = await resSem.json();
          setSemestres(lista);
          const actual = calcularSemestreActual();
          const found = lista.find((s) => s.año === actual.año && s.semestre === actual.semestre);
          setSemSeleccionado(found ? found.etiqueta : lista[lista.length - 1]?.etiqueta ?? null);
        }
      } catch (err) {
        console.error(err);
        setError("Error al cargar la página.");
      } finally {
        setChecking(false);
      }
    }
    init();
  }, [router]);

  const cargarEstadisticas = useCallback(async () => {
    if (!semSeleccionado) return;
    const sem = semestres.find((s) => s.etiqueta === semSeleccionado);
    if (!sem) return;
    setCargando(true); setError("");
    try {
      const res = await fetch(
        `${API_URL_BASE}/estadisticas/${sem.año}/semestre/${sem.semestre}`,
        { credentials: "include" }
      );
      if (res.status === 403) { setError("Sin permiso."); setStats(null); return; }
      if (!res.ok) { setError("Error al cargar estadísticas."); setStats(null); return; }
      setStats(await res.json());
    } catch {
      setError("Error de conexión.");
    } finally {
      setCargando(false);
    }
  }, [semSeleccionado, semestres]);

  useEffect(() => { cargarEstadisticas(); }, [cargarEstadisticas]);

  async function descargarPDF() {
    const sem = semestres.find((s) => s.etiqueta === semSeleccionado);
    if (!sem) return;
    setDescargando(true);
    try {
      const res = await fetch(
        `${API_URL_BASE}/estadisticas/${sem.año}/semestre/${sem.semestre}/pdf`,
        { credentials: "include" }
      );
      if (!res.ok) { alert("El PDF aún no está disponible en el backend."); return; }
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url; a.download = `estadisticas-${semSeleccionado}.pdf`; a.click();
      URL.revokeObjectURL(url);
    } catch {
      alert("Error al descargar el PDF.");
    } finally {
      setDescargando(false);
    }
  }

  if (checking)
    return <div className="p-8 text-muted-foreground animate-pulse text-sm">Cargando...</div>;

  const semActual = semestres.find((s) => s.etiqueta === semSeleccionado);

  return (
    <div className="p-4 lg:p-6 flex flex-col gap-4 bg-background">

      {/* Encabezado compacto */}
      <div className="flex flex-wrap justify-between items-center gap-3">
        <div>
          <div className="flex items-center gap-2">
            <FileBarChart2 className="w-4 h-4 text-primary" />
            <h1 className="text-lg font-extrabold text-foreground leading-none">Estadísticas</h1>
          </div>
          {semActual && (
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {semActual.periodoInicio} → {semActual.periodoFin}
            </p>
          )}
        </div>

        <div className="flex items-center gap-2">
          <Select
            value={semSeleccionado || ""}
            onValueChange={setSemSeleccionado}
            disabled={cargando || semestres.length === 0}
          >
            <SelectTrigger className="w-32 h-8 text-xs">
              <SelectValue placeholder="Semestre" />
            </SelectTrigger>
            <SelectContent>
              {semestres.map((s) => (
                <SelectItem key={s.etiqueta} value={s.etiqueta} className="text-xs">{s.etiqueta}</SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Button variant="outline" size="icon" className="h-8 w-8"
            onClick={cargarEstadisticas} disabled={cargando} title="Actualizar">
            <RefreshCw className={`w-3.5 h-3.5 ${cargando ? "animate-spin" : ""}`} />
          </Button>

          <Button onClick={descargarPDF} disabled={descargando || !stats}
            className="h-8 gap-1.5 text-xs px-3">
            <Download className="w-3.5 h-3.5" />
            {descargando ? "Descargando…" : "Exportar PDF"}
          </Button>
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="rounded-lg border border-primary/30 bg-primary/10 px-3 py-2 text-primary text-xs">
          {error}
        </div>
      )}

      {/* Skeleton */}
      {cargando && (
        <>
          <div className="grid grid-cols-4 gap-3">
            {[1,2,3,4].map((i) => <Skeleton key={i} className="h-16" />)}
          </div>
          <div className="grid grid-cols-3 gap-3">
            <Skeleton className="h-48 col-span-2" />
            <Skeleton className="h-48" />
          </div>
          <Skeleton className="h-32" />
        </>
      )}

      {/* Datos — todo visible en pantalla */}
      {!cargando && stats && (
        <>
          {/* Fila 1: 4 tarjetas */}
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-3">
            {METRIC_CARDS.map(({ key, label, cls }) => (
              <MetricCard key={key} label={label} value={stats[key]} cls={cls} />
            ))}
          </div>

          {/* Fila 2: bar chart + donut lado a lado */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-3">
            <Panel title="Consultas por área"
              badge={`Total ${stats.totalConsultas}`}
              className="lg:col-span-2">
              <BarChartAreas areas={stats.consultasPorArea} />
            </Panel>

            <Panel title="Estado de consultas"
              badge={`${Math.round((stats.consultasFinalizadas / (stats.totalConsultas || 1)) * 100)}% completado`}>
              <DonutChart
                finalizadas={stats.consultasFinalizadas}
                pendientes={stats.consultasPendientes}
              />
            </Panel>
          </div>

          {/* Fila 3: procesos por estado — horizontal */}
          <Panel title="Procesos por estado">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-8 gap-y-2">
              {stats.procesosPorEstado && stats.procesosPorEstado.length > 0
                ? (() => {
                    const maxVal = Math.max(...stats.procesosPorEstado.map((p) => p.cantidad), 1);
                    return stats.procesosPorEstado.map((p, i) => {
                      const pct = Math.round((p.cantidad / maxVal) * 100);
                      const color = CHART_COLORS[i % CHART_COLORS.length];
                      return (
                        <div key={p.nombre}>
                          <div className="flex justify-between text-xs mb-1">
                            <span className="font-medium text-foreground">{etiquetaEstado(p.nombre)}</span>
                            <span className="font-bold" style={{ color }}>{p.cantidad}</span>
                          </div>
                          <div className="w-full h-1.5 rounded-full bg-accent">
                            <div className="h-1.5 rounded-full transition-all duration-500"
                              style={{ width: `${pct}%`, backgroundColor: color }} />
                          </div>
                        </div>
                      );
                    });
                  })()
                : <p className="text-xs text-muted-foreground col-span-3">Sin procesos registrados.</p>
              }
            </div>
          </Panel>
        </>
      )}

      {/* Sin datos */}
      {!cargando && !stats && !error && (
        <div className="text-center py-12 text-muted-foreground">
          <FileBarChart2 className="w-10 h-10 mx-auto mb-2 opacity-25" />
          <p className="text-xs">Selecciona un semestre para ver las estadísticas.</p>
        </div>
      )}
    </div>
  );
}
