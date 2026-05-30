/**
 * Lee el cuerpo de la respuesta y devuelve JSON o texto.
 * @param {Response} response - Respuesta HTTP recibida.
 * @returns {Promise<any>} Cuerpo parseado o texto crudo.
 */
export async function readResponseBody(response) {
  if (!response || response.status === 204) return null;

  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

/**
 * Convierte un valor en mensaje de error legible.
 * @param {any} value - Valor de error o detalle.
 * @param {string|null} fieldName - Nombre del campo asociado.
 * @returns {string|null} Mensaje procesado o null.
 */
function valueToMessage(value, fieldName) {
  if (value === null || value === undefined || value === "") return null;

  if (typeof value === "string") {
    return fieldName ? `${fieldName}: ${value}` : value;
  }

  if (typeof value === "number" || typeof value === "boolean") {
    return fieldName ? `${fieldName}: ${value}` : String(value);
  }

  return null;
}

/**
 * Recorre valores anidados y agrega mensajes al arreglo.
 * @param {any} value - Valor a procesar para mensajes.
 * @param {string|null} fieldName - Nombre del campo actual.
 * @param {Array<string>} messages - Arreglo para acumular mensajes.
 * @returns {void} No retorna valor.
 */
function collectMessages(value, fieldName, messages) {
  const directMessage = valueToMessage(value, fieldName);
  if (directMessage) {
    messages.push(directMessage);
    return;
  }

  if (Array.isArray(value)) {
    value.forEach((item) => collectMessages(item, fieldName, messages));
    return;
  }

  if (value && typeof value === "object") {
    Object.entries(value).forEach(([key, nestedValue]) => {
      collectMessages(nestedValue, key, messages);
    });
  }
}

/**
 * Extrae mensajes de error desde la carga útil de la API.
 * @param {any} payload - Respuesta de error de la API.
 * @returns {Array<string>} Mensajes individuales.
 */
export function getApiErrorMessages(payload) {
  if (!payload) return [];
  if (typeof payload === "string") return payload ? [payload] : [];

  const messages = [];
  const detailSources = [
    payload.detalles,
    payload.details,
    payload.errors,
    payload.fieldErrors,
    payload.validaciones,
  ];

  detailSources.forEach((source) => collectMessages(source, null, messages));

  return [...new Set(messages.filter(Boolean))];
}

/**
 * Determina el título de error desde la carga útil de la API.
 * @param {any} payload - Respuesta de error de la API.
 * @param {string} [fallback="Error en la operación"] - Título por defecto.
 * @returns {string} Título de error.
 */
export function getApiErrorTitle(payload, fallback = "Error en la operación") {
  if (!payload) return fallback;
  if (typeof payload === "string") return payload || fallback;

  return (
    payload.mensaje ||
    payload.message ||
    payload.descripcion ||
    payload.error ||
    fallback
  );
}

/**
 * Construye la descripción de error a partir de los detalles disponibles.
 * @param {any} payload - Respuesta de error de la API.
 * @param {string} [fallback="Verifica la información e intenta nuevamente."] - Descripción por defecto.
 * @returns {string} Descripción de error compuesta.
 */
export function getApiErrorDescription(payload, fallback = "Verifica la información e intenta nuevamente.") {
  const messages = getApiErrorMessages(payload);

  if (messages.length > 0) {
    return messages.join("\n");
  }

  const title = getApiErrorTitle(payload, "");
  return title || fallback;
}
