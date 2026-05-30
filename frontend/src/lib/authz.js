/**
 * Normaliza un valor a texto sin acentos y en mayúsculas.
 * @param {string|number|boolean|null|undefined} value - Valor a normalizar.
 * @returns {string} Cadena normalizada.
 */
export function normalizar(value) {
  return String(value || "")
    .trim()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toUpperCase();
}

/**
 * Obtiene el nombre de un permiso a partir de un objeto o cadena.
 * @param {string|Object} permiso - Permiso en formato de cadena u objeto.
 * @returns {string} Nombre del permiso.
 */
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

/**
 * Devuelve la lista de permisos del usuario.
 * @param {Object} user - Objeto de usuario con permisos.
 * @returns {Array<any>} Lista de permisos.
 */
export function obtenerPermisos(user) {
  return Array.isArray(user?.permisos) ? user.permisos : [];
}

/**
 * Devuelve los nombres normalizados de los permisos.
 * @param {Object} user - Objeto de usuario con permisos.
 * @returns {Array<string>} Permisos en formato de texto.
 */
export function obtenerNombresPermisos(user) {
  return obtenerPermisos(user)
    .map(nombrePermiso)
    .filter(Boolean);
}

/**
 * Verifica si el usuario tiene un permiso específico.
 * @param {Object} user - Objeto de usuario con permisos.
 * @param {string} permiso - Nombre del permiso a verificar.
 * @returns {boolean} True si el permiso existe.
 */
export function tienePermiso(user, permiso) {
  const permisos = obtenerNombresPermisos(user).map(normalizar);
  return permisos.includes(normalizar(permiso));
}

/**
 * Verifica si el usuario tiene al menos uno de los permisos requeridos.
 * @param {Object} user - Objeto de usuario con permisos.
 * @param {Array<string>} permisosRequeridos - Permisos de los que se requiere alguno.
 * @returns {boolean} True si existe un permiso requerido.
 */
export function tieneAlgunPermiso(user, permisosRequeridos = []) {
  const permisos = obtenerNombresPermisos(user).map(normalizar);

  return permisosRequeridos
    .filter(Boolean)
    .some((permiso) => permisos.includes(normalizar(permiso)));
}

/**
 * Verifica si el usuario tiene todos los permisos requeridos.
 * @param {Object} user - Objeto de usuario con permisos.
 * @param {Array<string>} permisosRequeridos - Permisos que deben existir.
 * @returns {boolean} True si todos los permisos existen.
 */
export function tieneTodosLosPermisos(user, permisosRequeridos = []) {
  const permisos = obtenerNombresPermisos(user).map(normalizar);

  return permisosRequeridos
    .filter(Boolean)
    .every((permiso) => permisos.includes(normalizar(permiso)));
}

/**
 * Verifica si el usuario pertenece a un perfil específico.
 * @param {Object} user - Objeto de usuario con perfil.
 * @param {string} perfil - Perfil esperado.
 * @returns {boolean} True si el perfil coincide.
 */
export function tienePerfil(user, perfil) {
  return normalizar(user?.tipoPerfil) === normalizar(perfil);
}

/**
 * Verifica si el usuario tiene un rol específico.
 * @param {Object} user - Objeto de usuario con rol.
 * @param {string} rol - Rol esperado.
 * @returns {boolean} True si el rol coincide.
 */
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