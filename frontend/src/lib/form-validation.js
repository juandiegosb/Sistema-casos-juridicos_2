export const REQUIRED_MESSAGE = "El campo es obligatorio";

export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/i;

export function isBlank(value) {
  return String(value ?? "").trim() === "";
}

export function optionalEmailRule(message = "Ingrese un correo electrónico válido") {
  return {
    validate: (value) => isBlank(value) || EMAIL_PATTERN.test(String(value).trim()) || message,
  };
}

export function requiredEmailRule(message = "Ingrese un correo electrónico válido") {
  return {
    required: REQUIRED_MESSAGE,
    pattern: {
      value: EMAIL_PATTERN,
      message,
    },
  };
}

export function nonNegativeNumberRule(message = "El valor no puede ser negativo") {
  return {
    min: {
      value: 0,
      message,
    },
  };
}

export function maxNumberRule(max, message = `El valor no puede ser mayor a ${max}`) {
  return {
    max: {
      value: max,
      message,
    },
  };
}

export function requiredSelectRule(message = "Debe seleccionar una opción") {
  return {
    required: message,
  };
}
