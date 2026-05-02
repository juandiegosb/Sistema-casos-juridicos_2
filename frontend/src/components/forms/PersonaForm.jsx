import React from "react";
import { useForm } from "react-hook-form";
import { useApiForm } from "@/hooks/useApiForm";
import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { FormCheckbox } from "./parts/FormCheckbox";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export function PersonaForm({ onSubmit, initialValues = {} }) {
  const defaultFormValues = {
    sabeLeerEscribir: true,
    necesitaAjustePcd: false,
    ingresosAdicionales: false,
    energiaElectrica: false,
    acueducto: false,
    alcantarillado: false,
    ...initialValues,
  };
  const router = useRouter();
  const [checking, setChecking] = useState(true);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ defaultValues: defaultFormValues });

  useEffect(() => {
  async function verificar() {
    try {
      const res = await fetch("http://localhost:8080/api/auth/me", {
        method: "GET",
        credentials: "include",
      });

      if (res.status === 401) {
        router.push("/");
        return;
      }

      const user = await res.json();

      if (!user.permisos?.includes("Gestionar personas")) {
        router.push("/inicio");
        return;
      }

    } catch {
      router.push("/");
    } finally {
      setChecking(false);
    }
  }

  verificar();
}, [router]);

  const API_URL_BASE = "http://localhost:8080/api";

  const { submit, isSubmitting } = useApiForm({ endpoint: `${API_URL_BASE}/personas` });

  const MAX_ESTRATO = 7;
  const MAX_PERSONAS_A_CARGO = 10;
  const REQUIRED_MESSAGE = "El campo es obligatorio";

  const tipoDocumentoOptions = [
    { value: "CC", label: "Cédula de Ciudadanía" },
    { value: "TI", label: "Tarjeta de Identidad" },
    { value: "CE", label: "Cédula de Extranjería" },
    { value: "PA", label: "Pasaporte" },
  ];

  const pronombreOptions = [
    { value: "el", label: "Él" },
    { value: "ella", label: "Ella" },
    { value: "elle", label: "Elle" },
    { value: "otro", label: "Otro" },
  ];

  const sexoOptions = [
    { value: "hombre", label: "Hombre" },
    { value: "mujer", label: "Mujer" },
    { value: "intersexual", label: "Intersexual" },
  ];

  const tipoUsuarioOptions = [
    { value: "victima", label: "Víctima" },
    { value: "usuario_general", label: "Usuario General" },
  ];

  const generoOptions = [
    { value: "masculino", label: "Masculino" },
    { value: "femenino", label: "Femenino" },
    { value: "no_binario", label: "No Binario" },
    { value: "transgenero", label: "Transgénero" },
    { value: "otro", label: "Otro" },
  ];

  const orientacionSexualOptions = [
    { value: "heterosexual", label: "Heterosexual" },
    { value: "homosexual", label: "Homosexual" },
    { value: "bisexual", label: "Bisexual" },
    { value: "pansexual", label: "Pansexual" },
    { value: "asexual", label: "Asexual" },
    { value: "otro", label: "Otro" },
  ];

  const estadoCivilOptions = [
    { value: "soltero", label: "Soltero/a" },
    { value: "casado", label: "Casado/a" },
    { value: "union_libre", label: "Unión Libre" },
    { value: "divorciado", label: "Divorciado/a" },
    { value: "viudo", label: "Viudo/a" },
  ];

  const escolaridadOptions = [
    { value: "ninguna", label: "Ninguna" },
    { value: "primaria", label: "Primaria" },
    { value: "secundaria", label: "Secundaria" },
    { value: "tecnico", label: "Técnico" },
    { value: "tecnologo", label: "Tecnólogo" },
    { value: "universitario", label: "Universitario" },
    { value: "postgrado", label: "Postgrado" },
  ];

  const zonaOptions = [
    { value: "urbana", label: "Urbana" },
    { value: "rural", label: "Rural" },
  ];

  const handleSubmitForm = async (data) => {
    await submit(data);
  };

  if (checking) {
  return <div className="text-center mt-10">Cargando...</div>;
  }

  return (
    <div>
      <div className="space-y-8 p-6 bg-card rounded-xl shadow-sm border border-border">

        {/* Información Básica */}
        <section className="space-y-4">
          <h3 className="text-lg font-semibold border-b pb-2">
            Información Básica
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <FormSelect
              name="tipoUsuario"
              label="Tipo de Usuario"
              options={tipoUsuarioOptions}
              register={register}
              errors={errors}
              rules={{ required: REQUIRED_MESSAGE }}
            />
            <FormSelect
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
              name="nombreIdentitario"
              label="Nombre Identitario"
              register={register}
              errors={errors}
              rules={{ required: REQUIRED_MESSAGE }}
            />
            <FormSelect
              name="pronombre"
              label="Pronombre"
              options={pronombreOptions}
              register={register}
              errors={errors}
              rules={{ required: REQUIRED_MESSAGE }}
            />
            <FormSelect
              name="sexo"
              label="Sexo"
              options={sexoOptions}
              register={register}
              errors={errors}
              rules={{ required: REQUIRED_MESSAGE }}
            />
            <FormSelect
              name="genero"
              label="Género"
              options={generoOptions}
              register={register}
              errors={errors}
              rules={{ required: REQUIRED_MESSAGE }}
            />
            <FormSelect
              name="orientacionSexual"
              label="Orientación Sexual"
              options={orientacionSexualOptions}
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
            <FormInput
              name="nacionalidad"
              label="Nacionalidad"
              register={register}
              errors={errors}
              rules={{ required: REQUIRED_MESSAGE }}
            />
            <FormSelect
              name="estadoCivil"
              label="Estado Civil"
              options={estadoCivilOptions}
              register={register}
              errors={errors}
              rules={{ required: REQUIRED_MESSAGE }}
            />
            <FormSelect
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
          </div>

          <div className="flex gap-6 mt-4 p-4 rounded-lg bg-muted/20 dark:bg-muted/30">
            <FormCheckbox
              name="sabeLeerEscribir"
              label="¿Sabe leer y escribir?"
              register={register}
              errors={errors}
            />
            <FormCheckbox
              name="necesitaAjustePcd"
              label="¿Necesita ajuste razonable (PCD)?"
              register={register}
              errors={errors}
            />
          </div>
        </section>

        <Separator />

        {/* Información de Vivienda */}
        <section className="space-y-4">
          <h3 className="text-lg font-semibold border-b pb-2">
            Información de Vivienda
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
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
            <FormSelect
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
          </div>

          <div className="flex flex-wrap gap-6 mt-4 p-4 rounded-lg bg-muted/20 dark:bg-muted/30">
            <FormCheckbox
              name="ingresosAdicionales"
              label="¿Recibe ingresos adicionales?"
              register={register}
              errors={errors}
            />
            <FormCheckbox
              name="energiaElectrica"
              label="¿Cuenta con energía eléctrica?"
              register={register}
              errors={errors}
            />
            <FormCheckbox
              name="acueducto"
              label="¿Cuenta con acueducto?"
              register={register}
              errors={errors}
            />
            <FormCheckbox
              name="alcantarillado"
              label="¿Cuenta con alcantarillado?"
              register={register}
              errors={errors}
            />
          </div>
        </section>

        <Separator />

        {/* Aspectos Económicos */}
        <section className="space-y-4">
          <h3 className="text-lg font-semibold border-b pb-2">
            Aspectos Económicos
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
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
          </div>
        </section>

        <Separator />

        {/* Datos del Acudiente */}
        <section className="space-y-4">
          <h3 className="text-lg font-semibold border-b pb-2">
            Datos del Acudiente
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
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
          </div>
        </section>

        <Separator />

        {/* Información del Servicio */}
        <section className="space-y-4">
          <h3 className="text-lg font-semibold border-b pb-2">
            Información del Servicio
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
          </div>
        </section>

        <div className="flex justify-end gap-4 pt-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => reset(defaultFormValues)}
          >
            Limpiar Formulario
          </Button>
          <Button
            onClick={handleSubmit(handleSubmitForm)}
            disabled={isSubmitting}
          >
            {isSubmitting ? "Guardando..." : "Guardar Persona"}
          </Button>
        </div>
      </div>
    </div>
  );
}