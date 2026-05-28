function normalizarApiUrl(url) {
  let apiUrl = url || "http://localhost:8080/api";

  if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://")) {
    apiUrl = `https://${apiUrl}`;
  }

  if (apiUrl.endsWith("/") && apiUrl.length > 1) {
    apiUrl = apiUrl.slice(0, -1);
  }

  if (!apiUrl.endsWith("/api")) {
    apiUrl = `${apiUrl}/api`;
  }

  return apiUrl;
}

export const API_URL_BASE = normalizarApiUrl(
  process.env.NEXT_PUBLIC_API_URL_BASE ||
    process.env.NEXT_PUBLIC_API_URL ||
    "http://localhost:8080/api"
);

export const FILE_STORAGE_API_URL_BASE = normalizarApiUrl(
  process.env.NEXT_PUBLIC_FILE_STORAGE_API_URL_BASE ||
    process.env.NEXT_PUBLIC_API_URL_BASE ||
    process.env.NEXT_PUBLIC_API_URL ||
    "http://localhost:8080/api"
);