"use client";

import React, { useEffect, useState, useMemo } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";

import { API_URL_BASE, FILE_STORAGE_API_URL_BASE } from "@/lib/config";

const ESTADOS = ["Activo", "En proceso", "Pendiente", "Urgente", "Cerrado", "Archivado"];

const VACIOS = {
  fecha: "", descripcion: "", hechos: "", pretensiones: "",
  conceptoJuridico: "", tramite: "", observaciones: "",
  tipoViolencia: "", estado: "", resultado: "",
  personaId: "", sedeId: "", areaId: "", temaId: "",
  tipoId: "", asesorId: "", monitorId: "", estudianteId: "",
  partesIds: [], contrapartesIds: [],
};

// Modal selección simple
function ModalSimple({ abierto, titulo, items, busqueda, setBusqueda, onSeleccionar, onCerrar, seleccionado, renderItem }) {
  if (!abierto) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-background rounded-xl border shadow-lg w-full max-w-md mx-4 p-6 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">{titulo}</h3>
          <button type="button" onClick={onCerrar} className="text-muted-foreground hover:text-foreground text-xl">✕</button>
        </div>
        <input autoFocus type="text" placeholder="Buscar..."
          value={busqueda} onChange={e => setBusqueda(e.target.value)}
          className="w-full rounded-lg border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
        />
        <div className="max-h-72 overflow-y-auto space-y-1">
          <button type="button" onClick={() => onSeleccionar(null)}
            className={`w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-muted transition-colors ${!seleccionado ? "bg-primary/10 text-primary font-medium" : ""}`}>
            Sin asignar
          </button>
          {items.length === 0 ? (
            <p className="text-center text-sm text-muted-foreground py-4">Sin resultados</p>
          ) : items.map(item => (
            <button key={item.id} type="button" onClick={() => onSeleccionar(item)}
              className={`w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-muted transition-colors ${seleccionado?.id === item.id ? "bg-primary/10 text-primary font-medium" : ""}`}>
              {renderItem(item)}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

// Modal selección múltiple con checkboxes
function ModalMultiple({ abierto, titulo, items, busqueda, setBusqueda, onConfirmar, onCerrar, seleccionados, renderItem }) {
  const [temp, setTemp] = useState([]);

  useEffect(() => {
    if (abierto) setTemp(seleccionados || []);
  }, [abierto, seleccionados]);

  if (!abierto) return null;

  function toggleItem(id) {
    const numId = Number(id);
    setTemp(prev => prev.includes(numId) ? prev.filter(x => x !== numId) : [...prev, numId]);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-background rounded-xl border shadow-lg w-full max-w-md mx-4 p-6 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">{titulo}</h3>
          <button type="button" onClick={onCerrar} className="text-muted-foreground hover:text-foreground text-xl">✕</button>
        </div>
        <input autoFocus type="text" placeholder="Buscar..."
          value={busqueda} onChange={e => setBusqueda(e.target.value)}
          className="w-full rounded-lg border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
        />
        <div className="max-h-64 overflow-y-auto space-y-1">
          {items.length === 0 ? (
            <p className="text-center text-sm text-muted-foreground py-4">Sin resultados</p>
          ) : items.map(item => {
            const numId = Number(item.id);
            const marcado = temp.includes(numId);
            return (
              <button key={item.id} type="button" onClick={() => toggleItem(item.id)}
                className={`w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-muted transition-colors flex items-center gap-3 ${marcado ? "bg-primary/10" : ""}`}>
                <span className={`w-4 h-4 rounded border flex items-center justify-center flex-shrink-0 ${marcado ? "bg-primary border-primary" : "border-gray-400"}`}>
                  {marcado && <span className="text-white text-xs">✓</span>}
                </span>
                <span>{renderItem(item)}</span>
              </button>
            );
          })}
        </div>
        <div className="flex justify-between items-center pt-2">
          <span className="text-xs text-muted-foreground">{temp.length} seleccionado(s)</span>
          <div className="flex gap-2">
            <Button type="button" variant="outline" size="sm" onClick={() => setTemp([])}>Limpiar</Button>
            <Button type="button" size="sm" onClick={() => onConfirmar(temp)}>Confirmar</Button>
          </div>
        </div>
      </div>
    </div>
  );
}

export function ConsultasJuridicasForm() {
  const router = useRouter();

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [esAdministrativo, setEsAdministrativo] = useState(false);

  const [mostrarFormEdicion, setMostrarFormEdicion] = useState(false);
  const [idEditando, setIdEditando] = useState(null);
  const [form, setForm] = useState(VACIOS);
  const [guardando, setGuardando] = useState(false);

  const [personas, setPersonas] = useState([]);
  const [sedes, setSedes] = useState([]);
  const [areas, setAreas] = useState([]);
  const [temas, setTemas] = useState([]);
  const [tipos, setTipos] = useState([]);
  const [asesores, setAsesores] = useState([]);
  const [monitores, setMonitores] = useState([]);
  const [estudiantes, setEstudiantes] = useState([]);
  const [archivosCaso, setArchivosCaso] = useState([]);
  const [cargandoArchivos, setCargandoArchivos] = useState(false);

  // Estados modales edición
  const [modalAsesor, setModalAsesor] = useState({ abierto: false, busqueda: "" });
  const [modalMonitor, setModalMonitor] = useState({ abierto: false, busqueda: "" });
  const [modalEstudiante, setModalEstudiante] = useState({ abierto: false, busqueda: "" });
  const [modalParte, setModalParte] = useState({ abierto: false, busqueda: "" });
  const [modalPartesAdicionales, setModalPartesAdicionales] = useState({ abierto: false, busqueda: "" });
  const [modalContrapartes, setModalContrapartes] = useState({ abierto: false, busqueda: "" });

  // Seleccionados
  const asesorSeleccionado = useMemo(() => asesores.find(a => String(a.id) === String(form.asesorId)) || null, [asesores, form.asesorId]);
  const monitorSeleccionado = useMemo(() => monitores.find(m => String(m.id) === String(form.monitorId)) || null, [monitores, form.monitorId]);
  const estudianteSeleccionado = useMemo(() => estudiantes.find(e => String(e.id) === String(form.estudianteId)) || null, [estudiantes, form.estudianteId]);
  const parteSeleccionada = useMemo(() => personas.find(p => Number(p.id) === Number(form.personaId)) || null, [personas, form.personaId]);
  const partesAdicionalesSeleccionadas = useMemo(() => personas.filter(p => (form.partesIds || []).includes(Number(p.id))), [personas, form.partesIds]);
  const contrapartesSeleccionadas = useMemo(() => personas.filter(p => (form.contrapartesIds || []).includes(Number(p.id))), [personas, form.contrapartesIds]);

  // Filtros con regla de duplicados
  const personasParaPrincipal = useMemo(() => personas.filter(p => {
    const id = Number(p.id);
    return !(form.partesIds || []).includes(id) && !(form.contrapartesIds || []).includes(id);
  }), [personas, form.partesIds, form.contrapartesIds]);

  const personasParaAdicionales = useMemo(() => personas.filter(p => {
    const id = Number(p.id);
    return id !== Number(form.personaId) && !(form.contrapartesIds || []).includes(id);
  }), [personas, form.personaId, form.contrapartesIds]);

  const personasParaContrapartes = useMemo(() => personas.filter(p => {
    const id = Number(p.id);
    return id !== Number(form.personaId) && !(form.partesIds || []).includes(id);
  }), [personas, form.personaId, form.partesIds]);

  // Filtros por búsqueda
  const asesoresFiltrados = useMemo(() => {
    const t = modalAsesor.busqueda.toLowerCase();
    return t ? asesores.filter(a => `${a.nombre} ${a.documento}`.toLowerCase().includes(t)) : asesores;
  }, [asesores, modalAsesor.busqueda]);

  const monitoresFiltrados = useMemo(() => {
    const t = modalMonitor.busqueda.toLowerCase();
    return t ? monitores.filter(m => `${m.nombre} ${m.documento}`.toLowerCase().includes(t)) : monitores;
  }, [monitores, modalMonitor.busqueda]);

  const estudiantesFiltrados = useMemo(() => {
    const t = modalEstudiante.busqueda.toLowerCase();
    return t ? estudiantes.filter(e => `${e.nombre} ${e.documento} ${e.codigo}`.toLowerCase().includes(t)) : estudiantes;
  }, [estudiantes, modalEstudiante.busqueda]);

  const parteFiltrada = useMemo(() => {
    const t = modalParte.busqueda.toLowerCase();
    return t ? personasParaPrincipal.filter(p => `${p.nombres} ${p.apellidos} ${p.numeroDocumento}`.toLowerCase().includes(t)) : personasParaPrincipal;
  }, [personasParaPrincipal, modalParte.busqueda]);

  const partesAdicionalesFiltradas = useMemo(() => {
    const t = modalPartesAdicionales.busqueda.toLowerCase();
    return t ? personasParaAdicionales.filter(p => `${p.nombres} ${p.apellidos} ${p.numeroDocumento}`.toLowerCase().includes(t)) : personasParaAdicionales;
  }, [personasParaAdicionales, modalPartesAdicionales.busqueda]);

  const contrapartesFiltradas = useMemo(() => {
    const t = modalContrapartes.busqueda.toLowerCase();
    return t ? personasParaContrapartes.filter(p => `${p.nombres} ${p.apellidos} ${p.numeroDocumento}`.toLowerCase().includes(t)) : personasParaContrapartes;
  }, [personasParaContrapartes, modalContrapartes.busqueda]);

  useEffect(() => {
    async function init() {
      try {
        const res = await fetch(`${API_URL_BASE}/auth/me`, { credentials: "include" });
        if (res.status === 401) { router.push("/"); return; }
        const user = await res.json();
        const tipoPerfil = user.tipoPerfil?.toUpperCase();
        setEsAdministrativo(tipoPerfil === "ADMINISTRATIVO" || tipoPerfil === null || tipoPerfil === undefined);
      } catch {}
      cargarConsultas();
      cargarCatalogos();
    }
    init();
  }, []);

  const prevAreaId = React.useRef(null);
  useEffect(() => {
    if (prevAreaId.current === form.areaId) return;
    prevAreaId.current = form.areaId;
    if (form.areaId) {
      fetch(`${API_URL_BASE}/temas/area/${form.areaId}`, { credentials: "include" })
        .then(r => r.json()).then(d => setTemas(Array.isArray(d) ? d : [])).catch(() => setTemas([]));
    } else { setTemas([]); setTipos([]); }
  }, [form.areaId]);

  const prevTemaId = React.useRef(null);
  useEffect(() => {
    if (prevTemaId.current === form.temaId) return;
    prevTemaId.current = form.temaId;
    if (form.temaId) {
      fetch(`${API_URL_BASE}/tipos/tema/${form.temaId}`, { credentials: "include" })
        .then(r => r.json()).then(d => setTipos(Array.isArray(d) ? d : [])).catch(() => setTipos([]));
    } else { setTipos([]); }
  }, [form.temaId]);

  async function cargarConsultas(search = "") {
    setLoading(true);
    try {
      const res = await fetch(`${API_URL_BASE}/consultas?search=${encodeURIComponent(search)}`, { credentials: "include" });
      if (res.status === 401) { router.push("/"); return; }
      if (res.status === 403) { router.push("/inicio"); return; }
      const data = await res.json();
      setRows(Array.isArray(data) ? data : []);
    } catch { toast.error("Error de conexión"); }
    finally { setLoading(false); }
  }

  async function cargarCatalogos() {
    try {
      const [pR, sR, aR, asR, moR, esR] = await Promise.all([
        fetch(`${API_URL_BASE}/personas/activos`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/sedes`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/areas`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/asesores/activos`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/monitores/activos`, { credentials: "include" }).then(r => r.json()),
        fetch(`${API_URL_BASE}/estudiantes/activos`, { credentials: "include" }).then(r => r.json()),
      ]);
      setPersonas(Array.isArray(pR) ? pR : []);
      setSedes(Array.isArray(sR) ? sR : []);
      setAreas(Array.isArray(aR) ? aR : []);
      setAsesores(Array.isArray(asR) ? asR : []);
      setMonitores(Array.isArray(moR) ? moR : []);
      setEstudiantes(Array.isArray(esR) ? esR : []);
    } catch (e) { console.error("Error catálogos:", e); }
  }

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  async function abrirEditar(id) {
    try {
      const res = await fetch(`${API_URL_BASE}/consultas/${id}`, { credentials: "include" });
      if (res.status === 401) { router.push("/"); return; }
      const data = await res.json();

      if (data.areaId) {
        const temasRes = await fetch(`${API_URL_BASE}/temas/area/${data.areaId}`, { credentials: "include" });
        const temasData = await temasRes.json();
        setTemas(Array.isArray(temasData) ? temasData : []);
      }
      if (data.temaId) {
        const tiposRes = await fetch(`${API_URL_BASE}/tipos/tema/${data.temaId}`, { credentials: "include" });
        const tiposData = await tiposRes.json();
        setTipos(Array.isArray(tiposData) ? tiposData : []);
      }

      setForm({
        fecha: data.fecha ?? "",
        descripcion: data.descripcion ?? "",
        hechos: data.hechos ?? "",
        pretensiones: data.pretensiones ?? "",
        conceptoJuridico: data.conceptoJuridico ?? "",
        tramite: data.tramite ?? "",
        observaciones: data.observaciones ?? "",
        tipoViolencia: data.tipoViolencia ?? "",
        estado: data.estado ?? "",
        resultado: data.resultado ?? "",
        personaId: data.personaId ?? "",
        sedeId: data.sedeId ?? "",
        areaId: data.areaId ?? "",
        temaId: data.temaId ?? "",
        tipoId: data.tipoId ?? "",
        asesorId: data.asesorId ?? "",
        monitorId: data.monitorId ?? "",
        estudianteId: data.estudianteId ?? "",
        partesIds: Array.isArray(data.partesIds) ? data.partesIds.map(Number) : [],
        contrapartesIds: Array.isArray(data.contrapartesIds) ? data.contrapartesIds.map(Number) : [],
      });
      setIdEditando(id);
      setMostrarFormEdicion(true);
      cargarArchivosCaso(id);
    } catch { toast.error("Error al cargar la consulta"); }
  }

  async function cargarArchivosCaso(consultaId) {
    setCargandoArchivos(true);
    try {
      const res = await fetch(`${FILE_STORAGE_API_URL_BASE}/files/list/${consultaId}`, { credentials: "include" });
      if (!res.ok) { setArchivosCaso([]); return; }
      const data = await res.json();
      setArchivosCaso(Array.isArray(data) ? data : []);
    } catch { setArchivosCaso([]); }
    finally { setCargandoArchivos(false); }
  }

  const descargarArchivo = async (fileName) => {
    try {
      const response = await fetch(`${FILE_STORAGE_API_URL_BASE}/files/download/${idEditando}/${encodeURIComponent(fileName)}`, { method: 'GET', credentials: 'include' });
      if (!response.ok) throw new Error('Error descargando archivo');
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = fileName;
      document.body.appendChild(a); a.click(); a.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) { console.error(error); }
  };

  async function handleGuardar(e) {
    e.preventDefault();
    setGuardando(true);
    const payload = {
      ...form,
      personaId: form.personaId ? Number(form.personaId) : null,
      sedeId: form.sedeId ? Number(form.sedeId) : null,
      areaId: form.areaId ? Number(form.areaId) : null,
      temaId: form.temaId ? Number(form.temaId) : null,
      tipoId: form.tipoId ? Number(form.tipoId) : null,
      asesorId: form.asesorId ? Number(form.asesorId) : null,
      monitorId: form.monitorId ? Number(form.monitorId) : null,
      estudianteId: form.estudianteId ? Number(form.estudianteId) : null,
      partesIds: form.partesIds,
      contrapartesIds: form.contrapartesIds,
    };
    try {
      const res = await fetch(`${API_URL_BASE}/consultas/${idEditando}`, {
        method: "PUT", headers: { "Content-Type": "application/json" },
        credentials: "include", body: JSON.stringify(payload),
      });
      if (res.status === 401) { router.push("/"); return; }
      if (res.ok) {
        toast.success("Consulta actualizada");
        setMostrarFormEdicion(false);
        cargarConsultas(searchText);
      } else {
        const error = await res.json();
        const detalle = error.mensaje || error.message || error.descripcion || JSON.stringify(error);
        toast.error("Error al guardar", { description: detalle });
      }
    } catch { toast.error("Error de conexión"); }
    finally { setGuardando(false); }
  }

  async function handleArchivar(id) {
    if (!confirm("¿Archivar esta consulta? El registro quedará como Archivado.")) return;
    try {
      const res = await fetch(`${API_URL_BASE}/consultas/${id}/archivar`, { method: "PATCH", credentials: "include" });
      if (res.ok) { toast.success("Consulta archivada"); cargarConsultas(searchText); }
      else { toast.error("Error al archivar"); }
    } catch { toast.error("Error de conexión"); }
  }

  function renderPersona(p) {
    return (
      <>
        <div className="font-medium">{p.nombres} {p.apellidos}</div>
        <div className="text-xs text-muted-foreground">{p.numeroDocumento}</div>
      </>
    );
  }

  return (
    <>
      <div className="space-y-6">
        {/* BUSCADOR */}
        <div className="flex gap-3 items-end">
          <div className="flex-1 space-y-1">
            <label className="text-sm font-medium">Buscar consulta</label>
            <input value={searchText} onChange={e => setSearchText(e.target.value)}
              onKeyDown={e => e.key === "Enter" && cargarConsultas(searchText)}
              placeholder="Nombre, apellido, cédula o descripción..."
              className="w-full rounded-md border px-3 py-2 text-sm"
            />
          </div>
          <Button onClick={() => cargarConsultas(searchText)} disabled={loading}>
            {loading ? "Buscando..." : "Buscar"}
          </Button>
        </div>

        {/* FORMULARIO DE EDICIÓN INLINE */}
        {mostrarFormEdicion && (
          <div className="rounded-xl border bg-card p-6 shadow-sm space-y-4">
            <h2 className="text-lg font-semibold">Editar consulta #{idEditando}</h2>
            <form onSubmit={handleGuardar} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <C label="Fecha *"><input type="date" name="fecha" value={form.fecha} onChange={handleChange} required className={ic} /></C>
                <C label="Estado *">
                  <select name="estado" value={form.estado} onChange={handleChange} required className={ic}>
                    <option value="">Seleccione</option>
                    {ESTADOS.filter(e => e !== "Archivado").map(e => <option key={e} value={e}>{e}</option>)}
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

                {/* ASESOR */}
                <C label="Asesor">
                  <button type="button" onClick={() => setModalAsesor(p => ({ ...p, abierto: true }))}
                    className="flex h-9 w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-sm text-left hover:bg-muted/50 transition-colors">
                    <span className={asesorSeleccionado ? "text-foreground" : "text-muted-foreground"}>
                      {asesorSeleccionado ? `${asesorSeleccionado.nombre}${asesorSeleccionado.documento ? ` - ${asesorSeleccionado.documento}` : ""}` : "Sin asignar"}
                    </span>
                    <span className="text-muted-foreground">▼</span>
                  </button>
                </C>

                {/* MONITOR */}
                <C label="Monitor">
                  <button type="button" onClick={() => setModalMonitor(p => ({ ...p, abierto: true }))}
                    className="flex h-9 w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-sm text-left hover:bg-muted/50 transition-colors">
                    <span className={monitorSeleccionado ? "text-foreground" : "text-muted-foreground"}>
                      {monitorSeleccionado ? `${monitorSeleccionado.nombre}${monitorSeleccionado.documento ? ` - ${monitorSeleccionado.documento}` : ""}` : "Sin asignar"}
                    </span>
                    <span className="text-muted-foreground">▼</span>
                  </button>
                </C>

                {/* ESTUDIANTE */}
                <C label="Estudiante">
                  <button type="button" onClick={() => setModalEstudiante(p => ({ ...p, abierto: true }))}
                    className="flex h-9 w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-sm text-left hover:bg-muted/50 transition-colors">
                    <span className={estudianteSeleccionado ? "text-foreground" : "text-muted-foreground"}>
                      {estudianteSeleccionado ? `${estudianteSeleccionado.nombre}${estudianteSeleccionado.codigo ? ` - ${estudianteSeleccionado.codigo}` : ""}` : "Sin asignar"}
                    </span>
                    <span className="text-muted-foreground">▼</span>
                  </button>
                </C>
              </div>

              {/* PARTE PRINCIPAL */}
              <C label="Parte principal *">
                <button type="button" onClick={() => setModalParte(p => ({ ...p, abierto: true }))}
                  className="flex h-9 w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-sm text-left hover:bg-muted/50 transition-colors">
                  <span className={parteSeleccionada ? "text-foreground" : "text-muted-foreground"}>
                    {parteSeleccionada ? `${parteSeleccionada.nombres} ${parteSeleccionada.apellidos} - ${parteSeleccionada.numeroDocumento}` : "Buscar parte principal..."}
                  </span>
                  <span className="text-muted-foreground">▼</span>
                </button>
              </C>

              {/* PARTES ADICIONALES */}
              <C label="Partes adicionales">
                <button type="button" onClick={() => setModalPartesAdicionales(p => ({ ...p, abierto: true }))}
                  className="flex min-h-9 w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-sm text-left hover:bg-muted/50 transition-colors">
                  <span className={partesAdicionalesSeleccionadas.length > 0 ? "text-foreground" : "text-muted-foreground"}>
                    {partesAdicionalesSeleccionadas.length > 0
                      ? partesAdicionalesSeleccionadas.map(p => `${p.nombres} ${p.apellidos}`).join(", ")
                      : "Buscar y agregar partes..."}
                  </span>
                  <span className="text-muted-foreground">▼</span>
                </button>
              </C>

              {/* CONTRAPARTES */}
              <C label="Contrapartes">
                <button type="button" onClick={() => setModalContrapartes(p => ({ ...p, abierto: true }))}
                  className="flex min-h-9 w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-sm text-left hover:bg-muted/50 transition-colors">
                  <span className={contrapartesSeleccionadas.length > 0 ? "text-foreground" : "text-muted-foreground"}>
                    {contrapartesSeleccionadas.length > 0
                      ? contrapartesSeleccionadas.map(p => `${p.nombres} ${p.apellidos}`).join(", ")
                      : "Buscar y agregar contrapartes..."}
                  </span>
                  <span className="text-muted-foreground">▼</span>
                </button>
              </C>

              <C label="Descripción *"><textarea name="descripcion" value={form.descripcion} onChange={handleChange} required rows={3} placeholder="Resumen de la consulta" className={ic} /></C>
              <C label="Hechos *"><textarea name="hechos" value={form.hechos} onChange={handleChange} required rows={3} placeholder="Descripción de los hechos" className={ic} /></C>
              <C label="Pretensiones *"><textarea name="pretensiones" value={form.pretensiones} onChange={handleChange} required rows={3} placeholder="Qué solicita el consultante" className={ic} /></C>
              <C label="Concepto jurídico *"><textarea name="conceptoJuridico" value={form.conceptoJuridico} onChange={handleChange} required rows={3} placeholder="Fundamento legal aplicable" className={ic} /></C>
              <C label="Observaciones"><textarea name="observaciones" value={form.observaciones} onChange={handleChange} rows={2} placeholder="Opcional" className={ic} /></C>

              <div className="space-y-2">
                <label className="text-sm font-medium">Archivos relacionados</label>
                {cargandoArchivos ? (
                  <p className="text-sm text-muted-foreground">Cargando archivos...</p>
                ) : archivosCaso.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No hay archivos adjuntos.</p>
                ) : (
                  <ul className="space-y-2">
                    {archivosCaso.map(fileName => (
                      <li key={fileName} className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
                        <span className="truncate">{fileName}</span>
                        <button type="button" onClick={() => descargarArchivo(fileName)} className="text-primary hover:underline">Descargar</button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <Button type="button" variant="outline" onClick={() => setMostrarFormEdicion(false)} disabled={guardando}>Cancelar</Button>
                <Button type="submit" disabled={guardando}>{guardando ? "Guardando..." : "Actualizar"}</Button>
              </div>
            </form>
          </div>
        )}

        {/* TABLA DE RESULTADOS */}
        <div className="overflow-hidden rounded-lg border bg-card">
          <table className="min-w-full">
            <thead className="bg-muted">
              <tr>
                {["ID", "Consulta", "Fecha", "Nombre", "Apellido", "Cédula", "Estado", "Acciones"].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr><td colSpan={8} className="text-center py-8 text-sm text-muted-foreground">{loading ? "Cargando..." : "Sin resultados. Usa el buscador o crea una nueva consulta."}</td></tr>
              ) : rows.map(row => (
                <tr key={row.id} className="border-t hover:bg-muted/50 transition-colors">
                  <td className="px-4 py-3 text-sm">{row.id}</td>
                  <td className="px-4 py-3 text-sm max-w-[200px] truncate" title={row.consulta}>{row.consulta}</td>
                  <td className="px-4 py-3 text-sm">{row.fecha}</td>
                  <td className="px-4 py-3 text-sm">{row.nombre}</td>
                  <td className="px-4 py-3 text-sm">{row.apellido}</td>
                  <td className="px-4 py-3 text-sm">{row.cedula}</td>
                  <td className="px-4 py-3 text-sm">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      row.estado === "Archivado" ? "bg-gray-100 text-gray-600" :
                      row.estado === "Cerrado"   ? "bg-red-100 text-red-600" :
                      row.estado === "Urgente"   ? "bg-orange-100 text-orange-600" :
                      row.estado === "Activo"    ? "bg-green-100 text-green-600" :
                      "bg-blue-100 text-blue-600"
                    }`}>
                      {row.estado ?? "Sin estado"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <div className="flex gap-2">
                      <Button size="sm" variant="outline" onClick={() => abrirEditar(row.id)}>Editar</Button>
                      {esAdministrativo && (
                        <Button size="sm" variant="destructive" onClick={() => handleArchivar(row.id)} disabled={row.estado === "Archivado"}>
                          {row.estado === "Archivado" ? "Archivado" : "Archivar"}
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* MODAL ASESOR */}
      <ModalSimple
        abierto={modalAsesor.abierto} titulo="Seleccionar Asesor"
        items={asesoresFiltrados} busqueda={modalAsesor.busqueda}
        setBusqueda={v => setModalAsesor(p => ({ ...p, busqueda: v }))}
        onSeleccionar={item => { setForm(prev => ({ ...prev, asesorId: item ? String(item.id) : "" })); setModalAsesor({ abierto: false, busqueda: "" }); }}
        onCerrar={() => setModalAsesor({ abierto: false, busqueda: "" })}
        seleccionado={asesorSeleccionado}
        renderItem={a => (<><div className="font-medium">{a.nombre}</div><div className="text-xs text-muted-foreground">{a.documento}</div></>)}
      />

      {/* MODAL MONITOR */}
      <ModalSimple
        abierto={modalMonitor.abierto} titulo="Seleccionar Monitor"
        items={monitoresFiltrados} busqueda={modalMonitor.busqueda}
        setBusqueda={v => setModalMonitor(p => ({ ...p, busqueda: v }))}
        onSeleccionar={item => { setForm(prev => ({ ...prev, monitorId: item ? String(item.id) : "" })); setModalMonitor({ abierto: false, busqueda: "" }); }}
        onCerrar={() => setModalMonitor({ abierto: false, busqueda: "" })}
        seleccionado={monitorSeleccionado}
        renderItem={m => (<><div className="font-medium">{m.nombre}</div><div className="text-xs text-muted-foreground">{m.documento}</div></>)}
      />

      {/* MODAL ESTUDIANTE */}
      <ModalSimple
        abierto={modalEstudiante.abierto} titulo="Seleccionar Estudiante"
        items={estudiantesFiltrados} busqueda={modalEstudiante.busqueda}
        setBusqueda={v => setModalEstudiante(p => ({ ...p, busqueda: v }))}
        onSeleccionar={item => { setForm(prev => ({ ...prev, estudianteId: item ? String(item.id) : "" })); setModalEstudiante({ abierto: false, busqueda: "" }); }}
        onCerrar={() => setModalEstudiante({ abierto: false, busqueda: "" })}
        seleccionado={estudianteSeleccionado}
        renderItem={e => (<><div className="font-medium">{e.nombre}</div><div className="text-xs text-muted-foreground">{e.codigo} — {e.documento}</div></>)}
      />

      {/* MODAL PARTE PRINCIPAL */}
      <ModalSimple
        abierto={modalParte.abierto} titulo="Seleccionar Parte Principal"
        items={parteFiltrada} busqueda={modalParte.busqueda}
        setBusqueda={v => setModalParte(p => ({ ...p, busqueda: v }))}
        onSeleccionar={item => { setForm(prev => ({ ...prev, personaId: item ? String(item.id) : "" })); setModalParte({ abierto: false, busqueda: "" }); }}
        onCerrar={() => setModalParte({ abierto: false, busqueda: "" })}
        seleccionado={parteSeleccionada}
        renderItem={renderPersona}
      />

      {/* MODAL PARTES ADICIONALES */}
      <ModalMultiple
        abierto={modalPartesAdicionales.abierto} titulo="Seleccionar Partes Adicionales"
        items={partesAdicionalesFiltradas} busqueda={modalPartesAdicionales.busqueda}
        setBusqueda={v => setModalPartesAdicionales(p => ({ ...p, busqueda: v }))}
        onConfirmar={ids => { setForm(prev => ({ ...prev, partesIds: ids })); setModalPartesAdicionales({ abierto: false, busqueda: "" }); }}
        onCerrar={() => setModalPartesAdicionales({ abierto: false, busqueda: "" })}
        seleccionados={form.partesIds}
        renderItem={renderPersona}
      />

      {/* MODAL CONTRAPARTES */}
      <ModalMultiple
        abierto={modalContrapartes.abierto} titulo="Seleccionar Contrapartes"
        items={contrapartesFiltradas} busqueda={modalContrapartes.busqueda}
        setBusqueda={v => setModalContrapartes(p => ({ ...p, busqueda: v }))}
        onConfirmar={ids => { setForm(prev => ({ ...prev, contrapartesIds: ids })); setModalContrapartes({ abierto: false, busqueda: "" }); }}
        onCerrar={() => setModalContrapartes({ abierto: false, busqueda: "" })}
        seleccionados={form.contrapartesIds}
        renderItem={renderPersona}
      />
    </>
  );
}

function C({ label, children }) {
  return <div className="flex flex-col gap-1"><label className="text-sm font-medium">{label}</label>{children}</div>;
}

const ic = "w-full rounded-md border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring disabled:opacity-50";