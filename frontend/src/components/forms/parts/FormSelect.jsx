export function FormSelect({
  name,
  label,
  options,
  register,
  errors,
  rules,
  placeholder = "Seleccione una opción",
  ...props
}) {
  return (
    <div className="flex flex-col gap-1.5 w-full">
      {label && (
        <label htmlFor={name} className="text-sm font-medium leading-none">
          {label}
        </label>
      )}

      <select
        id={name}
        defaultValue=""
        {...register(name, rules)}
        {...props}
        className={`flex h-8 w-full rounded-lg border px-2.5 py-1 ${
          errors?.[name] ? "border-red-500" : ""
        }`}
      >
        <option value="">{placeholder}</option>
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>

      {errors?.[name] && (
        <p className="text-xs text-red-500">{errors[name]?.message}</p>
      )}
    </div>
  );
}
