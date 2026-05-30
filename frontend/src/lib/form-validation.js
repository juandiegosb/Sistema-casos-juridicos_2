export const REQUIRED_MESSAGE = "El campo es obligatorio";

export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/i;

/**
 * Verifica si un valor es vacío.
 * @param {any} value - Valor a comprobar.
 * @returns {boolean} True si el valor es vacío.
 */
export function isBlank(value) {
  return String(value ?? "").trim() === "";
}

/**
 * Regla de validación para correo opcional.
 * @param {string} [message="Ingrese un correo electrónico válido"] - Mensaje mostrado en caso de error.
 * @returns {Object} Reglas de validación.
 */
export function optionalEmailRule(message = "Ingrese un correo electrónico válido") {
  return {
    validate: (value) => isBlank(value) || EMAIL_PATTERN.test(String(value).trim()) || message,
  };
}

/**
 * Regla de validación para correo obligatorio.
 * @param {string} [message="Ingrese un correo electrónico válido"] - Mensaje mostrado en caso de error.
 * @returns {Object} Reglas de validación.
 */
export function requiredEmailRule(message = "Ingrese un correo electrónico válido") {
  return {
    required: REQUIRED_MESSAGE,
    pattern: {
      value: EMAIL_PATTERN,
      message,
    },
  };
}

/**
 * Regla de validación para número no negativo.
 * @param {string} [message="El valor no puede ser negativo"] - Mensaje mostrado en caso de error.
 * @returns {Object} Reglas de validación.
 */
export function nonNegativeNumberRule(message = "El valor no puede ser negativo") {
  return {
    min: {
      value: 0,
      message,
    },
  };
}

/**
 * Regla de validación para número máximo.
 * @param {number} max - Valor máximo permitido.
 * @param {string} [message] - Mensaje mostrado en caso de error.
 * @returns {Object} Reglas de validación.
 */
export function maxNumberRule(max, message = `El valor no puede ser mayor a ${max}`) {
  return {
    max: {
      value: max,
      message,
    },
  };
}

/**
 * Regla de validación para selección obligatoria.
 * @param {string} [message="Debe seleccionar una opción"] - Mensaje mostrado en caso de error.
 * @returns {Object} Reglas de validación.
 */
export function requiredSelectRule(message = "Debe seleccionar una opción") {
  return {
    required: message,
  };
}
