package lyc.compiler.files;

public class Nodo {
    private final String valor;
    private final Nodo izquierdo;
    private final Nodo derecho;

    public Nodo(Object valor, Nodo izquierdo, Nodo derecho) {
        this.valor = valor.toString();
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public Nodo(Object valor){
        this.valor = valor.toString();
        this.izquierdo = null;
        this.derecho = null;
    }

    public String getValor() { return valor; }
    public Nodo getIzquierdo() { return izquierdo; }
    public Nodo getDerecho() { return derecho; }
}
