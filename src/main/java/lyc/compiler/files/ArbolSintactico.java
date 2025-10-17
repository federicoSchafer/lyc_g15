package lyc.compiler.files;

public class ArbolSintactico {
    public static Nodo crearHoja(Object etiqueta) {
        return new Nodo(etiqueta);
    }

    public static Nodo crearNodo(Object etiqueta, Nodo hijoIzq, Nodo hijoDer) {
        return new Nodo(etiqueta, hijoIzq, hijoDer);
    }
}
