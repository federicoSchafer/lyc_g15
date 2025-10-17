package lyc.compiler.files;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DotFileGenerator implements FileGenerator {

        private final Nodo raiz;

        public DotFileGenerator(Nodo raiz) {
            this.raiz = raiz;
        }

        @Override
        public void generate(FileWriter writer) throws IOException {
            writer.write("digraph G {\n");
            if (raiz != null) {
                AtomicInteger idCounter = new AtomicInteger(0);
                writeNode(writer, raiz, idCounter);
            }
            writer.write("}\n");
        }

        private int writeNode(FileWriter writer, Nodo nodo, AtomicInteger idCounter) throws IOException {
            int id = idCounter.getAndIncrement();
            writer.write("  node" + id + " [label=\"" + nodo.getValor() + "\"];\n");

            if (nodo.getIzquierdo() != null) {
                int leftId = writeNode(writer, nodo.getIzquierdo(), idCounter);
                writer.write("  node" + id + " -> node" + leftId + ";\n");
            }
            if (nodo.getDerecho() != null) {
                int rightId = writeNode(writer, nodo.getDerecho(), idCounter);
                writer.write("  node" + id + " -> node" + rightId + ";\n");
            }

            return id;
        }
}
