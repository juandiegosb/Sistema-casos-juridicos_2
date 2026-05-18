export function normalizar(value) {
  return String(value || "").trim().toUpperCase();
}

export function obtenerPermisos(user) {
  return Array.isArray(user?.permisos) ? user.permisos : [];
}

export function tienePermiso(user, permiso) {
  const permisos = obtenerPermisos(user).map(normalizar);
  return permisos.includes(normalizar(permiso));
}

export function tieneAlgunPermiso(user, permisosRequeridos = []) {
  const permisos = obtenerPermisos(user).map(normalizar);

  return permisosRequeridos.some((permiso) =>
    permisos.includes(normalizar(permiso))
  );
}

export function tieneTodosLosPermisos(user, permisosRequeridos = []) {
  const permisos = obtenerPermisos(user).map(normalizar);

  return permisosRequeridos.every((permiso) =>
    permisos.includes(normalizar(permiso))
  );
}

export function tienePerfil(user, perfil) {
  return normalizar(user?.tipoPerfil) === normalizar(perfil);
}

export function tieneRol(user, rol) {
  return normalizar(user?.rolNombre) === normalizar(rol);
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