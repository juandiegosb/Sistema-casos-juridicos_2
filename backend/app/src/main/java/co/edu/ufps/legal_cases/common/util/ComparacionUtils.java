package co.edu.ufps.legal_cases.common.util;

import java.util.Objects;
import java.util.function.Function;

public final class ComparacionUtils {

    private ComparacionUtils() {
    }

    //Para hacer comparaciones en lo service y no tener problemas por los nulls
    public static boolean equalsIgnoreCase(String actual, String nuevo) {
        if (actual == null || nuevo == null) {
            return Objects.equals(actual, nuevo);
        }

        return actual.equalsIgnoreCase(nuevo);
    }

    //Generica para comparar cualquier objeto por su id, evitando problemas con nulls
    //El funtion es para obtener el id de cualquier objeto, por ejemplo: TipoDocumento::getId
    public static <T> boolean mismoId(T actual, T nuevo, Function<T, Long> obtenerId) {
        Long idActual = actual != null ? obtenerId.apply(actual) : null;
        Long idNuevo = nuevo != null ? obtenerId.apply(nuevo) : null;

        return Objects.equals(idActual, idNuevo);
    }
}