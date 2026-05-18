"use client";

import React, { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useApiForm } from "@/hooks/useApiForm";
import { FormInput } from "./parts/FormInput";
import { FormCheckbox } from "./parts/FormCheckbox";
import { Button } from "@/components/ui/button";
import { API_URL_BASE } from "@/lib/config";

export function PersonaForm({ onSubmit, initialValues = {} }) {
  const router = useRouter();

  const REQUIRED_MESSAGE = "El campo es obligatorio";
  const MAX_ESTRATO = 7;
  const MAX_PERSONAS_A_CARGO = 10;

  const defaultFormValues = {
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
    sabeLeerEscribir: true,
    discapacidad: "",
    caracterizacionPcd: "",
    necesitaAjustePcd: false,
    departamento: "",
    municipio: "",
    barrio: "",
    direccion: "",
    comuna: "",
    localidad: "",
    estrato: "",
    tipoVivienda: "",
    zona: "",
    tenencia: "",
    numeroPersonasACargo: "",
    ingresosAdicionales: false,
    energiaElectrica: false,
    acueducto: false,
    alcantarillado: false,
    ocupacion: "",
    empresa: "",
    salario: "",
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
    ...initialValues,
  };

  const pasos = [
    "Identificación",
    "Identidad",
    "Contacto",
    "Vivienda",
    "Economía",
    "Acudiente",
    "Servicio",
  ];

  const fallbackTipoDocumentoOptions = [
    { value: "CC", label: "Cédula de Ciudadanía" },
    { value: "TI", label: "Tarjeta de Identidad" },
    { value: "CE", label: "Cédula de Extranjería" },
    { value: "PA", label: "Pasaporte" },
  ];

  const tipoUsuarioOptions = [
    { value: "Víctima", label: "Víctima" },
    { value: "Usuario General", label: "Usuario General" },
  ];

  const pronombreOptions = [
    { value: "Él", label: "Él" },
    { value: "Ella", label: "Ella" },
    { value: "Elle", label: "Elle" },
    { value: "Otro", label: "Otro" },
  ];

  const sexoOptions = [
    { value: "Hombre", label: "Hombre" },
    { value: "Mujer", label: "Mujer" },
    { value: "Intersexual", label: "Intersexual" },
  ];

  const generoOptions = [
    { value: "Masculino", label: "Masculino" },
    { value: "Femenino", label: "Femenino" },
    { value: "No binario", label: "No binario" },
    { value: "Transgénero", label: "Transgénero" },
    { value: "Otro", label: "Otro" },
  ];

  const orientacionSexualOptions = [
    { value: "Heterosexual", label: "Heterosexual" },
    { value: "Homosexual", label: "Homosexual" },
    { value: "Bisexual", label: "Bisexual" },
    { value: "Pansexual", label: "Pansexual" },
    { value: "Asexual", label: "Asexual" },
    { value: "Otro", label: "Otro" },
  ];

  const estadoCivilOptions = [
    { value: "Soltero/a", label: "Soltero/a" },
    { value: "Casado/a", label: "Casado/a" },
    { value: "Unión libre", label: "Unión libre" },
    { value: "Divorciado/a", label: "Divorciado/a" },
    { value: "Viudo/a", label: "Viudo/a" },
  ];

  const escolaridadOptions = [
    { value: "Ninguna", label: "Ninguna" },
    { value: "Primaria", label: "Primaria" },
    { value: "Secundaria", label: "Secundaria" },
    { value: "Técnico", label: "Técnico" },
    { value: "Tecnólogo", label: "Tecnólogo" },
    { value: "Universitario", label: "Universitario" },
    { value: "Postgrado", label: "Postgrado" },
  ];

  const zonaOptions = [
    { value: "Urbana", label: "Urbana" },
    { value: "Rural", label: "Rural" },
  ];

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: defaultFormValues,
  });

  const { submit, isSubmitting } = useApiForm({
    endpoint: `${API_URL_BASE}/personas`,
  });

  const [checking, setChecking] = useState(true);
  const [pasoActual, setPasoActual] = useState(0);
  const [tipoDocumentoOptions, setTipoDocumentoOptions] = useState(
    fallbackTipoDocumentoOptions
  );
  const [catalogoMensaje, setCatalogoMensaje] = useState("");

  const formValues = watch();

  const esMenorEdad = useMemo(() => {
    if (!formValues.fechaNacimiento) return false;

    const nacimiento = new Date(formValues.fechaNacimiento);
    const hoy = new Date();

    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const mes = hoy.getMonth() - nacimiento.getMonth();

    if (mes < 0 || (mes === 0 && hoy.getDate() < nacimiento.getDate())) {
      edad -= 1;
    }

    return edad < 18;
  }, [formValues.fechaNacimiento]);

  useEffect(() => {
    verificarSesion();
    cargarCatalogos();
  }, []);

  async function verificarSesion() {
    try {
      const res = await fetch(`${API_URL_BASE}/auth/me`, {
        method: "GET",
        credentials: "include",
      });

      if (res.status === 401) {
        router.push("/");
        return;
      }

      if (!res.ok) {
        router.push("/");
        return;
      }
    } catch {
      router.push("/");
    } finally {
      setChecking(false);
    }
  }

  async function cargarCatalogos() {
    try {
      const res = await fetch(`${API_URL_BASE}/tipos-documento/activos`, {
        method: "GET",
        credentials: "include",
      });

      if (res.status === 403) {
        setCatalogoMensaje(
          "No se pudieron cargar los tipos de documento desde catálogos. Se usan opciones básicas."
        );
        setTipoDocumentoOptions(fallbackTipoDocumentoOptions);
        return;
      }

      if (!res.ok) {
        setTipoDocumentoOptions(fallbackTipoDocumentoOptions);
        return;
      }

      const data = await res.json();

      const opciones = Array.isArray(data)
        ? data
            .map((item) => ({
              value:
                item.codigo ||
                item.abreviatura ||
                item.nombre ||
                item.descripcion ||
                "",
              label:
                item.nombre ||
                item.descripcion ||
                item.codigo ||
                item.abreviatura ||
                "",
            }))
            .filter((item) => item.value && item.label)
        : [];

      setTipoDocumentoOptions(
        opciones.length > 0 ? opciones : fallbackTipoDocumentoOptions
      );
    } catch {
      setTipoDocumentoOptions(fallbackTipoDocumentoOptions);
    }
  }

  async function handleSubmitForm(data) {
    const payload = {
      ...data,
      estrato: Number(data.estrato || 0),
      numeroPersonasACargo: Number(data.numeroPersonasACargo || 0),
      salario: Number(data.salario || 0),
    };

    await submit(payload);

    if (typeof onSubmit === "function") {
      onSubmit(payload);
    }
  }

  function irAnterior() {
    setPasoActual((actual) => Math.max(actual - 1, 0));
  }

  function irSiguiente() {
    setPasoActual((actual) => Math.min(actual + 1, pasos.length - 1));
  }

  function limpiarFormulario() {
    reset(defaultFormValues);
    setPasoActual(0);
  }

  if (checking) {
    return <div className="text-center mt-10">Cargando...</div>;
  }

  return (
    <div className="space-y-6 p-6 bg-card rounded-xl shadow-sm border border-border">
      <div>
        <h3 className="text-xl font-bold">Registro de persona</h3>
        <p className="text-sm text-muted-foreground">
          Completa la información por grupos. Puedes cambiar de sección en cualquier momento.
        </p>
      </div>

      {catalogoMensaje && (
        <div className="rounded-lg border border-yellow-500/30 bg-yellow-500/10 px-4 py-3 text-sm text-yellow-700 dark:text-yellow-200">
          {catalogoMensaje}
        </div>
      )}

      <div className="grid grid-cols-2 gap-2 md:grid-cols-4 lg:grid-cols-8">
        {pasos.map((paso, index) => (
          <button
            key={paso}
            type="button"
            onClick={() => setPasoActual(index)}
            className={`rounded-lg border px-3 py-2 text-sm font-medium transition ${
              pasoActual === index
                ? "border-primary bg-primary text-primary-foreground"
                : "bg-background hover:bg-muted"
            }`}
          >
            {paso}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit(handleSubmitForm)} className="space-y-6">
        <div className="rounded-xl border bg-background p-5">
          {pasoActual === 0 && (
            <Seccion
              titulo="Identificación"
              descripcion="Datos principales para reconocer a la persona."
            >
              <FormSelectField
                name="tipoUsuario"
                label="Tipo de Usuario"
                options={tipoUsuarioOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormSelectField
                name="tipoDocumento"
                label="Tipo de Documento"
                options={tipoDocumentoOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="numeroDocumento"
                label="Número de Documento"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="fechaExpedicion"
                label="Fecha de Expedición"
                type="date"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="ciudadExpedicion"
                label="Ciudad de Expedición"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="nombres"
                label="Nombres"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="apellidos"
                label="Apellidos"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="fechaNacimiento"
                label="Fecha de Nacimiento"
                type="date"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />
            </Seccion>
          )}

          {pasoActual === 1 && (
            <Seccion
              titulo="Identidad y caracterización"
              descripcion="Información de identidad, características personales y condiciones diferenciales."
            >
              <FormInput
                name="nombreIdentitario"
                label="Nombre Identitario"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormSelectField
                name="pronombre"
                label="Pronombre"
                options={pronombreOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormSelectField
                name="sexo"
                label="Sexo"
                options={sexoOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormSelectField
                name="genero"
                label="Género"
                options={generoOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormSelectField
                name="orientacionSexual"
                label="Orientación Sexual"
                options={orientacionSexualOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="nacionalidad"
                label="Nacionalidad"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormSelectField
                name="estadoCivil"
                label="Estado Civil"
                options={estadoCivilOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormSelectField
                name="escolaridad"
                label="Escolaridad"
                options={escolaridadOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="grupoEtnico"
                label="Grupo Étnico"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="condicionActual"
                label="Condición Actual"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="discapacidad"
                label="Discapacidad"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="caracterizacionPcd"
                label="Caracterización PCD"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <div className="rounded-lg border bg-muted/20 p-3">
                <FormCheckbox
                  name="sabeLeerEscribir"
                  label="¿Sabe leer y escribir?"
                  register={register}
                  errors={errors}
                />
              </div>

              <div className="rounded-lg border bg-muted/20 p-3">
                <FormCheckbox
                  name="necesitaAjustePcd"
                  label="¿Necesita ajuste razonable (PCD)?"
                  register={register}
                  errors={errors}
                />
              </div>
            </Seccion>
          )}

          {pasoActual === 2 && (
            <Seccion
              titulo="Contacto"
              descripcion="Datos de comunicación. Debe existir al menos teléfono o correo."
            >
              <FormInput
                name="telefono"
                label="Teléfono"
                register={register}
                errors={errors}
              />

              <FormInput
                name="correo"
                label="Correo Electrónico"
                type="email"
                register={register}
                errors={errors}
              />
            </Seccion>
          )}

          {pasoActual === 3 && (
            <Seccion
              titulo="Información de vivienda"
              descripcion="Información de residencia y condiciones de vivienda."
            >
              <FormInput
                name="departamento"
                label="Departamento"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="municipio"
                label="Municipio"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="barrio"
                label="Barrio"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="direccion"
                label="Dirección"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="comuna"
                label="Comuna"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="localidad"
                label="Localidad"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="estrato"
                label="Estrato"
                type="number"
                max={MAX_ESTRATO}
                min={0}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="tipoVivienda"
                label="Tipo de Vivienda"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormSelectField
                name="zona"
                label="Zona"
                options={zonaOptions}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="tenencia"
                label="Tenencia de la Vivienda"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="numeroPersonasACargo"
                label="Número de Personas a Cargo"
                type="number"
                min={0}
                max={MAX_PERSONAS_A_CARGO}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <div className="rounded-lg border bg-muted/20 p-3">
                <FormCheckbox
                  name="ingresosAdicionales"
                  label="¿Recibe ingresos adicionales?"
                  register={register}
                  errors={errors}
                />
              </div>

              <div className="rounded-lg border bg-muted/20 p-3">
                <FormCheckbox
                  name="energiaElectrica"
                  label="¿Cuenta con energía eléctrica?"
                  register={register}
                  errors={errors}
                />
              </div>

              <div className="rounded-lg border bg-muted/20 p-3">
                <FormCheckbox
                  name="acueducto"
                  label="¿Cuenta con acueducto?"
                  register={register}
                  errors={errors}
                />
              </div>

              <div className="rounded-lg border bg-muted/20 p-3">
                <FormCheckbox
                  name="alcantarillado"
                  label="¿Cuenta con alcantarillado?"
                  register={register}
                  errors={errors}
                />
              </div>
            </Seccion>
          )}

          {pasoActual === 4 && (
            <Seccion
              titulo="Aspectos económicos"
              descripcion="Datos laborales y económicos de la persona."
            >
              <FormInput
                name="ocupacion"
                label="Ocupación"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="empresa"
                label="Empresa donde labora"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="salario"
                label="Salario"
                type="number"
                step={10000}
                min={0}
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="cargo"
                label="Cargo"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="direccionEmpresa"
                label="Dirección de la Empresa"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="telefonoEmpresa"
                label="Teléfono de la Empresa"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />
            </Seccion>
          )}

          {pasoActual === 5 && (
            <Seccion
              titulo="Datos del acudiente"
              descripcion={
                esMenorEdad
                  ? "La persona es menor de edad. Los datos del acudiente son necesarios."
                  : "Estos datos aplican principalmente cuando la persona es menor de edad."
              }
            >
              <FormInput
                name="nombreCompletoAcudiente"
                label="Nombre Completo"
                register={register}
                errors={errors}
              />

              <FormInput
                name="relacionAcudiente"
                label="Relación / Parentesco"
                register={register}
                errors={errors}
              />

              <FormInput
                name="telefonoAcudiente"
                label="Teléfono"
                register={register}
                errors={errors}
              />

              <FormInput
                name="correoAcudiente"
                label="Correo Electrónico"
                type="email"
                register={register}
                errors={errors}
              />

              <FormInput
                name="direccionAcudiente"
                label="Dirección"
                register={register}
                errors={errors}
              />
            </Seccion>
          )}

          {pasoActual === 6 && (
            <Seccion
              titulo="Información del servicio"
              descripcion="Información sobre cómo llegó la persona al servicio."
            >
              <FormInput
                name="comoSeEntero"
                label="¿Cómo se enteró de nuestros servicios?"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />

              <FormInput
                name="relacionConUniversidad"
                label="Relación con la Universidad"
                register={register}
                errors={errors}
                rules={{ required: REQUIRED_MESSAGE }}
              />
            </Seccion>
          )}

        </div>

        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div className="flex gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={irAnterior}
              disabled={pasoActual === 0 || isSubmitting}
            >
              <ChevronLeft className="mr-1 size-4" />
              Anterior
            </Button>

            <Button
              type="button"
              variant="outline"
              onClick={irSiguiente}
              disabled={pasoActual === pasos.length - 1 || isSubmitting}
            >
              Siguiente
              <ChevronRight className="ml-1 size-4" />
            </Button>
          </div>

          <div className="flex justify-end gap-3">
            <Button
              type="button"
              variant="outline"
              onClick={limpiarFormulario}
              disabled={isSubmitting}
            >
              Limpiar formulario
            </Button>

            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Guardando..." : "Guardar Persona"}
            </Button>
          </div>
        </div>
      </form>
    </div>
  );
}

function Seccion({ titulo, descripcion, children }) {
  return (
    <section className="space-y-4">
      <div>
        <h3 className="text-lg font-semibold border-b pb-2">{titulo}</h3>
        {descripcion && (
          <p className="mt-2 text-sm text-muted-foreground">{descripcion}</p>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {children}
      </div>
    </section>
  );
}

function FormSelectField({ name, label, options, register, errors, rules }) {
  return (
    <div className="flex flex-col gap-1.5 w-full">
      <label htmlFor={name} className="text-sm font-medium leading-none">
        {label}
      </label>

      <select
        id={name}
        defaultValue=""
        {...register(name, rules)}
        className={`flex h-9 w-full rounded-lg border bg-background px-2.5 py-1 text-sm ${
          errors?.[name] ? "border-red-500" : ""
        }`}
      >
        <option value="">Seleccione una opción</option>

        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>

      {errors?.[name] && (
        <p className="text-xs text-red-500">{errors[name]?.message}</p>
      )}
    </div>
  );
}
