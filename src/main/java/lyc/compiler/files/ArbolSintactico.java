package lyc.compiler.files;

public class ArbolSintactico {
    //Hojas
    public static Nodo crearHoja(Object etiqueta) {
        return new Nodo(etiqueta);
    }

    //Nodo para operadores unarios (un hijo)
    public static Nodo crearNodo(Object etiqueta, Nodo hijoIzq) {
        return new Nodo(etiqueta, hijoIzq);
    }

    //Nodo con dos hijos
    public static Nodo crearNodo(Object etiqueta, Nodo hijoIzq, Nodo hijoDer) {
        return new Nodo(etiqueta, hijoIzq, hijoDer);
    }
}
