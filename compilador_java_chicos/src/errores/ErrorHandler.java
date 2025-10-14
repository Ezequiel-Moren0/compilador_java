package errores;

public class ErrorHandler {
    public void reportar(String tipo, String descripcion, int linea) {
        System.err.printf("%s [línea %d]: %s%n", tipo, linea, descripcion);
    }
}
