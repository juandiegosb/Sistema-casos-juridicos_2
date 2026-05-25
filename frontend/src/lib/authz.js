export function normalizar(value) {
  return String(value || "")
    .trim()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toUpperCase();
}

export function nombrePermiso(permiso) {
  if (typeof permiso === "string") return permiso;

  return (
    permiso?.nombre ||
    permiso?.nombrePermiso ||
    permiso?.descripcion ||
    permiso?.permiso ||
    ""
  );
}

export function obtenerPermisos(user) {
  return Array.isArray(user?.permisos) ? user.permisos : [];
}

export function obtenerNombresPermisos(user) {
  return obtenerPermisos(user)
    .map(nombrePermiso)
    .filter(Boolean);
}

export function tienePermiso(user, permiso) {
  const permisos = obtenerNombresPermisos(user).map(normalizar);
  return permisos.includes(normalizar(permiso));
}

export function tieneAlgunPermiso(user, permisosRequeridos = []) {
  const permisos = obtenerNombresPermisos(user).map(normalizar);

  return permisosRequeridos
    .filter(Boolean)
    .some((permiso) => permisos.includes(normalizar(permiso)));
}

export function tieneTodosLosPermisos(user, permisosRequeridos = []) {
  const permisos = obtenerNombresPermisos(user).map(normalizar);

  return permisosRequeridos
    .filter(Boolean)
    .every((permiso) => permisos.includes(normalizar(permiso)));
}

export function tienePerfil(user, perfil) {
  return normalizar(user?.tipoPerfil) === normalizar(perfil);
}

export function tieneRol(user, rol) {
  return normalizar(user?.rolNombre || user?.rol?.nombre) === normalizar(rol);
}

export function esAdministrativo(user) {
  return tienePerfil(user, "ADMINISTRATIVO");
}

export function esAsesor(user) {
  return tienePerfil(user, "ASESOR");
}

export function esEstudiante(user) {
  return tienePerfil(user, "ESTUDIANTE");
}

export function esMonitor(user) {
  return tienePerfil(user, "MONITOR");
}

export function esConciliador(user) {
  return tienePerfil(user, "CONCILIADOR");
}