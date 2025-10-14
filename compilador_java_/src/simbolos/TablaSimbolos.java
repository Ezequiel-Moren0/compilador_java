package simbolos;

import java.util.*;

public class TablaSimbolos {
    private HashMap<String, Simbolo> tabla = new HashMap<>();

    public boolean agregar(String nombre, String tipo, String valor, String ambito, int linea) {
        if (tabla.containsKey(nombre)) {
            return false;
        }
        tabla.put(nombre, new Simbolo(nombre, tipo, valor, ambito, linea));
        return true;
    }

    public Simbolo buscar(String nombre) {
        return tabla.get(nombre);
    }

    public boolean existe(String nombre) {
        return tabla.containsKey(nombre);
    }

    public void actualizarValor(String nombre, String valor) {
        Simbolo s = tabla.get(nombre);
        if (s != null) s.setValor(valor);
    }

    public void mostrar() {
        System.out.println("\n--- TABLA DE SÍMBOLOS ---");
        System.out.println("Nombre\tTipo\tValor\tÁmbito\tLínea");
        for (Simbolo s : tabla.values()) {
            System.out.println(s);
        }
    }
}
