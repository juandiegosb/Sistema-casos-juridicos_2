package co.edu.ufps.legal_cases.common.util;

public final class NormalizacionUtils {

    private NormalizacionUtils() {
    }

    public static String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.trim().replaceAll("\\s+", " ");

        return limpio.isBlank() ? null : limpio;
    }

    public static boolean estaInformado(String valor) {
        String limpio = normalizarTexto(valor);

        return limpio != null && !limpio.equalsIgnoreCase("No informa");
    }

    public static String normalizarNumeroDocumento(String valor) {
        return removerCaracteres(normalizarTexto(valor), "[\\.\\-\\s]");
    }

    public static String normalizarTelefono(String valor) {
        return removerCaracteres(normalizarTexto(valor), "[^0-9]");
    }

    public static String normalizarEmail(String valor) {
        return convertirMinuscula(normalizarTexto(valor));
    }

    public static String normalizarUsuario(String valor) {
        return convertirMinuscula(normalizarTexto(valor));
    }

    public static String normalizarCodigo(String valor) {
        return convertirMayuscula(normalizarTexto(valor));
    }

    private static String removerCaracteres(String valor, String patron) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.replaceAll(patron, "");

        return limpio.isBlank() ? null : limpio;
    }

    private static String convertirMinuscula(String valor) {
        return valor != null ? valor.toLowerCase() : null;
    }

    private static String convertirMayuscula(String valor) {
        return valor != null ? valor.toUpperCase() : null;
    }
}