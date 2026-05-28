import React from "react";
import { Input } from "@/components/ui/input";

function hasRequiredRule(rules) {
  return Boolean(rules?.required);
}

function renderLabel(label, required) {
  if (!label) return null;

  if (required && typeof label === "string") {
    return (
      <span className="inline-flex items-center gap-1">
        <span>{label}</span>
        <span className="text-red-500" aria-hidden="true">*</span>
      </span>
    );
  }

  return label;
}

export function FormInput({ name, label, type = "text", register, errors, rules, ...props }) {
  const error = errors?.[name];
  const required = hasRequiredRule(rules);

  return (
    <div className="flex flex-col gap-1.5 w-full">
      {label && (
        <label htmlFor={name} className="text-sm font-medium leading-none">
          {renderLabel(label, required)}
        </label>
      )}

      <Input
        id={name}
        type={type}
        aria-invalid={error ? "true" : "false"}
        aria-required={required ? "true" : "false"}
        {...register(name, rules)}
        {...props}
        className={`${props.className || ""} ${error ? "border-red-500" : ""}`.trim()}
      />

      {error && (
        <p className="text-xs text-red-500">
          {error?.message}
        </p>
      )}
    </div>
  );
}
