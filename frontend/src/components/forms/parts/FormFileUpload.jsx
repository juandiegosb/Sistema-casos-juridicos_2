import React from 'react';
import { UploadCloud, X, File as FileIcon } from 'lucide-react';

/**
 * Campo de carga de archivos compatible con react-hook-form.
 * @param {Object} props - Propiedades del componente.
 * @param {string} props.name - Nombre del campo de archivos.
 * @param {string} props.label - Etiqueta mostrada para el campo.
 * @param {boolean} [props.multiple=false] - Permite seleccionar múltiples archivos.
 * @param {function} props.setValue - Función para actualizar el valor en react-hook-form.
 * @param {File|File[]} props.value - Archivo o lista de archivos seleccionados.
 * @param {Object} props.errors - Objetos de error de validación.
 * @param {Object} [props.rules] - Reglas de validación del campo.
 * @returns {JSX.Element} Campo visual de carga de archivos.
 */
export function FormFileUpload({ 
  name, 
  label, 
  multiple = false, 
  setValue, 
  value,
  errors, 
  rules, 
  ...props 
}) {
  const selectedFiles = Array.isArray(value) ? value : (value ? [value] : []);

  const handleFileChange = (event) => {
    const files = Array.from(event.target.files);
    if (!files || files.length === 0) return;

    let newSelectedFiles = [];
    if (multiple) {
      newSelectedFiles = [...selectedFiles, ...files];
    } else {
      newSelectedFiles = [files[0]]; 
    }

    if (setValue) {
      setValue(name, multiple ? newSelectedFiles : newSelectedFiles[0], { shouldValidate: true, shouldDirty: true });
    }

    event.target.value = '';
  };

  const removeFile = (fileNameToRemove) => {
    const newSelectedFiles = selectedFiles.filter(f => f.name !== fileNameToRemove);
    
    if (setValue) {
      if (newSelectedFiles.length === 0) {
        setValue(name, multiple ? [] : null, { shouldValidate: true, shouldDirty: true });
      } else {
        setValue(name, multiple ? newSelectedFiles : newSelectedFiles[0], { shouldValidate: true, shouldDirty: true });
      }
    }
  };

  return (
    <div className="flex flex-col gap-1.5 w-full">
      {label && (
        <label htmlFor={name} className="text-sm font-medium leading-none">
          {label}
        </label>
      )}

      <div className={`
        relative flex flex-col items-center justify-center w-full h-32 
        border-2 border-dashed rounded-lg cursor-pointer 
        transition-colors hover:bg-muted/50
        ${errors?.[name] ? 'border-red-500 bg-red-50/50 dark:bg-red-950/20' : 'border-muted-foreground/25 bg-background'}
      `}>
        <input
          id={name}
          type="file"
          multiple={multiple}
          onChange={handleFileChange}
          className="absolute inset-0 w-full h-full opacity-0 cursor-pointer disabled:cursor-not-allowed"
          {...props}
        />
        
        <div className="flex flex-col items-center justify-center pt-5 pb-6 text-muted-foreground">
          <UploadCloud className="w-8 h-8 mb-2" />
          <p className="mb-2 text-sm text-center">
            <span className="font-semibold">Haz clic para seleccionar</span> o arrastra y suelta
          </p>
          <p className="text-xs text-center text-muted-foreground/70">
            {multiple ? 'Puedes seleccionar múltiples archivos' : 'Selecciona un archivo'}
          </p>
        </div>
      </div>

      {errors?.[name] && (
        <p className="text-xs text-red-500">
          {errors[name]?.message}
        </p>
      )}

      {/* Lista de archivos seleccionados */}
      {selectedFiles.length > 0 && (
        <div className="flex flex-col gap-2 mt-2">
          {selectedFiles.map((file, idx) => (
            <div key={idx} className="flex items-center justify-between p-2 text-sm border rounded-md bg-muted/30">
              <div className="flex items-center gap-2 truncate">
                <FileIcon className="w-4 h-4 shrink-0 text-muted-foreground" />
                <span className="truncate">{file.name}</span>
              </div>
              <button 
                type="button" 
                onClick={() => removeFile(file.name)}
                className="p-1 transition-colors rounded hover:bg-destructive/10 hover:text-destructive"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
