function normalizeUrl(value, fallback) {
  let url = String(value || fallback || "").trim();

  if (!url) return "";

  if (!url.startsWith("http://") && !url.startsWith("https://")) {
    url = `https://${url}`;
  }

  return url.replace(/\/+$/, "");
}

export const API_URL_BASE = normalizeUrl(
  process.env.NEXT_PUBLIC_API_URL || process.env.NEXT_PUBLIC_API_URL_BASE,
  "http://localhost:8080/api"
);

export const FILE_STORAGE_API_URL_BASE = normalizeUrl(
  process.env.NEXT_PUBLIC_FILE_STORAGE_API_URL ||
    process.env.NEXT_PUBLIC_FILE_STORAGE_API_URL_BASE ||
    process.env.NEXT_PUBLIC_API_URL ||
    process.env.NEXT_PUBLIC_API_URL_BASE,
  "http://localhost:8080/api"
);
