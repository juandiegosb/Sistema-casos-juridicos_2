"use client";

import React, { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { API_URL_BASE } from "@/lib/config";
import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { FormCheckbox } from "./parts/FormCheckbox";
import { useForm } from "react-hook-form";

const TIPOS_PERFIL = [
  {
    value: "ADMINISTRATIVO",
    label: "Administrativo",
    endpoint: "administrativo",
    rolId: 1,
  },
  {
    value: "ASESOR",
    label: "Asesor",
    endpoint: "asesor",
    rolId: 2,
  },
  {
    value: "ESTUDIANTE",
    label: "Estudiante",
    endpoint: "estudiante",
    rolId: 3,
  },
  {
    value: "MONITOR",
    label: "Monitor",
    endpoint: "monitor",
    rolId: 4,
  },
  {
    value: "CONCILIADOR",
    label: "Conciliador",
    endpoint: "conciliador",
    rolId: 5,
  },
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

export function CambiarRolUsuarioForm() {
  const [usuarios, setUsuarios] = useState([]);
  const [tiposDocumento, setTiposDocumento] = useState([]);
  const [sedes, setSedes] = useState([]);
  const [asesores, setAsesores] = useState([]);
  const [areas, setAreas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [perfilPrevioInfo, setPerfilPrevioInfo] = useState(null);

  const {
    register,
    handleSubmit,
    watch,
    reset,
    setValue,
    formState: { errors },
  } = useForm({
    defaultValues: VALORES_INICIALES,
  });

  const REQUIRED = "Campo obligatorio";

  const usuarioSistemaId = watch("usuarioSistemaId");
  const destino = watch("destino");

  const usuarioSeleccionado = useMemo(() => {
    return usuarios.find((usuario) => String(usuario.id) === String(usuarioSistemaId));
  }, [usuarios, usuarioSistemaId]);

  const perfilDestino = useMemo(() => {
    return TIPOS_PERFIL.find((perfil) => perfil.value === destino);
  }, [destino]);

  const perfilesDisponibles = useMemo(() => {
    if (!usuarioSeleccionado?.tipoPerfil) {
      return TIPOS_PERFIL;
    }

    return TIPOS_PERFIL.filter(
      (perfil) => perfil.value !== usuarioSeleccionado.tipoPerfil
    );
  }, [usuarioSeleccionado]);

  useEffect(() => {
    cargarDatosIniciales();
  }, []);

  useEffect(() => {
    if (!usuarioSeleccionado) {
      return;
    }

    setValue("destino", "");
    setPerfilPrevioInfo(null);
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

    if (!text) {
      return null;
    }

    try {
      return JSON.parse(text);
    } catch {
      return { mensaje: text };
    }
  }

  async function cargarDatosIniciales() {
    try {
      setLoading(true);

      const [usuariosRes, tiposRes, sedesRes, asesoresRes, areasRes] =
        await Promise.all([
          fetch(`${API_URL_BASE}/usuarios-sistema`, {
            credentials: "include",
          }),
          fetch(`${API_URL_BASE}/tipos-documento`, {
            credentials: "include",
          }),
          fetch(`${API_URL_BASE}/sedes`, {
            credentials: "include",
          }),
          fetch(`${API_URL_BASE}/asesores/activos`, {
            credentials: "include",
          }),
          fetch(`${API_URL_BASE}/areas`, {
            credentials: "include",
          }),
        ]);

      if (usuariosRes.status === 401) {
        toast.error("La sesión expiró");
        return;
      }

      if (usuariosRes.status === 403) {
        toast.error("No tienes permisos para gestionar usuarios");
        return;
      }

      const usuariosData = usuariosRes.ok ? await usuariosRes.json() : [];
      const tiposData = tiposRes.ok ? await tiposRes.json() : [];
      const sedesData = sedesRes.ok ? await sedesRes.json() : [];
      const asesoresData = asesoresRes.ok ? await asesoresRes.json() : [];
      const areasData = areasRes.ok ? await areasRes.json() : [];

      setUsuarios(Array.isArray(usuariosData) ? usuariosData : []);

      setTiposDocumento(
        Array.isArray(tiposData)
          ? tiposData.map((tipo) => ({
              value: tipo.id,
              label: tipo.displayName || tipo.nombre || tipo.descripcion,
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
        Array.isArray(asesoresData)
          ? asesoresData.map((asesor) => ({
              value: asesor.id,
              label: asesor.documento
                ? `${asesor.nombre} - ${asesor.documento}`
                : asesor.nombre,
            }))
          : []
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

  async function cargarPerfilPrevio(usuarioId, perfil) {
    try {
      setPerfilPrevioInfo(null);

      const response = await fetch(
        `${API_URL_BASE}/usuarios-sistema/${usuarioId}/perfil/${perfil.endpoint}`,
        {
          method: "GET",
          credentials: "include",
        }
      );

      if (response.status === 404) {
        limpiarCamposPerfil();
        setPerfilPrevioInfo({
          existe: false,
          activo: false,
          tipoPerfil: perfil.value,
          datos: null,
        });
        return;
      }

      if (!response.ok) {
        limpiarCamposPerfil();
        return;
      }

      const data = await response.json();
      setPerfilPrevioInfo(data);

      if (data?.datos) {
        precargarDatosPerfil(data.datos);
      } else {
        limpiarCamposPerfil();
      }
    } catch (error) {
      console.error(error);
      limpiarCamposPerfil();
    }
  }

  function limpiarCamposPerfil() {
    setValue("motivo", "");
    setValue("nombre", "");
    setValue("tipoDocumentoId", "");
    setValue("documento", "");
    setValue("telefono", "");
    setValue("usuario", "");
    setValue("codigo", "");
    setValue("sedeId", "");
    setValue("asesorId", "");
    setValue("areaId", "");
    setValue("conciliacion", false);
    setValue("directora", false);
    setValue("tipoConciliador", "");
  }

  function precargarDatosPerfil(datos) {
    setValue("nombre", datos.nombre || "");
    setValue("tipoDocumentoId", datos.tipoDocumentoId || "");
    setValue("documento", datos.documento || "");
    setValue("telefono", datos.telefono || "");
    setValue("usuario", datos.usuario || "");
    setValue("codigo", datos.codigo || "");
    setValue("sedeId", datos.sedeId || "");
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

    if (data.tipoDocumentoId) {
      payload.tipoDocumentoId = Number(data.tipoDocumentoId);
    }

    if (data.documento) {
      payload.documento = data.documento;
    }

    if (data.sedeId) {
      payload.sedeId = Number(data.sedeId);
    }

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
    if (!usuarioSeleccionado) {
      toast.error("Selecciona un usuario");
      return;
    }

    if (!perfilDestino) {
      toast.error("Selecciona el perfil destino");
      return;
    }

    try {
      setGuardando(true);

      const payload = construirPayload(data);

      const response = await fetch(
        `${API_URL_BASE}/usuarios-sistema/${usuarioSeleccionado.id}/perfil/${perfilDestino.endpoint}`,
        {
          method: "PATCH",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(payload),
        }
      );

      const result = await leerRespuesta(response);

      if (!response.ok) {
        throw new Error(
          result?.mensaje ||
            result?.message ||
            "No se pudo cambiar el perfil del usuario"
        );
      }

      toast.success("Perfil y rol actualizados correctamente");

      setUsuarios((current) =>
        current.map((usuario) =>
          usuario.id === result.id ? result : usuario
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
          Selecciona un usuario, elige el nuevo perfil y guarda el cambio. El
          rol se asigna automáticamente según el perfil destino.
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">Usuario</label>
            <select
              {...register("usuarioSistemaId", { required: REQUIRED })}
              className={`flex h-9 w-full rounded-lg border bg-background px-3 py-2 text-sm ${
                errors?.usuarioSistemaId ? "border-red-500" : ""
              }`}
            >
              <option value="">Seleccione un usuario</option>

              {usuarios.map((usuario) => (
                <option key={usuario.id} value={usuario.id}>
                  {usuario.username} - {usuario.rolNombre || "Sin rol"} -{" "}
                  {usuario.tipoPerfil || "Sin perfil"}
                </option>
              ))}
            </select>

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

        {perfilDestino && (
          <div className="rounded-lg border bg-muted/30 p-4 text-sm">
            <p>
              <span className="font-medium">Rol destino:</span>{" "}
              {perfilDestino.label}
            </p>
            <p>
              <span className="font-medium">rolId enviado:</span>{" "}
              {perfilDestino.rolId}
            </p>
          </div>
        )}

        {perfilPrevioInfo && (
          <div className="rounded-lg border bg-background p-4 text-sm">
            {perfilPrevioInfo.existe ? (
              <p>
                Se encontró un perfil previo{" "}
                {perfilPrevioInfo.activo ? "activo" : "inactivo"} para este
                destino. Los datos fueron precargados.
              </p>
            ) : (
              <p>
                Este usuario no tiene datos previos para el perfil destino. Debes
                completar el formulario.
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
                onClick={() => {
                  reset(VALORES_INICIALES);
                  setPerfilPrevioInfo(null);
                }}
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
    </div>
  );
}