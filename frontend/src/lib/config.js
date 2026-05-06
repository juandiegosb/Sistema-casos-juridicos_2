export const API_URL_BASE = (function() {
  let url = process.env.NEXT_PUBLIC_API_URL_BASE || "http://localhost:8080/api";
  if (url && !url.startsWith("http://") && !url.startsWith("https://")) {
    url = "https://" + url;
  }
  return url;
})();

export const FILE_STORAGE_API_URL_BASE = (function() {
  let url = process.env.NEXT_PUBLIC_FILE_STORAGE_API_URL_BASE || "http://localhost:8080/api";
  if (url && !url.startsWith("http://") && !url.startsWith("https://")) {
    url = "https://" + url;
  }
  return url;
})();