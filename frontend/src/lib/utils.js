import { clsx } from "clsx";
import { twMerge } from "tailwind-merge"

/**
 * Combina clases CSS y resuelve conflictos con Tailwind.
 * @param {...any} inputs - Clases CSS y condiciones.
 * @returns {string} Cadena de clases resultante.
 */
export function cn(...inputs) {
  return twMerge(clsx(inputs));
}
