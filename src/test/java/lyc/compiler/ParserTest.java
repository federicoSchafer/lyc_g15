package lyc.compiler;

import java_cup.runtime.Symbol;
import lyc.compiler.factories.ParserFactory;
import lyc.compiler.model.InvalidIntegerException;
import lyc.compiler.model.InvalidLengthException;
import lyc.compiler.model.UnknownCharacterException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;
import static lyc.compiler.Constants.EXAMPLES_ROOT_DIRECTORY;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled
public class ParserTest {

    // ----------------------------
    // Asignaciones con expresiones
    // ----------------------------
    @Test
    public void assignmentWithExpression() throws Exception {
        compilationSuccessful("c=d*(e-21)/4");
        compilationSuccessful("x = (a+b)*3 - 10/2");
        compilationSuccessful("result = (v1 + v2) * (v3 - 5)");
    }

    // ----------------------------
    // Asignaciones múltiples desde archivo
    // ----------------------------
    @Test
    void assignments() throws Exception {
        compilationSuccessful(readFromFile("assignments.txt"));
    }

    // ----------------------------
    // Números fuera de rango
    // ----------------------------
    @Test
    public void invalidPositiveInteger() {
        assertThrows(InvalidIntegerException.class, () -> {
            compilationSuccessful("x = 2147483648"); // Integer.MAX_VALUE + 1
        });
    }

    @Test
    public void invalidNegativeInteger() {
        assertThrows(InvalidIntegerException.class, () -> {
            compilationSuccessful("x = -2147483649"); // Integer.MIN_VALUE - 1
        });
    }

    // ----------------------------
    // Números válidos en límites
    // ----------------------------
    @Test
    public void edgeIntegerValues() throws Exception {
        compilationSuccessful("a = 2147483647"); // Integer.MAX_VALUE
        compilationSuccessful("b = -2147483648"); // Integer.MIN_VALUE
    }

    // ----------------------------
    // Expresiones complejas con signos anidados
    // ----------------------------
    @Test
    public void nestedSigns() throws Exception {
        compilationSuccessful("x = -(-a + 3*-(b-2))");
        compilationSuccessful("y = -(1 + -(2 + 3))");
    }

    // ----------------------------
    // Strings demasiado largos
    // ----------------------------
    @Test
    public void stringTooLong() {
        String longString = "\"" + "a".repeat(Constants.MAX_LENGTH + 1) + "\"";
        assertThrows(InvalidLengthException.class, () -> compilationSuccessful("s = " + longString));
    }

    // ----------------------------
    // Identificadores demasiado largos
    // ----------------------------
    @Test
    public void identifierTooLong() {
        String longIdentifier = "a".repeat(Constants.MAX_LENGTH + 1);
        assertThrows(InvalidLengthException.class, () -> compilationSuccessful(longIdentifier + " = 5"));
    }

    // ----------------------------
    // Caracteres desconocidos
    // ----------------------------
    @Test
    public void unknownCharacter() {
        assertThrows(UnknownCharacterException.class, () -> compilationSuccessful("x = 5 $"));
    }

    // ----------------------------
    // Errores de sintaxis
    // ----------------------------
    @Test
    public void syntaxError() {
        compilationError("1234");       // número sin asignación
        compilationError("x = * 5");    // operador incorrecto
        compilationError("y = (2 + 3"); // paréntesis sin cerrar
    }

    // ----------------------------
    // Métodos auxiliares
    // ----------------------------
    private void compilationSuccessful(String input) throws Exception {
        Symbol result = scan(input);
        assertThat(result.sym).isEqualTo(ParserSym.EOF);
    }

    private void compilationError(String input){
        assertThrows(Exception.class, () -> scan(input));
    }

    private Symbol scan(String input) throws Exception {
        return ParserFactory.create(input).parse();
    }

    private String readFromFile(String fileName) throws IOException {
        URL url = new URL(EXAMPLES_ROOT_DIRECTORY + "/%s".formatted(fileName));
        assertThat(url).isNotNull();
        return IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
    }
}
