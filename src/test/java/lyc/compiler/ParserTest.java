package lyc.compiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import lyc.compiler.model.*;
import java_cup.runtime.Symbol;
import lyc.compiler.factories.ParserFactory;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static lyc.compiler.constants.Constants.*;

public class ParserTest {

    // ----------------------------
    // Asignaciones simples
    // ----------------------------
    @Test
    public void simpleAssignments() throws Exception {
        compilationSuccessful("x = 5;");
        compilationSuccessful("y = variable;");
        compilationSuccessful("z = \"hello\";");
    }

    // ----------------------------
    // Asignaciones con expresiones
    // ----------------------------
    @Test
    public void assignmentWithExpression() throws Exception {
        compilationSuccessful("c = d * (e - 21) / 4;");
        compilationSuccessful("x = (a + b) * 3 - 10 / 2;");
        compilationSuccessful("result = (v1 + v2) * (v3 - 5);");
    }

    // ----------------------------
    // Operadores unarios
    // ----------------------------
    @Test
    public void unaryOperators() throws Exception {
        compilationSuccessful("x = -5;");
        compilationSuccessful("y = +variable;");
        compilationSuccessful("z = -(a + b);");
        compilationSuccessful("w = -(-x);");
    }

    // ----------------------------
    // Expresiones con signos anidados
    // ----------------------------
    @Test
    public void nestedSigns() throws Exception {
        compilationSuccessful("x = -(a + 3 * -(b - 2));");
        compilationSuccessful("y = -(1 + -(2 + 3));");
    }

    // ----------------------------
    // Numeros validos en limites
    // ----------------------------
    @Test
    public void edgeIntegerValues() throws Exception {
        compilationSuccessful("a = 2147483647;"); // Integer.MAX_VALUE
        compilationSuccessful("b = -2147483648;"); // Integer.MIN_VALUE (como operador unario + literal)
    }

    // ----------------------------
    // Parentesis y precedencia
    // ----------------------------
    @Test
    public void precedenceAndParentheses() throws Exception {
        compilationSuccessful("result = a + b * c;");
        compilationSuccessful("result = (a + b) * c;");
        compilationSuccessful("result = a * (b + c);");
        compilationSuccessful("complex = ((a + b) * (c - d)) / ((e + f) - g);");
    }

    // ----------------------------
    // Expresiones con strings
    // ----------------------------
    @Test
    public void stringExpressions() throws Exception {
        compilationSuccessful("message = \"Hello World\";");
        compilationSuccessful("path = \"C:\\\\temp\\\\file.txt\";");
        compilationSuccessful("quote = \"She said \\\"Hello\\\"\";");
    }

    
    @Test
    public void lexerErrorIntegerOverflow() {
        assertThrows(InvalidIntegerException.class, () -> {
            scan("x = 2147483648;"); // Integer.MAX_VALUE + 1
        });
    }

    @Test 
    public void lexerErrorStringTooLong() {
        String longString = "\"" + "a".repeat(MAX_LENGTH + 1) + "\"";
        assertThrows(InvalidLengthException.class, () -> {
            scan("s = " + longString + ";");
        });
    }

    @Test
    public void lexerErrorIdentifierTooLong() {
        String longIdentifier = "a".repeat(MAX_LENGTH + 1);
        assertThrows(InvalidLengthException.class, () -> {
            scan(longIdentifier + " = 5;");
        });
    }

    @Test
    public void lexerErrorUnknownCharacter() {
        assertThrows(UnknownCharacterException.class, () -> {
            scan("x = 5 $;");
        });
    }

    // ----------------------------
    // TESTS DE ERRORES DEL PARSER (errores de sintaxis)
    // ----------------------------
    @Test
    public void parserSyntaxErrors() {
        // Número sin asignación
        compilationError("1234;");
        
        // Operador incorrecto
        compilationError("x = * 5;");
        
        // Paréntesis sin cerrar
        compilationError("y = (2 + 3;");
        
        // Falta punto y coma
        compilationError("x = 5");
        
        // Operador sin operando
        compilationError("x = 5 +;");
        
        // División sin operando
        compilationError("x = / 5;");
    }

        // ----------------------------
    // Métodos auxiliares
    // ----------------------------
    private void compilationSuccessful(String input) throws Exception {
        Symbol result = scan(input);
        assertThat(result.sym).isEqualTo(ParserSym.EOF);
    }

    private void compilationError(String input) {
        assertThrows(Exception.class, () -> scan(input));
    }

    private Symbol scan(String input) throws Exception {
        return ParserFactory.create(input).parse();
    }
}