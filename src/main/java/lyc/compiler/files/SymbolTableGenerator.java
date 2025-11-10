package lyc.compiler.files;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SymbolTableGenerator implements FileGenerator{
    private final Set<SymbolEntry> entries = new LinkedHashSet<>();

    public void addVariables(List<Object> names, Object t) {
        for (Object name : names) {
            String value = "";
            String symbolName = "_" + name.toString();
            String type = t.toString();
            entries.add(new SymbolEntry(symbolName, type, value, symbolName.length()));
        }
    }


    public void addConstant(Object name, Object value, String type) {
        String symbolName = "_" + name.toString();
        String valStr = value.toString();
        entries.add(new SymbolEntry(symbolName, type, valStr, valStr.length()));
    }

    public boolean findVariable(Object id){
        String symbolName = "_" + id.toString();
        return entries.contains(new SymbolEntry(symbolName, null, null, null));
    }

    public String varType(Object id){
        String symbolName = "_" + id.toString();
        for (SymbolEntry entry : entries) {
            if (entry.getName().equals(symbolName)) {
                return entry.getType();
            }
        }
        return null;
    }


    @Override
    public void generate(FileWriter fileWriter) throws IOException {
        fileWriter.write(String.format("%-15s %-10s %-15s %-10s%n", "Nombre", "TipoDato", "Valor", "Longitud"));
        for (SymbolEntry entry : entries) {
            String lengthStr = entry.length == null ? "-" : entry.length.toString();
            fileWriter.write(String.format("%-15s %-10s %-15s %-10s%n",
                    entry.name, entry.type, entry.value, lengthStr));
        }
    }

    public Set<SymbolEntry> getEntries() {
        return entries;
    }

    public static class SymbolEntry {
        String name;
        String type;
        String value;
        Integer length;

        SymbolEntry(String name, String type, String value, Integer length) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.length = length;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SymbolEntry)) return false;
            SymbolEntry that = (SymbolEntry) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }
}
