import React from 'react';
import { FormFileUpload } from './FormFileUpload';

export default function ArchivosConsultaForm({ archivos, onChange }) {
  return (
    <div className="space-y-4">
      <h3 className="text-lg font-medium">Documentos Adicionales</h3>
      <p className="text-sm text-muted-foreground">
        Selecciona los documentos relacionados con la consulta. Estos se subirán por separado.
      </p>
      <FormFileUpload
        name="archivos"
        label="Documentos a subir"
        multiple={true}
        setValue={(name, value) => onChange(value)}
        value={archivos}
        errors={{}}
      />
    </div>
  );
}