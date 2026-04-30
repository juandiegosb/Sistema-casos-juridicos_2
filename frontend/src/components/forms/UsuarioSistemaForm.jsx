import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { FormCheckbox } from "./parts/FormCheckbox";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";

export function UsuarioSistemaForm() {
  const { register, handleSubmit, watch, reset, formState: { errors } } = useForm();

  const personaSeleccionada = watch("personaId");
  const nuevoRol = watch("nuevoRol");

  const API_URL = "http://localhost:8080/api";
  const REQUIRED = "Campo obligatorio";

  const [personas, setPersonas] = useState([]);
  const [asesores, setAsesores] = useState([]);
  const [areas, setAreas] = useState([]);
  const [sedes, setSedes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [personaActual, setPersonaActual] = useState(null);

  const fetchData = async (url) => {
    const res = await fetch(url);
    if (!res.ok) throw new Error("Error en la petición");
    return await res.json();
  };

  // Cargar personas disponibles al montar el componente
  useEffect(() => {
    const cargarPersonas = async () => {
      try {
        const personasData = await fetchData(`${API_URL}/personas`);
        setPersonas(
          personasData.map(p => ({
            value: p.id,
            label: `${p.nombres} ${p.apellidos} - ${p.numeroDocumento}`
          }))
        );
      } catch (error) {
        console.error("Error cargando personas:", error);
        toast.error("Error al cargar personas");
      }
    };

    cargarPersonas();
  }, []);

  // Cargar datos específicos según el nuevo rol seleccionado
  useEffect(() => {
    const cargarDatosRol = async () => {
      if (!nuevoRol) return;
      try {
        if (nuevoRol === "estudiantes") {
          const asesoresData = await fetchData(`${API_URL}/asesores`);
          setAsesores(
            asesoresData.map(a => ({
              value: a.id,
              label: `${a.nombre} - ${a.documento}`
            }))
          );
        }

        if (nuevoRol === "asesores") {
          const areasData = await fetchData(`${API_URL}/areas`);
          setAreas(
            areasData.map(a => ({
              value: a.id,
              label: a.nombre
            }))
          );
        }

        const sedesData = await fetchData(`${API_URL}/sedes`);
        setSedes(
          sedesData.map(s => ({
            value: s.id,
            label: s.nombre
          }))
        );
      } catch (error) {
        console.error("Error cargando datos:", error);
      }
    };

    cargarDatosRol();
  }, [nuevoRol]);

  const tipoOptions = [
    { value: "estudiantes", label: "Estudiante" },
    { value: "asesores", label: "Asesor" },
    { value: "monitores", label: "Monitor" },
    { value: "administrativos", label: "Administrativo" },
    { value: "conciliadores", label: "Conciliador" },
  ];

  const handleSubmitForm = async (data) => {
    if (!personaSeleccionada || !nuevoRol) {
      toast.error("Debe seleccionar una persona y un rol");
      return;
    }

    setLoading(true);
    try {
      const updateData = {
        personaId: personaSeleccionada,
        nuevoRol: nuevoRol,
        datosEspecificos: {}
      };

      // Agregar datos específicos según el rol
      if (nuevoRol === "estudiantes" && data.asesorId) {
        updateData.datosEspecificos.asesorId = data.asesorId;
        updateData.datosEspecificos.conciliacion = data.conciliacion || false;
      }

      if (nuevoRol === "asesores" && data.areaId) {
        updateData.datosEspecificos.areaId = data.areaId;
      }

      if (nuevoRol === "administrativos") {
        updateData.datosEspecificos.directora = data.directora || false;
      }

      if (nuevoRol === "conciliadores" && data.tipoConciliador) {
        updateData.datosEspecificos.tipoConciliador = data.tipoConciliador;
      }

      const res = await fetch(`${API_URL}/personas/${personaSeleccionada}/cambiar-rol`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(updateData)
      });

      if (!res.ok) throw new Error("Error al actualizar");

      const result = await res.json();
      toast.success(`Rol actualizado: ${result.tipoUsuario}`);
      reset();
      setPersonaActual(null);

    } catch (error) {
      console.error("Error al enviar:", error);
      toast.error("Error al actualizar el rol");
    } finally {
      setLoading(false);
    }
  };

  const renderCamposEspecificos = () => {
    switch (nuevoRol) {
      case "estudiantes":
        return (
          <>
            <FormSelect
              name="asesorId"
              label="Asesor"
              options={asesores}
              register={register}
              errors={errors}
              rules={{ required: REQUIRED }}
            />

            <FormCheckbox
              name="conciliacion"
              label="¿Tiene conciliación?"
              register={register}
              errors={errors}
            />
          </>
        );

      case "asesores":
        return (
          <FormSelect
            name="areaId"
            label="Área"
            options={areas}
            register={register}
            errors={errors}
            rules={{ required: REQUIRED }}
          />
        );

      case "administrativos":
        return (
          <FormCheckbox
            name="directora"
            label="¿Es directora?"
            register={register}
            errors={errors}
          />
        );

      case "conciliadores":
        return (
          <FormSelect
            name="tipoConciliador"
            label="Tipo de Conciliador"
            options={[
              { value: "JUDICIAL", label: "Judicial" },
              { value: "EXTRAJUDICIAL", label: "Extrajudicial" },
            ]}
            register={register}
            errors={errors}
            rules={{ required: REQUIRED }}
          />
        );

      default:
        return null;
    }
  };

  return (
    <div className="space-y-8 p-6 bg-card rounded-xl shadow border">

      <div>
        <h2 className="text-2xl font-bold">Asignar/Cambiar Rol de Usuario</h2>
        <p className="text-muted-foreground">
          Selecciona una persona y asígna o cambia su rol en el sistema.
        </p>
      </div>

      <FormSelect
        name="personaId"
        label="Seleccionar Persona"
        options={personas}
        register={register}
        errors={errors}
        rules={{ required: REQUIRED }}
      />

      <FormSelect
        name="nuevoRol"
        label="Nuevo Rol"
        options={tipoOptions}
        register={register}
        errors={errors}
        rules={{ required: REQUIRED }}
      />

      <Separator />

      {nuevoRol && (
        <section className="space-y-4">
          <h3 className="font-semibold">Información específica del rol</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {renderCamposEspecificos()}
          </div>
        </section>
      )}

      <div className="flex justify-end gap-4">
        <Button
          variant="outline"
          onClick={() => {
            reset();
            setPersonaActual(null);
          }}
        >
          Limpiar
        </Button>
        <Button
          onClick={handleSubmit(handleSubmitForm)}
          disabled={loading || !personaSeleccionada || !nuevoRol}
        >
          {loading ? "Actualizando..." : "Actualizar Rol"}
        </Button>
      </div>

    </div>
  );
}