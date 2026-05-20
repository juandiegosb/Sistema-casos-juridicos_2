"use client";

import React, { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { API_URL_BASE } from "@/lib/config";
import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { FormCheckbox } from "./parts/FormCheckbox";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { PERMISOS } from "@/lib/permission";
import { tienePermiso } from "@/lib/authz";

const TIPOS_PERFIL = [
  { value: "ADMINISTRATIVO", label: "Administrativo", endpoint: "administrativo", endpointActual: "administrativos", rolId: 1 },
  { value: "ASESOR", label: "Asesor", endpoint: "asesor", endpointActual: "asesores", rolId: 2 },
  { value: "ESTUDIANTE", label: "Estudiante", endpoint: "estudiante", endpointActual: "estudiantes", rolId: 3 },
  { value: "MONITOR", label: "Monitor", endpoint: "monitor", endpointActual: "monitores", rolId: 4 },
  { value: "CONCILIADOR", label: "Conciliador", endpoint: "conciliador", endpointActual: "conciliadores", rolId: 5 },
];

const VALORES_INICIALES = {
  usuarioSistemaId: "",
  destino: "",
  motivo: "",
  nombre: "",
  tipoDocumentoId: "",
  documento: "",
  telefono: "",
  usuario: "",
  codigo: "",
  sedeId: "",
  asesorId: "",
  areaId: "",
  conciliacion: false,
  directora: false,
  tipoConciliador: "",
};

function usuarioActivo(usuario) {
  return (
    usuario?.activo !== false &&
    String(usuario?.estado || "").toUpperCase() !== "INACTIVO"
  );
}

function filtrarActivos(lista) {
  return Array.isArray(lista) ? lista.filter(usuarioActivo) : [];
}

export function CambiarRolUsuarioForm() {
  const router = useRouter();

  const [user, setUser] = useState(null);
  const [usuarios, setUsuarios] = useState([]);
  const [tiposDocumento, setTiposDocumento] = useState([]);
  const [sedes, setSedes] = useState([]);
  const [asesores, setAsesores] = useState([]);
  const [areas, setAreas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [perfilPrevioInfo, setPerfilPrevioInfo] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [busquedaModal, setBusquedaModal] = useState("");

  const {
    register,
    handleSubmit,
    watch,
    reset,
    setValue,
    formState: { errors },
  } = useForm({ defaultValues: VALORES_INICIALES });

  const REQUIRED = "Campo obligatorio";
  const usuarioSistemaId = watch("usuarioSistemaId");
  const destino = watch("destino");

  const usuarioSeleccionado = useMemo(() => {
    return usuarios.find((usuario) => String(usuario.id) === String(usuarioSistemaId));
  }, [usuarios, usuarioSistemaId]);

  const perfilDestino = useMemo(() => {
    return TIPOS_PERFIL.find((perfil) => perfil.value === destino);
  }, [destino]);

  const puedeAsignarRol = tienePermiso(user, PERMISOS.ASIGNAR_ROL_USUARIOS);
  const puedeGestionarAdministradores = tienePermiso(
    user,
    PERMISOS.GESTIONAR_ADMINISTRADORES
  );

  const perfilesDisponibles = useMemo(() => {
    let perfiles = TIPOS_PERFIL;

    if (!puedeGestionarAdministradores) {
      perfiles = perfiles.filter((perfil) => perfil.value !== "ADMINISTRATIVO");
    }

    if (!usuarioSeleccionado?.tipoPerfil) return perfiles;

    return perfiles.filter((perfil) => perfil.value !== usuarioSeleccionado.tipoPerfil);
  }, [usuarioSeleccionado, puedeGestionarAdministradores]);

  const usuariosFiltrados = useMemo(() => {
    const activos = filtrarActivos(usuarios);
    const q = busquedaModal.trim().toLowerCase();

    if (!q) return activos;

    return activos.filter((usuario) =>
      `${usuario.username || ""} ${usuario.rolNombre || ""} ${usuario.tipoPerfil || ""}`
        .toLowerCase()
        .includes(q)
    );
  }, [usuarios, busquedaModal]);

  useEffect(() => {
    cargarDatosIniciales();
  }, []);

  useEffect(() => {
    if (!usuarioSeleccionado) return;

    setValue("destino", "");
    setPerfilPrevioInfo(null);
    cargarDatosActualesUsuario(usuarioSeleccionado);
  }, [usuarioSeleccionado, setValue]);

  useEffect(() => {
    if (!usuarioSeleccionado || !perfilDestino) {
      setPerfilPrevioInfo(null);
      return;
    }

    cargarPerfilPrevio(usuarioSeleccionado.id, perfilDestino);
  }, [usuarioSeleccionado, perfilDestino]);

  async function leerRespuesta(response) {
    const text = await response.text();

    if (!text) return null;

    try {
      return JSON.parse(text);
    } catch {
      return { mensaje: text };
    }
  }

  async function cargarDatosIniciales() {
    try {
      setLoading(true);

      const meRes = await fetch(`${API_URL_BASE}/auth/me`, {
        method: "GET",
        credentials: "include",
      });

      if (meRes.status === 401 || !meRes.ok) {
        router.replace("/");
        return;
      }

      const meData = await meRes.json();

      if (!tienePermiso(meData, PERMISOS.ASIGNAR_ROL_USUARIOS)) {
        router.replace("/inicio");
        return;
      }

      setUser(meData);

      const [usuariosRes, tiposRes, sedesRes, asesoresRes, areasRes] =
        await Promise.all([
          fetch(`${API_URL_BASE}/usuarios-sistema/activos`, { credentials: "include" }),
          fetch(`${API_URL_BASE}/tipos-documento`, { credentials: "include" }),
          fetch(`${API_URL_BASE}/sedes`, { credentials: "include" }),
          fetch(`${API_URL_BASE}/asesores/activos`, { credentials: "include" }),
          fetch(`${API_URL_BASE}/areas`, { credentials: "include" }),
        ]);

      if (usuariosRes.status === 401) {
        router.replace("/");
        return;
      }

      if (usuariosRes.status === 403) {
        router.replace("/inicio");
        return;
      }

      const usuariosData = usuariosRes.ok ? await usuariosRes.json() : [];
      const tiposData = tiposRes.ok ? await tiposRes.json() : [];
      const sedesData = sedesRes.ok ? await sedesRes.json() : [];
      const asesoresData = asesoresRes.ok ? await asesoresRes.json() : [];
      const areasData = areasRes.ok ? await areasRes.json() : [];

      setUsuarios(filtrarActivos(usuariosData));

      setTiposDocumento(
        Array.isArray(tiposData)
          ? tiposData.map((tipo) => ({
              value: tipo.id,
              label:
                tipo.displayName ||
                tipo.nombre ||
                tipo.descripcion ||
                tipo.codigo ||
                String(tipo.id),
            }))
          : []
      );

      setSedes(
        Array.isArray(sedesData)
          ? sedesData.map((sede) => ({
              value: sede.id,
              label: sede.nombre,
            }))
          : []
      );

      setAsesores(
        filtrarActivos(asesoresData).map((asesor) => ({
          value: asesor.id,
          label: asesor.documento
            ? `${asesor.nombre} - ${asesor.documento}`
            : asesor.nombre,
        }))
      );

      setAreas(
        Array.isArray(areasData)
          ? areasData.map((area) => ({
              value: area.id,
              label: area.nombre,
            }))
          : []
      );
    } catch (error) {
      console.error(error);
      toast.error("Error cargando datos");
    } finally {
      setLoading(false);
    }
  }

  async function cargarDatosActualesUsuario(usuario) {
    const perfilActual = TIPOS_PERFIL.find(
      (perfil) => perfil.value === usuario.tipoPerfil
    );

    if (!perfilActual || !usuario.perfilId) {
      setValue("usuario", usuario.username || "");
      return;
    }

    try {
      const res = await fetch(
        `${API_URL_BASE}/${perfilActual.endpointActual}/${usuario.perfilId}`,
        { credentials: "include" }
      );

      if (!res.ok) {
        setValue("usuario", usuario.username || "");
        return;
      }

      const datos = await res.json();
      precargarDatosComunes(datos);
    } catch (error) {
      console.error("Error cargando datos actuales:", error);
      setValue("usuario", usuario.username || "");
    }
  }

  async function cargarPerfilPrevio(usuarioId, perfil) {
    try {
      setPerfilPrevioInfo(null);
      limpiarCamposDestino();

      const response = await fetch(
        `${API_URL_BASE}/usuarios-sistema/${usuarioId}/perfil/${perfil.endpoint}`,
        {
          method: "GET",
          credentials: "include",
        }
      );

      if (response.status === 401) {
        router.replace("/");
        return;
      }

      if (response.status === 403) {
        router.replace("/inicio");
        return;
      }

      if (response.status === 404) {
        setPerfilPrevioInfo({
          existe: false,
          activo: false,
          tipoPerfil: perfil.value,
          datos: null,
        });
        return;
      }

      if (!response.ok) return;

      const data = await response.json();
      setPerfilPrevioInfo(data);

      if (data?.datos) {
        precargarDatosPerfil(data.datos);
      }
    } catch (error) {
      console.error(error);
      limpiarCamposDestino();
    }
  }

  function precargarDatosComunes(datos) {
    setValue("nombre", datos.nombre || "");
    setValue("tipoDocumentoId", datos.tipoDocumentoId || "");
    setValue("documento", datos.documento || "");
    setValue("telefono", datos.telefono || "");
    setValue("usuario", datos.usuario || datos.username || "");
    setValue("codigo", datos.codigo || "");
    setValue("sedeId", datos.sedeId || "");
  }

  function limpiarCamposDestino() {
    setValue("asesorId", "");
    setValue("areaId", "");
    setValue("conciliacion", false);
    setValue("directora", false);
    setValue("tipoConciliador", "");
  }

  function precargarDatosPerfil(datos) {
    precargarDatosComunes(datos);
    setValue("asesorId", datos.asesorId || "");
    setValue("areaId", datos.areaId || "");
    setValue("conciliacion", Boolean(datos.conciliacion));
    setValue("directora", Boolean(datos.directora));
    setValue("tipoConciliador", datos.tipoConciliador || "");
  }

  function construirPayload(data) {
    const payload = {
      rolId: perfilDestino.rolId,
      motivo: data.motivo,
      nombre: data.nombre,
      telefono: data.telefono,
      usuario: data.usuario,
      codigo: data.codigo,
    };

    if (data.tipoDocumentoId) payload.tipoDocumentoId = Number(data.tipoDocumentoId);
    if (data.documento) payload.documento = data.documento;
    if (data.sedeId) payload.sedeId = Number(data.sedeId);

    if (destino === "ESTUDIANTE") {
      payload.asesorId = Number(data.asesorId);
      payload.conciliacion = Boolean(data.conciliacion);
    }

    if (destino === "ASESOR") {
      payload.areaId = Number(data.areaId);
    }

    if (destino === "ADMINISTRATIVO") {
      payload.directora = Boolean(data.directora);
    }

    if (destino === "CONCILIADOR") {
      payload.tipoConciliador = data.tipoConciliador;
    }

    return payload;
  }

  async function onSubmit(data) {
    if (!puedeAsignarRol) {
      router.replace("/inicio");
      return;
    }

    if (!usuarioSeleccionado) {
      toast.error("Selecciona un usuario");
      return;
    }

    if (!usuarioActivo(usuarioSeleccionado)) {
      toast.error("No puedes cambiar el rol de un usuario inactivo");
      return;
    }

    if (!perfilDestino) {
      toast.error("Selecciona el perfil destino");
      return;
    }

    if (destino === "ADMINISTRATIVO" && !puedeGestionarAdministradores) {
      toast.error("No tienes permiso para gestionar administradores");
      return;
    }

    try {
      setGuardando(true);

      const response = await fetch(
        `${API_URL_BASE}/usuarios-sistema/${usuarioSeleccionado.id}/perfil/${perfilDestino.endpoint}`,
        {
          method: "PATCH",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(construirPayload(data)),
        }
      );

      const result = await leerRespuesta(response);

      if (response.status === 401) {
        router.replace("/");
        return;
      }

      if (response.status === 403) {
        router.replace("/inicio");
        return;
      }

      if (!response.ok) {
        throw new Error(
          result?.mensaje ||
            result?.message ||
            "No se pudo cambiar el perfil del usuario"
        );
      }

      toast.success("Perfil y rol actualizados correctamente");

      setUsuarios((current) =>
        filtrarActivos(
          current.map((usuario) =>
            usuario.id === result.id ? result : usuario
          )
        )
      );

      reset({
        ...VALORES_INICIALES,
        usuarioSistemaId: String(result.id),
      });

      setPerfilPrevioInfo(null);
    } catch (error) {
      console.error(error);
      toast.error(error.message || "Error cambiando perfil");
    } finally {
      setGuardando(false);
    }
  }

  function seleccionarUsuario(usuario) {
    if (!usuarioActivo(usuario)) return;

    reset({
      ...VALORES_INICIALES,
      usuarioSistemaId: String(usuario.id),
    });

    setPerfilPrevioInfo(null);
    setModalAbierto(false);
    setBusquedaModal("");
  }

  function limpiarSeleccion() {
    reset(VALORES_INICIALES);
    setPerfilPrevioInfo(null);
    setBusquedaModal("");
  }

  function renderCamposComunes() {
    return (
      <>
        <FormInput
          name="motivo"
          label="Motivo del cambio"
          register={register}
          errors={errors}
          rules={{ required: REQUIRED }}
        />

        <FormInput
          name="nombre"
          label="Nombre completo"
          register={register}
          errors={errors}
          rules={{ required: REQUIRED }}
        />

        {tiposDocumento.length > 0 && (
          <FormSelect
            name="tipoDocumentoId"
            label="Tipo de documento"
            options={tiposDocumento}
            register={register}
            errors={errors}
            rules={{
              required:
                destino === "ESTUDIANTE" || destino === "ASESOR"
                  ? REQUIRED
                  : false,
              valueAsNumber: true,
            }}
          />
        )}

        <FormInput
          name="documento"
          label="Documento"
          register={register}
          errors={errors}
          rules={{
            required:
              destino === "ESTUDIANTE" ||
              destino === "ASESOR" ||
              destino === "CONCILIADOR"
                ? REQUIRED
                : false,
          }}
        />

        <FormInput
          name="telefono"
          label="Teléfono"
          register={register}
          errors={errors}
          rules={{ required: REQUIRED }}
        />

        <FormInput
          name="usuario"
          label="Usuario"
          register={register}
          errors={errors}
          rules={{ required: REQUIRED }}
        />

        <FormInput
          name="codigo"
          label="Código"
          register={register}
          errors={errors}
          rules={{ required: REQUIRED }}
        />

        {sedes.length > 0 && (
          <FormSelect
            name="sedeId"
            label="Sede"
            options={sedes}
            register={register}
            errors={errors}
            rules={{
              required:
                destino === "ESTUDIANTE" || destino === "ASESOR"
                  ? REQUIRED
                  : false,
              valueAsNumber: true,
            }}
          />
        )}
      </>
    );
  }

  function renderCamposDestino() {
    switch (destino) {
      case "ESTUDIANTE":
        return (
          <>
            <FormSelect
              name="asesorId"
              label="Asesor"
              options={asesores}
              register={register}
              errors={errors}
              rules={{ required: REQUIRED, valueAsNumber: true }}
            />

            <FormCheckbox
              name="conciliacion"
              label="¿Conciliación?"
              register={register}
            />
          </>
        );

      case "ASESOR":
        return (
          <FormSelect
            name="areaId"
            label="Área"
            options={areas}
            register={register}
            errors={errors}
            rules={{ required: REQUIRED, valueAsNumber: true }}
          />
        );

      case "ADMINISTRATIVO":
        return (
          <FormCheckbox
            name="directora"
            label="¿Directora?"
            register={register}
          />
        );

      case "CONCILIADOR":
        return (
          <FormSelect
            name="tipoConciliador"
            label="Tipo de conciliador"
            options={[
              { value: "INTERNO", label: "Interno" },
              { value: "EXTERNO", label: "Externo" },
            ]}
            register={register}
            errors={errors}
            rules={{ required: REQUIRED }}
          />
        );

      case "MONITOR":
      default:
        return null;
    }
  }

  if (loading) {
    return <div className="text-center mt-10">Cargando usuarios...</div>;
  }

  return (
    <div className="space-y-6 p-6 bg-card rounded-xl border">
      <div>
        <h2 className="text-2xl font-bold">Cambiar rol y perfil de usuario</h2>
        <p className="text-muted-foreground">
          Selecciona un usuario, elige el nuevo perfil y guarda el cambio.
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <input
          type="hidden"
          {...register("usuarioSistemaId", { required: REQUIRED })}
        />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">Usuario</label>

            <Button
              type="button"
              variant="outline"
              onClick={() => setModalAbierto(true)}
              className="w-full justify-start"
            >
              {usuarioSeleccionado
                ? `${usuarioSeleccionado.username} - ${
                    usuarioSeleccionado.rolNombre || "Sin rol"
                  } - ${usuarioSeleccionado.tipoPerfil || "Sin perfil"}`
                : "Buscar usuario"}
            </Button>

            {errors?.usuarioSistemaId && (
              <p className="text-xs text-red-500">
                {errors.usuarioSistemaId.message}
              </p>
            )}
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">Nuevo perfil / rol</label>

            <select
              {...register("destino", { required: REQUIRED })}
              disabled={!usuarioSeleccionado}
              className={`flex h-9 w-full rounded-lg border bg-background px-3 py-2 text-sm ${
                errors?.destino ? "border-red-500" : ""
              }`}
            >
              <option value="">Seleccione un perfil destino</option>

              {perfilesDisponibles.map((perfil) => (
                <option key={perfil.value} value={perfil.value}>
                  {perfil.label}
                </option>
              ))}
            </select>

            {errors?.destino && (
              <p className="text-xs text-red-500">
                {errors.destino.message}
              </p>
            )}
          </div>
        </div>

        {usuarioSeleccionado && (
          <div className="rounded-lg border bg-muted/30 p-4 text-sm">
            <p>
              <span className="font-medium">Usuario actual:</span>{" "}
              {usuarioSeleccionado.username}
            </p>
            <p>
              <span className="font-medium">Rol actual:</span>{" "}
              {usuarioSeleccionado.rolNombre || "Sin rol"}
            </p>
            <p>
              <span className="font-medium">Perfil actual:</span>{" "}
              {usuarioSeleccionado.tipoPerfil || "Sin perfil"}
            </p>
          </div>
        )}

        {perfilPrevioInfo && (
          <div className="rounded-lg border bg-background p-4 text-sm">
            {perfilPrevioInfo.existe ? (
              <p>
                Se encontró un perfil previo{" "}
                {perfilPrevioInfo.activo ? "activo" : "inactivo"} para este destino. Los datos fueron precargados.
              </p>
            ) : (
              <p>
                Este usuario no tiene datos previos para el perfil destino. Se mantienen los datos del perfil actual.
              </p>
            )}
          </div>
        )}

        {destino && (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {renderCamposComunes()}
              {renderCamposDestino()}
            </div>

            <div className="flex justify-end gap-3">
              <Button
                type="button"
                variant="outline"
                disabled={guardando}
                onClick={limpiarSeleccion}
              >
                Limpiar
              </Button>

              <Button type="submit" disabled={guardando}>
                {guardando ? "Guardando..." : "Cambiar rol y perfil"}
              </Button>
            </div>
          </>
        )}
      </form>

      {modalAbierto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/70 px-4 backdrop-blur-sm">
          <div className="w-full max-w-3xl rounded-xl border bg-card shadow-lg">
            <div className="flex items-center justify-between border-b p-4">
              <h3 className="text-lg font-semibold">Seleccionar usuario activo</h3>

              <Button
                type="button"
                variant="outline"
                onClick={() => setModalAbierto(false)}
              >
                Cerrar
              </Button>
            </div>

            <div className="space-y-4 p-4">
              <input
                value={busquedaModal}
                onChange={(event) => setBusquedaModal(event.target.value)}
                placeholder="Buscar por usuario, rol o perfil..."
                className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />

              <div className="max-h-[420px] overflow-auto rounded-lg border">
                <table className="w-full text-sm">
                  <thead className="bg-muted">
                    <tr>
                      <th className="px-4 py-3 text-left font-medium">ID</th>
                      <th className="px-4 py-3 text-left font-medium">Usuario</th>
                      <th className="px-4 py-3 text-left font-medium">Rol</th>
                      <th className="px-4 py-3 text-left font-medium">Perfil</th>
                      <th className="px-4 py-3 text-right font-medium">Acción</th>
                    </tr>
                  </thead>

                  <tbody>
                    {usuariosFiltrados.length === 0 ? (
                      <tr>
                        <td
                          colSpan={5}
                          className="px-4 py-8 text-center text-muted-foreground"
                        >
                          No hay usuarios activos para mostrar.
                        </td>
                      </tr>
                    ) : (
                      usuariosFiltrados.map((usuario) => (
                        <tr key={usuario.id} className="border-t hover:bg-muted/50">
                          <td className="px-4 py-3">{usuario.id}</td>
                          <td className="px-4 py-3">{usuario.username}</td>
                          <td className="px-4 py-3">
                            {usuario.rolNombre || "Sin rol"}
                          </td>
                          <td className="px-4 py-3">
                            {usuario.tipoPerfil || "Sin perfil"}
                          </td>
                          <td className="px-4 py-3 text-right">
                            <Button
                              type="button"
                              size="sm"
                              onClick={() => seleccionarUsuario(usuario)}
                            >
                              Seleccionar
                            </Button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}