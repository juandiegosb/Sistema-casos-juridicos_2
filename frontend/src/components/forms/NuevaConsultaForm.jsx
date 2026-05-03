"use client";

import React, { useEffect, useState, useRef } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { X, ChevronDown, Search } from "lucide-react";
import { API_URL_BASE, FILE_STORAGE_API_URL_BASE } from "@/lib/config";
import ArchivosConsultaForm from "./parts/ArchivosConsultaForm";

const ESTADOS = ["Activo", "En proceso", "Pendiente", "Urgente", "Cerrado", "Archivado"];

const VACIOS = {
  fecha: "", descripcion: "", hechos: "", pretensiones: "",
  conceptoJuridico: "", tramite: "", observaciones: "",
  tipoViolencia: "", estado: "", resultado: "",
  personaId: "", sedeId: "", areaId: "", temaId: "",
  tipoId: "", asesorId: "", monitorId: "", estudianteId: "",
  partesIds: [], contrapartesIds: [],
};

/* ─────────────────────────────────────────────────────
   Combobox de búsqueda — selección de UNA persona
   ───────────────────────────────────────────────────── */
function PersonaCombobox({ personas, value, onChange, placeholder = "Buscar persona...", disabled = false }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const wrapRef = useRef(null);

  const selected = personas.find(p => String(p.id) === String(value));

  const filtered = personas.filter(p => {
    if (!query) return true;
    const q = query.toLowerCase();
    return (
      (p.nombres || "").toLowerCase().includes(q) ||
      (p.apellidos || "").toLowerCase().includes(q) ||
      (p.numeroDocumento || "").toLowerCase().includes(q)
    );
  }).slice(0, 50);

  useEffect(() => {
    function handleClickOutside(e) {
      if (wrapRef.current && !wrapRef.current.contains(e.target)) {
        setOpen(false);
        setQuery("");
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  function select(p) {
    onChange(String(p.id));
    setOpen(false);
    setQuery("");
  }

  function clear() {
    onChange("");
    setOpen(false);
    setQuery("");
  }

  return (
    <div ref={wrapRef} className="relative">
      <button
        type="button"
        disabled={disabled}
        onClick={() => setOpen(o => !o)}
        className={`${ic} flex items-center justify-between gap-2 text-left`}
      >
        <span className={selected ? "" : "text-muted-foreground"}>
          {selected
            ? `${selected.nombres} ${selected.apellidos} — ${selected.numeroDocumento}`
            : placeholder}
        </span>
        <span className="flex items-center gap-1 shrink-0">
          {selected && (
            <span
              role="button"
              tabIndex={0}
              onClick={e => { e.stopPropagation(); clear(); }}
              onKeyDown={e => e.key === "Enter" && clear()}
              className="text-muted-foreground hover:text-foreground"
            >
              <X className="w-3 h-3" />
            </span>
          )}
          <ChevronDown className="w-4 h-4 text-muted-foreground" />
        </span>
      </button>

      {open && (
        <div className="absolute z-50 mt-1 w-full rounded-md border bg-background shadow-lg">
          <div className="flex items-center gap-2 px-3 py-2 border-b">
            <Search className="w-4 h-4 text-muted-foreground shrink-0" />
            <input
              autoFocus
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Nombre, apellido o cédula..."
              className="flex-1 bg-transparent text-sm outline-none"
            />
          </div>
          <div className="max-h-48 overflow-y-auto">
            {filtered.length === 0 ? (
              <p className="py-4 text-center text-sm text-muted-foreground">Sin resultados</p>
            ) : filtered.map(p => (
              <button
                key={p.id}
                type="button"
                onClick={() => select(p)}
                className={`w-full text-left px-3 py-2 text-sm hover:bg-muted transition-colors ${String(p.id) === String(value) ? "bg-primary/10 font-medium" : ""}`}
              >
                {p.nombres} {p.apellidos} — {p.numeroDocumento}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────────────
   Combobox multi-selección — partes / contrapartes
   ───────────────────────────────────────────────────── */
function PersonaMultiCombobox({ personas, selectedIds, onChange, placeholder = "Agregar persona...", disabled = false }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const wrapRef = useRef(null);

  const filtered = personas.filter(p => {
    if (!query) return true;
    const q = query.toLowerCase();
    return (
      (p.nombres || "").toLowerCase().includes(q) ||
      (p.apellidos || "").toLowerCase().includes(q) ||
      (p.numeroDocumento || "").toLowerCase().includes(q)
    );
  }).slice(0, 50);

  useEffect(() => {
    function handleClickOutside(e) {
      if (wrapRef.current && !wrapRef.current.contains(e.target)) {
        setOpen(false);
        setQuery("");
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  function toggle(p) {
    const numId = Number(p.id);
    const existe = selectedIds.includes(numId);
    onChange(existe ? selectedIds.filter(x => x !== numId) : [...selectedIds, numId]);
  }

  function remove(id) {
    onChange(selectedIds.filter(x => x !== id));
  }

  const selectedPersonas = personas.filter(p => selectedIds.includes(Number(p.id)));

  return (
    <div ref={wrapRef} className="relative space-y-2">
      {selectedPersonas.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {selectedPersonas.map(p => (
            <span key={p.id} className="inline-flex items-center gap-1 rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary border border-primary/20">
              {p.nombres} {p.apellidos}
              <button type="button" onClick={() => remove(Number(p.id))} className="hover:text-destructive ml-1">
                <X className="w-3 h-3" />
              </button>
            </span>
          ))}
        </div>
      )}

      <button
        type="button"
        disabled={disabled}
        onClick={() => setOpen(o => !o)}
        className={`${ic} flex items-center justify-between gap-2 text-left`}
      >
        <span className="text-muted-foreground text-sm">{placeholder}</span>
        <ChevronDown className="w-4 h-4 text-muted-foreground shrink-0" />
      </button>

      {open && (
        <div className="absolute z-50 mt-1 w-full rounded-md border bg-background shadow-lg">
          <div className="flex items-center gap-2 px-3 py-2 border-b">
            <Search className="w-4 h-4 text-muted-foreground shrink-0" />
            <input
              autoFocus
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Nombre, apellido o cédula..."
              className="flex-1 bg-transparent text-sm outline-none"
            />
          </div>
          <div className="max-h-48 overflow-y-auto">
            {filtered.length === 0 ? (
              <p className="py-4 text-center text-sm text-muted-foreground">Sin resultados</p>
            ) : filtered.map(p => {
              const isSelected = selectedIds.includes(Number(p.id));
              return (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => toggle(p)}
                  className={`w-full text-left px-3 py-2 text-sm hover:bg-muted transition-colors flex items-center gap-2 ${isSelected ? "bg-primary/10" : ""}`}
                >
                  <span className={`w-4 h-4 rounded border flex items-center justify-center shrink-0 ${isSelected ? "bg-primary border-primary" : "border-muted-foreground"}`}>
                    {isSelected && <span className="text-primary-foreground text-xs font-bold">✓</span>}
                  </span>
                  {p.nombres} {p.apellidos} — {p.numeroDocumento}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────────────
   Formulario Nueva Consulta
   ───────────────────────────────────────────────────── */
export function NuevaConsultaForm() {
  const router = useRouter();

  const [form, setForm] = useState(VACIOS);
  const [archivos, setArchivos] = useState([]);
  const [guardando, setGuardando] = useState(false);
  const [checking, setChecking] = useState(true);

  const [todasPersonas, setTodasPersonas] = useState([]);
  const [sedes, setSedes] = useState([]);
  const [areas, setAreas] = useState([]);
  const [temas, setTemas] = useState([]);
  const [tipos, setTipos] = useState([]);
  const [asesores, setAsesores] = useState([]);
  const [monitores, setMonitores] = useState([]);
  const [estudiantes, setEstudiantes] = useState([]);

  useEffect(() => {
    const verificar = async () => {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, { credentials: "include" });
        if (res.status === 401) { router.push("/"); return; }
        const user = await res.json();
        if (!user?.permisos?.includes("Gestionar consultas")) {
          toast.error("No tienes permisos");
          router.push("/inicio");
          return;
        }
        await cargarCatalogos();
      } catch {
        router.push("/");
      } finally {
        setChecking(false);
      }
    };
    verificar();
  }, [router]);

  const prevAreaId = useRef(null);
  useEffect(() => {
    if (prevAreaId.current === form.areaId) return;
    prevAreaId.current = form.areaId;
    if (form.areaId) {
      fetch(`${API_URL_BASE}/temas/area/${form.areaId}`, { credentials: "include" })
        .then(r => r.json())
        .then(d => setTemas(Array.isArray(d) ? d : []))
        .catch(() => setTemas([]));
      setForm(prev => ({ ...prev, temaId: "", tipoId: "" }));
    } else {
      setTemas([]);
      setTipos([]);
    }
  }, [form.areaId]);

  const prevTemaId = useRef(null);
  useEffect(() => {
    if (prevTemaId.current === form.temaId) return;
    prevTemaId.current = form.temaId;
    if (form.temaId) {
      fetch(`${API_URL_BASE}/tipos/tema/${form.temaId}`, { credentials: "include" })
        .then(r => r.json())
        .then(d => setTipos(Array.isArray(d) ? d : []))
        .catch(() => setTipos([]));
      setForm(prev => ({ ...prev, tipoId: "" }));
    } else {
      setTipos([]);
    }
  }, [form.temaId]);

  async function cargarCatalogos() {
    try {
      const [pR, sR, aR, asR, moR, esR] = await Promise.all([
        fetch(`${API_URL_BASE}/personas`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/sedes`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/areas`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/asesores/activos`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/monitores/activos`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/estudiantes/activos`, { credentials: "include" }).then(r => r.json()),
      ]);
      setTodasPersonas(Array.isArray(pR) ? pR : []);
      setSedes(Array.isArray(sR) ? sR : []);
      setAreas(Array.isArray(aR) ? aR : []);
      setAsesores(Array.isArray(asR) ? asR : []);
      setMonitores(Array.isArray(moR) ? moR : []);
      setEstudiantes(Array.isArray(esR) ? esR : []);
    } catch (e) {
      console.error("Error catálogos:", e);
      toast.error("Error cargando datos");
    }
  }

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  function handlePersonaId(id) {
    setForm(prev => ({
      ...prev,
      personaId: id,
      partesIds: prev.partesIds.filter(x => x !== Number(id)),
      contrapartesIds: prev.contrapartesIds.filter(x => x !== Number(id)),
    }));
  }

  const personasParaPartes = todasPersonas.filter(p =>
    String(p.id) !== String(form.personaId) &&
    !form.contrapartesIds.includes(Number(p.id))
  );

  const personasParaContrapartes = todasPersonas.filter(p =>
    String(p.id) !== String(form.personaId) &&
    !form.partesIds.includes(Number(p.id))
  );

  async function handleGuardar(e) {
    e.preventDefault();
    setGuardando(true);
    let consultaOk = false;
    let uploadOk = true;

    const payload = {
      ...form,
      personaId: Number(form.personaId),
      sedeId: Number(form.sedeId),
      areaId: Number(form.areaId),
      temaId: Number(form.temaId),
      tipoId: form.tipoId ? Number(form.tipoId) : null,
      asesorId: form.asesorId ? Number(form.asesorId) : null,
      monitorId: form.monitorId ? Number(form.monitorId) : null,
      estudianteId: form.estudianteId ? Number(form.estudianteId) : null,
    };

    try {
      const res = await fetch(`${API_URL_BASE}/consultas`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (res.status === 401) { router.push("/"); return; }
      if (res.status === 403) { toast.error("No tienes permisos"); return; }
      if (res.ok) {
        const responseBody = await res.json();
        const consultaId = responseBody?.id;

        if (!consultaId) {
          throw new Error("No se recibió el id de la consulta");
        }

        consultaOk = true;
        toast.success("Consulta creada");
        if (archivos && archivos.length > 0) {
          console.log("Subiendo archivos:", archivos);
          const formData = new FormData();
          archivos.forEach(file => {
            formData.append('files', file);
          });
          formData.append('path', String(consultaId));
          try {
            const uploadRes = await fetch(`${FILE_STORAGE_API_URL_BASE}/files/upload-multiple`, {
              method: 'POST',
              credentials: "include",
              body: formData,
            });
            console.log("Upload response status:", uploadRes.status);
            if (uploadRes.ok) {
              toast.success("Archivos subidos correctamente");
            } else {
              uploadOk = false;
              const errorText = await uploadRes.text();
              console.log("Upload error:", errorText);
              toast.error("Error al subir archivos");
            }
          } catch (uploadError) {
            uploadOk = false;
            console.log("Error en upload fetch:", uploadError);
            toast.error("Error al subir archivos");
          }
        }
      } else {
        const error = await res.json();
        toast.error(error.mensaje || "Error al crear");
      }
    } catch (error) {
      console.log("Error en crear consulta:", error);
      toast.error("Error de conexión");
    }

    if (consultaOk && uploadOk) {
      router.push("/consultasjuridicas");
    }
    setGuardando(false);
  }

  if (checking) return <p className="p-6">Verificando permisos...</p>;

  return (
    <form onSubmit={handleGuardar} className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <C label="Fecha *"><input type="date" name="fecha" value={form.fecha} onChange={handleChange} required className={ic} /></C>
        <C label="Estado *">
          <select name="estado" value={form.estado} onChange={handleChange} required className={ic}>
            <option value="">Seleccione</option>
            {ESTADOS.map(e => <option key={e} value={e}>{e}</option>)}
          </select>
        </C>
        <C label="Trámite *"><input name="tramite" value={form.tramite} onChange={handleChange} required placeholder="Ej: Conciliación" className={ic} /></C>

        <C label="Sede *">
          <select name="sedeId" value={form.sedeId} onChange={handleChange} required className={ic}>
            <option value="">Seleccione</option>
            {sedes.map(s => <option key={s.id} value={s.id}>{s.nombre}</option>)}
          </select>
        </C>
        <C label="Área *">
          <select name="areaId" value={form.areaId} onChange={handleChange} required className={ic}>
            <option value="">Seleccione</option>
            {areas.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
          </select>
        </C>
        <C label="Tema *">
          <select name="temaId" value={form.temaId} onChange={handleChange} required className={ic} disabled={!form.areaId}>
            <option value="">{form.areaId ? "Seleccione" : "Seleccione área primero"}</option>
            {temas.map(t => <option key={t.id} value={t.id}>{t.nombre}</option>)}
          </select>
        </C>
        <C label="Tipo">
          <select name="tipoId" value={form.tipoId} onChange={handleChange} className={ic} disabled={!form.temaId}>
            <option value="">{form.temaId ? "Sin tipo" : "Seleccione tema primero"}</option>
            {tipos.map(t => <option key={t.id} value={t.id}>{t.nombre}</option>)}
          </select>
        </C>
        <C label="Tipo de violencia"><input name="tipoViolencia" value={form.tipoViolencia} onChange={handleChange} placeholder="Opcional" className={ic} /></C>
        <C label="Resultado"><input name="resultado" value={form.resultado} onChange={handleChange} placeholder="Opcional" className={ic} /></C>

        <C label="Asesor">
          <select name="asesorId" value={form.asesorId} onChange={handleChange} className={ic}>
            <option value="">Sin asignar</option>
            {asesores.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
          </select>
        </C>
        <C label="Monitor">
          <select name="monitorId" value={form.monitorId} onChange={handleChange} className={ic}>
            <option value="">Sin asignar</option>
            {monitores.map(m => <option key={m.id} value={m.id}>{m.nombre}</option>)}
          </select>
        </C>
        <C label="Estudiante">
          <select name="estudianteId" value={form.estudianteId} onChange={handleChange} className={ic}>
            <option value="">Sin asignar</option>
            {estudiantes.map(e => <option key={e.id} value={e.id}>{e.nombre}</option>)}
          </select>
        </C>
      </div>

      <C label="Parte principal *">
        <PersonaCombobox
          personas={todasPersonas}
          value={form.personaId}
          onChange={handlePersonaId}
          placeholder="Buscar por nombre o cédula..."
        />
      </C>

      <C label="Partes adicionales">
        <PersonaMultiCombobox
          personas={personasParaPartes}
          selectedIds={form.partesIds}
          onChange={ids => setForm(prev => ({ ...prev, partesIds: ids }))}
          placeholder="Buscar y agregar parte adicional..."
          disabled={!form.personaId}
        />
        {!form.personaId && <p className="text-xs text-muted-foreground">Seleccione la parte principal primero</p>}
      </C>

      <C label="Contrapartes">
        <PersonaMultiCombobox
          personas={personasParaContrapartes}
          selectedIds={form.contrapartesIds}
          onChange={ids => setForm(prev => ({ ...prev, contrapartesIds: ids }))}
          placeholder="Buscar y agregar contraparte..."
          disabled={!form.personaId}
        />
        {!form.personaId && <p className="text-xs text-muted-foreground">Seleccione la parte principal primero</p>}
      </C>

      <C label="Descripción *"><textarea name="descripcion" value={form.descripcion} onChange={handleChange} required rows={3} placeholder="Resumen de la consulta" className={ic} /></C>
      <C label="Hechos *"><textarea name="hechos" value={form.hechos} onChange={handleChange} required rows={3} placeholder="Descripción de los hechos" className={ic} /></C>
      <C label="Pretensiones *"><textarea name="pretensiones" value={form.pretensiones} onChange={handleChange} required rows={3} placeholder="Qué solicita el consultante" className={ic} /></C>
      <C label="Concepto jurídico *"><textarea name="conceptoJuridico" value={form.conceptoJuridico} onChange={handleChange} required rows={3} placeholder="Fundamento legal aplicable" className={ic} /></C>
      <C label="Observaciones"><textarea name="observaciones" value={form.observaciones} onChange={handleChange} rows={2} placeholder="Opcional" className={ic} /></C>

      <ArchivosConsultaForm archivos={archivos} onChange={setArchivos} />

      <div className="flex justify-end gap-3 pt-2">
        <Button type="button" variant="outline" onClick={() => router.push("/consultasjuridicas")} disabled={guardando}>Cancelar</Button>
        <Button type="submit" disabled={guardando}>{guardando ? "Guardando..." : "Crear consulta"}</Button>
      </div>
    </form>
  );
}

function C({ label, children }) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-sm font-medium">{label}</label>
      {children}
    </div>
  );
}

const ic = "w-full rounded-md border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring disabled:opacity-50";
