package lyc.compiler.files;

import java.io.FileWriter;
import java.io.IOException;

public class AsmCodeGenerator implements FileGenerator {

    private final String assemblerCode;

    public AsmCodeGenerator(String assemblerCode) {
        this.assemblerCode = assemblerCode;
    }

    @Override
    public void generate(FileWriter fileWriter) throws IOException {
        fileWriter.write(assemblerCode);
    }
}
