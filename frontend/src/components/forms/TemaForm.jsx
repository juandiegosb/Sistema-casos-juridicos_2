import React, { useState, useEffect } from "react";
import { useApiForm } from "@/hooks/useApiForm";
import { useForm } from "react-hook-form";
import { FormInput } from "./parts/FormInput";
import { FormSelect } from "./parts/FormSelect";
import { Button } from "@/components/ui/button";

export function TemaForm() {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();

  const API_URL_BASE = "http://localhost:8080/api"

  const [areas, setAreas] = useState([]);
  const { submit, isSubmitting } = useApiForm({ endpoint: `${API_URL_BASE}/temas`})

  useEffect(() => {
      const fetchAreas = async () => {
        try {
          const response = await fetch(`${API_URL_BASE}/areas`);
          if (response.ok) {
            const areasData = await response.json();
            const areaOptions = areasData.map(area => ({
              value: area.id.toString(),
              label: area.nombre
            }));
            setAreas(areaOptions);
          } else {
            console.error('Error al cargar áreas');
          }
        } catch (error) {
          console.error('Error de red al cargar áreas:', error);
        }
      };
      fetchAreas();
  }, []);

  const onSubmit = async (data) => {
    await submit(data);
  };

  return (
    <div>
        <FormInput
            name="nombre"
            label="Nombre"
            register={register}
            errors={errors}
            rules={{ required: "El nombre es obligatorio" }}
        />
        <FormSelect
            name="areaId"
            label="Área"
            options={areas}
            register={register}
            errors={errors}
        />
        <Button className="my-2" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
            Guardar tema
        </Button>
    </div>
  )
}
