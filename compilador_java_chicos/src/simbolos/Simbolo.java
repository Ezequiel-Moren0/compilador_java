package simbolos;

public class Simbolo {
    private String nombre;
    private String tipo; // "long", "double", "string", "bool", "long[]", etc.
    private String valor; // representación textual del valor si existe
    private String ambito; // "global" o nombre de función
    private int linea;

    public Simbolo(String nombre, String tipo, String valor, String ambito, int linea) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = valor;
        this.ambito = ambito;
        this.linea = linea;
    }

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getValor() { return valor; }
    public String getAmbito() { return ambito; }
    public int getLinea() { return linea; }

    public void setValor(String valor) { this.valor = valor; }

    @Override
    public String toString() {
        return nombre + "\t" + tipo + "\t" + valor + "\t" + ambito + "\t" + linea;
    }
}
