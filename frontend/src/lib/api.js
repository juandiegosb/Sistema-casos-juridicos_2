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

export function getApiErrorDescription(payload, fallback = "Verifica la información e intenta nuevamente.") {
  const messages = getApiErrorMessages(payload);

  if (messages.length > 0) {
    return messages.join("\n");
  }

  const title = getApiErrorTitle(payload, "");
  return title || fallback;
}
