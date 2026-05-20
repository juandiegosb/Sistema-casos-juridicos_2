"use client";

import React, { useEffect, useMemo, useState } from "react";
import { Search, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { API_URL_BASE } from "@/lib/config";
import { ConfirmActionDialog } from "@/components/ui/ConfirmActionDialog";
import { useRouter } from "next/navigation";
import { PERMISOS } from "@/lib/permission";
import { tienePermiso } from "@/lib/authz";

const FORM_INICIAL = {
  tipoUsuario: "",
  tipoDocumento: "",
  numeroDocumento: "",
  fechaExpedicion: "",
  ciudadExpedicion: "",
  nombres: "",
  apellidos: "",
  nombreIdentitario: "",
  pronombre: "",
  sexo: "",
  genero: "",
  orientacionSexual: "",
  fechaNacimiento: "",
  telefono: "",
  correo: "",
  nacionalidad: "",
  estadoCivil: "",
  escolaridad: "",
  grupoEtnico: "",
  condicionActual: "",
  sabeLeerEscribir: false,
  discapacidad: "",
  caracterizacionPcd: "",
  necesitaAjustePcd: false,
  departamento: "",
  municipio: "",
  barrio: "",
  direccion: "",
  comuna: "",
  localidad: "",
  estrato: 0,
  tipoVivienda: "",
  zona: "",
  tenencia: "",
  numeroPersonasACargo: 0,
  ingresosAdicionales: false,
  energiaElectrica: false,
  acueducto: false,
  alcantarillado: false,
  ocupacion: "",
  empresa: "",
  salario: 0,
  cargo: "",
  direccionEmpresa: "",
  telefonoEmpresa: "",
  nombreCompletoAcudiente: "",
  relacionAcudiente: "",
  telefonoAcudiente: "",
  correoAcudiente: "",
  direccionAcudiente: "",
  comoSeEntero: "",
  relacionConUniversidad: "",
};

function normalizar(value) {
  return String(value || "").trim().toUpperCase();
}

function esAdministrador(user) {
  return normalizar(user?.rolNombre) === "ADMINISTRADOR";
}

async function leerRespuesta(res) {
  const text = await res.text();

  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return { mensaje: text };
  }
}

function nombreCompleto(persona) {
  return [persona?.nombres, persona?.apellidos].filter(Boolean).join(" ");
}

function valorTexto(value) {
  return value || "N/A";
}

function mostrarTipoPersona(tipoUsuario) {
  const tipo = String(tipoUsuario || "").trim().toLowerCase();

  if (tipo === "consultante") return "Usuario general";
  if (tipo === "victima" || tipo === "víctima") return "Víctima";
  if (tipo === "usuario general") return "Usuario general";

  return tipoUsuario || "N/A";
}

function convertirPersonaAForm(persona) {
  return {
    ...FORM_INICIAL,
    ...persona,
    tipoUsuario: mostrarTipoPersona(persona.tipoUsuario),
    sabeLeerEscribir: Boolean(persona.sabeLeerEscribir),
    necesitaAjustePcd: Boolean(persona.necesitaAjustePcd),
    ingresosAdicionales: Boolean(persona.ingresosAdicionales),
    energiaElectrica: Boolean(persona.energiaElectrica),
    acueducto: Boolean(persona.acueducto),
    alcantarillado: Boolean(persona.alcantarillado),
    estrato: persona.estrato ?? 0,
    numeroPersonasACargo: persona.numeroPersonasACargo ?? 0,
    salario: persona.salario ?? 0,
  };
}

export function PersonasForm() {
  const [user, setUser] = useState(null);
  const [personas, setPersonas] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [personaEditando, setPersonaEditando] = useState(null);
  const [personaADesactivar, setPersonaADesactivar] = useState(null);
  const [form, setForm] = useState(FORM_INICIAL);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [desactivando, setDesactivando] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const router = useRouter();

  const puedeEditar = tienePermiso(user, PERMISOS.EDITAR_PERSONAS);
  const puedeDesactivar = tienePermiso(
    user,
    PERMISOS.CAMBIAR_ESTADO_PERSONAS
  );

  const personasFiltradas = useMemo(() => {
    const q = busqueda.trim().toLowerCase();

    if (!q) return personas;

    return personas.filter((persona) =>
      [
        persona.id,
        persona.tipoUsuario,
        persona.tipoDocumento,
        persona.numeroDocumento,
        persona.nombres,
        persona.apellidos,
        persona.telefono,
        persona.correo,
        persona.municipio,
        persona.barrio,
        persona.direccion,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(q)
    );
  }, [personas, busqueda]);

  useEffect(() => {
    cargarInicial();
  }, [router]);

  async function cargarInicial() {
    try {
      setLoading(true);
      setError("");
      setMensaje("");

      const meRes = await fetch(`${API_URL_BASE}/auth/me`, {
        credentials: "include",
      });

      if (meRes.status === 401) {
        router.replace("/");
        return;
      }

      if (!meRes.ok) {
        router.replace("/");
        return;
      }

      const meData = await meRes.json();

      const puedeEntrar =
        tienePermiso(meData, PERMISOS.ACCEDER_PERSONAS) &&
        tienePermiso(meData, PERMISOS.VER_PERSONAS);

      if (!puedeEntrar) {
        router.replace("/inicio");
        return;
      }

      setUser(meData);

      const personasRes = await fetch(`${API_URL_BASE}/personas/activos`, {
        credentials: "include",
      });

      if (personasRes.status === 401) {
        router.replace("/");
        return;
      }

      if (personasRes.status === 403) {
        router.replace("/inicio");
        return;
      }

      if (!personasRes.ok) {
        throw new Error("No se pudieron cargar las personas");
      }

      const personasData = await personasRes.json();
      setPersonas(Array.isArray(personasData) ? personasData : []);
    } catch (err) {
      console.error(err);
      setError(err.message || "Error cargando personas");
    } finally {
      setLoading(false);
    }
  }

  function abrirEdicion(persona) {
    if (!puedeEditar) {
      setError("No tienes permisos para editar personas");
      return;
    }

    setMensaje("");
    setError("");
    setPersonaEditando(persona);
    setForm(convertirPersonaAForm(persona));
  }

  function cerrarEdicion() {
    setPersonaEditando(null);
    setForm(FORM_INICIAL);
    setSaving(false);
  }

  function abrirConfirmacionDesactivar(persona) {
    setMensaje("");
    setError("");
    setPersonaADesactivar(persona);
  }

  function cerrarConfirmacionDesactivar() {
    if (desactivando) return;
    setPersonaADesactivar(null);
  }

  function handleChange(event) {
    const { name, value, type, checked } = event.target;

    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  }

  function handleNumberChange(event) {
    const { name, value } = event.target;

    setForm((prev) => ({
      ...prev,
      [name]: value === "" ? 0 : Number(value),
    }));
  }

  async function guardarEdicion(event) {
    event.preventDefault();

    if (!personaEditando?.id) {
      setError("No hay una persona seleccionada");
      return;
    }

    if (!puedeEditar) {
      setError("No tienes permisos para editar personas");
      return;
    }

    try {
      setSaving(true);
      setError("");
      setMensaje("");

      const payload = {
        ...form,
        id: personaEditando.id,
        estrato: Number(form.estrato || 0),
        numeroPersonasACargo: Number(form.numeroPersonasACargo || 0),
        salario: Number(form.salario || 0),
      };

      const res = await fetch(`${API_URL_BASE}/personas/${personaEditando.id}`, {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      const data = await leerRespuesta(res);

      if (res.status === 401) {
        router.replace("/");
        return;
      }

      if (res.status === 403) {
        router.replace("/inicio");
        return;
      }

      if (!res.ok) {
        throw new Error(
          data?.mensaje || data?.message || "No se pudo actualizar la persona"
        );
      }

      setMensaje("Persona actualizada correctamente");
      cerrarEdicion();
      await cargarInicial();
    } catch (err) {
      console.error(err);
      setError(err.message || "Error actualizando persona");
    } finally {
      setSaving(false);
    }
  }

  async function confirmarDesactivarPersona() {
    if (!personaADesactivar?.id) return;

    if (!puedeDesactivar) {
      setError("No tienes permisos para desactivar personas");
      setPersonaADesactivar(null);
      return;
    }

    try {
      setDesactivando(true);
      setError("");
      setMensaje("");

      const res = await fetch(
        `${API_URL_BASE}/personas/${personaADesactivar.id}/desactivar`,
        {
          method: "PATCH",
          credentials: "include",
        }
      );

      const data = await leerRespuesta(res);

      if (res.status === 401) {
        router.replace("/");
        return;
      }

      if (res.status === 403) {
        router.replace("/inicio");
        return;
      }

      if (!res.ok) {
        throw new Error(
          data?.mensaje || data?.message || "No se pudo desactivar la persona"
        );
      }

      setMensaje("Persona desactivada correctamente");
      setPersonaADesactivar(null);

      setPersonas((prev) =>
        prev.map((persona) =>
          persona.id === personaADesactivar.id
            ? { ...persona, activo: false }
            : persona
        )
      );

      await cargarInicial();
    } catch (err) {
      console.error(err);
      setError(err.message || "Error desactivando persona");
    } finally {
      setDesactivando(false);
    }
  }

  if (loading) {
    return (
      <div className="rounded-xl border bg-card p-8 text-center text-muted-foreground">
        Cargando personas...
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {error && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      )}

      {mensaje && (
        <div className="rounded-lg border border-primary/30 bg-primary/10 px-4 py-3 text-sm text-primary">
          {mensaje}
        </div>
      )}

      <div className="rounded-xl border bg-card p-5 space-y-4">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <h3 className="text-xl font-bold">Personas registradas</h3>
            <p className="text-sm text-muted-foreground">
              Listado general de personas creadas desde recepción.
            </p>
          </div>

          <Button type="button" variant="outline" onClick={cargarInicial}>
            Actualizar
          </Button>
        </div>

        <div className="relative">
          <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
          <input
            value={busqueda}
            onChange={(event) => setBusqueda(event.target.value)}
            placeholder="Buscar por documento, nombre, teléfono, correo o dirección..."
            className="h-10 w-full rounded-md border bg-background pl-9 pr-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
          />
        </div>

        <div className="overflow-hidden rounded-lg border border-primary/30">
          <div className="max-h-[560px] overflow-auto">
            <table className="w-full text-sm">
              <thead className="sticky top-0 bg-primary/20">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">ID</th>
                  <th className="px-4 py-3 text-left font-medium">Persona</th>
                  <th className="px-4 py-3 text-left font-medium">Documento</th>
                  <th className="px-4 py-3 text-left font-medium">Contacto</th>
                  <th className="px-4 py-3 text-left font-medium">Ubicación</th>
                  <th className="px-4 py-3 text-left font-medium">Tipo</th>
                  <th className="px-4 py-3 text-right font-medium">Acciones</th>
                </tr>
              </thead>

              <tbody>
                {personasFiltradas.length === 0 ? (
                  <tr>
                    <td
                      colSpan={7}
                      className="px-4 py-8 text-center text-muted-foreground"
                    >
                      No hay personas para mostrar.
                    </td>
                  </tr>
                ) : (
                  personasFiltradas.map((persona) => (
                    <tr
                      key={persona.id}
                      className="border-t border-primary/20 transition hover:bg-primary/10"
                    >
                      <td className="px-4 py-3">{persona.id}</td>

                      <td className="px-4 py-3">
                        <div className="font-medium">
                          {nombreCompleto(persona) || "Sin nombre"}
                        </div>
                        <div className="text-xs text-muted-foreground">
                          Nacimiento: {valorTexto(persona.fechaNacimiento)}
                        </div>
                      </td>

                      <td className="px-4 py-3">
                        <div>{valorTexto(persona.tipoDocumento)}</div>
                        <div className="text-xs text-muted-foreground">
                          {valorTexto(persona.numeroDocumento)}
                        </div>
                      </td>

                      <td className="px-4 py-3">
                        <div>{valorTexto(persona.telefono)}</div>
                        <div className="text-xs text-muted-foreground">
                          {valorTexto(persona.correo)}
                        </div>
                      </td>

                      <td className="px-4 py-3">
                        <div>{valorTexto(persona.municipio)}</div>
                        <div className="text-xs text-muted-foreground">
                          {valorTexto(persona.direccion)}
                        </div>
                      </td>

                      <td className="px-4 py-3">
                        <span className="rounded-full border border-primary/30 bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary">
                          {mostrarTipoPersona(persona.tipoUsuario)}
                        </span>
                      </td>

                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-2">
                          {puedeEditar && (
                            <Button
                              type="button"
                              variant="outline"
                              size="sm"
                              onClick={() => abrirEdicion(persona)}
                              className="border-primary/30 bg-primary/10 hover:bg-primary/20"
                            >
                              Editar
                            </Button>
                          )}

                          {puedeDesactivar && (
                            <Button
                              type="button"
                              variant="destructive"
                              size="sm"
                              disabled={persona.activo === false}
                              onClick={() => abrirConfirmacionDesactivar(persona)}
                              className="bg-destructive/20 text-destructive hover:bg-destructive/30 disabled:cursor-not-allowed disabled:opacity-60"
                            >
                              {persona.activo === false ? "Inactivo" : "Desactivar"}
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {personaEditando && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/70 px-4 backdrop-blur-sm">
          <div className="flex max-h-[92vh] w-full max-w-6xl flex-col overflow-hidden rounded-2xl border bg-card shadow-2xl">
            <div className="flex items-center justify-between border-b bg-primary/10 px-6 py-5">
              <div>
                <h3 className="text-xl font-bold">Editar persona</h3>
                <p className="text-sm text-muted-foreground">
                  Actualiza la información general de {nombreCompleto(personaEditando)}
                </p>
              </div>

              <button
                type="button"
                onClick={cerrarEdicion}
                className="rounded-full border bg-background p-2 text-muted-foreground transition hover:bg-muted hover:text-foreground"
              >
                <X className="size-5" />
              </button>
            </div>

            <form onSubmit={guardarEdicion} className="flex min-h-0 flex-1 flex-col">
              <div className="flex-1 overflow-auto p-6">
                <div className="space-y-6">
                  <Seccion titulo="Información básica">
                    <Input label="Tipo usuario" name="tipoUsuario" value={form.tipoUsuario} onChange={handleChange} />
                    <Input label="Tipo documento" name="tipoDocumento" value={form.tipoDocumento} onChange={handleChange} />
                    <Input label="Número documento" name="numeroDocumento" value={form.numeroDocumento} onChange={handleChange} />
                    <Input label="Fecha expedición" name="fechaExpedicion" type="date" value={form.fechaExpedicion} onChange={handleChange} />
                    <Input label="Ciudad expedición" name="ciudadExpedicion" value={form.ciudadExpedicion} onChange={handleChange} />
                    <Input label="Nombres" name="nombres" value={form.nombres} onChange={handleChange} />
                    <Input label="Apellidos" name="apellidos" value={form.apellidos} onChange={handleChange} />
                    <Input label="Nombre identitario" name="nombreIdentitario" value={form.nombreIdentitario} onChange={handleChange} />
                    <Input label="Pronombre" name="pronombre" value={form.pronombre} onChange={handleChange} />
                    <Input label="Sexo" name="sexo" value={form.sexo} onChange={handleChange} />
                    <Input label="Género" name="genero" value={form.genero} onChange={handleChange} />
                    <Input label="Orientación sexual" name="orientacionSexual" value={form.orientacionSexual} onChange={handleChange} />
                    <Input label="Fecha nacimiento" name="fechaNacimiento" type="date" value={form.fechaNacimiento} onChange={handleChange} />
                  </Seccion>

                  <Seccion titulo="Contacto">
                    <Input label="Teléfono" name="telefono" value={form.telefono} onChange={handleChange} />
                    <Input label="Correo" name="correo" type="email" value={form.correo} onChange={handleChange} />
                    <Input label="Nacionalidad" name="nacionalidad" value={form.nacionalidad} onChange={handleChange} />
                    <Input label="Estado civil" name="estadoCivil" value={form.estadoCivil} onChange={handleChange} />
                    <Input label="Escolaridad" name="escolaridad" value={form.escolaridad} onChange={handleChange} />
                    <Input label="Grupo étnico" name="grupoEtnico" value={form.grupoEtnico} onChange={handleChange} />
                    <Input label="Condición actual" name="condicionActual" value={form.condicionActual} onChange={handleChange} />
                    <Input label="Discapacidad" name="discapacidad" value={form.discapacidad} onChange={handleChange} />
                    <Input label="Caracterización PCD" name="caracterizacionPcd" value={form.caracterizacionPcd} onChange={handleChange} />
                  </Seccion>

                  <Seccion titulo="Ubicación y vivienda">
                    <Input label="Departamento" name="departamento" value={form.departamento} onChange={handleChange} />
                    <Input label="Municipio" name="municipio" value={form.municipio} onChange={handleChange} />
                    <Input label="Barrio" name="barrio" value={form.barrio} onChange={handleChange} />
                    <Input label="Dirección" name="direccion" value={form.direccion} onChange={handleChange} />
                    <Input label="Comuna" name="comuna" value={form.comuna} onChange={handleChange} />
                    <Input label="Localidad" name="localidad" value={form.localidad} onChange={handleChange} />
                    <Input label="Estrato" name="estrato" type="number" value={form.estrato} onChange={handleNumberChange} />
                    <Input label="Tipo vivienda" name="tipoVivienda" value={form.tipoVivienda} onChange={handleChange} />
                    <Input label="Zona" name="zona" value={form.zona} onChange={handleChange} />
                    <Input label="Tenencia" name="tenencia" value={form.tenencia} onChange={handleChange} />
                    <Input label="Personas a cargo" name="numeroPersonasACargo" type="number" value={form.numeroPersonasACargo} onChange={handleNumberChange} />
                  </Seccion>

                  <Seccion titulo="Economía">
                    <Input label="Ocupación" name="ocupacion" value={form.ocupacion} onChange={handleChange} />
                    <Input label="Empresa" name="empresa" value={form.empresa} onChange={handleChange} />
                    <Input label="Salario" name="salario" type="number" value={form.salario} onChange={handleNumberChange} />
                    <Input label="Cargo" name="cargo" value={form.cargo} onChange={handleChange} />
                    <Input label="Dirección empresa" name="direccionEmpresa" value={form.direccionEmpresa} onChange={handleChange} />
                    <Input label="Teléfono empresa" name="telefonoEmpresa" value={form.telefonoEmpresa} onChange={handleChange} />
                  </Seccion>

                  <Seccion titulo="Acudiente">
                    <Input label="Nombre acudiente" name="nombreCompletoAcudiente" value={form.nombreCompletoAcudiente} onChange={handleChange} />
                    <Input label="Relación acudiente" name="relacionAcudiente" value={form.relacionAcudiente} onChange={handleChange} />
                    <Input label="Teléfono acudiente" name="telefonoAcudiente" value={form.telefonoAcudiente} onChange={handleChange} />
                    <Input label="Correo acudiente" name="correoAcudiente" value={form.correoAcudiente} onChange={handleChange} />
                    <Input label="Dirección acudiente" name="direccionAcudiente" value={form.direccionAcudiente} onChange={handleChange} />
                  </Seccion>

                  <Seccion titulo="Servicio">
                    <Input label="Cómo se enteró" name="comoSeEntero" value={form.comoSeEntero} onChange={handleChange} />
                    <Input label="Relación con universidad" name="relacionConUniversidad" value={form.relacionConUniversidad} onChange={handleChange} />
                  </Seccion>

                  <div className="rounded-xl border bg-muted/20 p-4">
                    <h4 className="mb-3 font-semibold">Condiciones adicionales</h4>

                    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                      <Checkbox label="Sabe leer y escribir" name="sabeLeerEscribir" checked={form.sabeLeerEscribir} onChange={handleChange} />
                      <Checkbox label="Necesita ajuste PCD" name="necesitaAjustePcd" checked={form.necesitaAjustePcd} onChange={handleChange} />
                      <Checkbox label="Ingresos adicionales" name="ingresosAdicionales" checked={form.ingresosAdicionales} onChange={handleChange} />
                      <Checkbox label="Energía eléctrica" name="energiaElectrica" checked={form.energiaElectrica} onChange={handleChange} />
                      <Checkbox label="Acueducto" name="acueducto" checked={form.acueducto} onChange={handleChange} />
                      <Checkbox label="Alcantarillado" name="alcantarillado" checked={form.alcantarillado} onChange={handleChange} />
                    </div>
                  </div>
                </div>
              </div>

              <div className="flex justify-end gap-3 border-t bg-card px-6 py-4">
                <Button type="button" variant="outline" onClick={cerrarEdicion} disabled={saving}>
                  Cancelar
                </Button>

                <Button type="submit" disabled={saving}>
                  {saving ? "Guardando..." : "Guardar cambios"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      <ConfirmActionDialog
        open={Boolean(personaADesactivar)}
        title="Desactivar persona"
        description={`¿Deseas desactivar a "${
          nombreCompleto(personaADesactivar) || "esta persona"
        }"? Podrás reactivarla después desde la página de eliminación.`}
        confirmText="Desactivar"
        cancelText="Cancelar"
        loading={desactivando}
        onClose={cerrarConfirmacionDesactivar}
        onConfirm={confirmarDesactivarPersona}
      />
    </div>
  );
}

function Seccion({ titulo, children }) {
  return (
    <section className="rounded-xl border bg-background p-4">
      <h4 className="mb-4 border-b pb-2 font-semibold">{titulo}</h4>

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-3">
        {children}
      </div>
    </section>
  );
}

function Input({ label, name, value, onChange, type = "text" }) {
  return (
    <label className="flex flex-col gap-1.5 text-sm">
      <span className="font-medium">{label}</span>
      <input
        type={type}
        name={name}
        value={value ?? ""}
        onChange={onChange}
        className="h-10 rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
      />
    </label>
  );
}

function Checkbox({ label, name, checked, onChange }) {
  return (
    <label className="flex items-center gap-2 rounded-md border bg-background px-3 py-2 text-sm">
      <input
        type="checkbox"
        name={name}
        checked={Boolean(checked)}
        onChange={onChange}
        className="h-4 w-4"
      />
      <span>{label}</span>
    </label>
  );
}