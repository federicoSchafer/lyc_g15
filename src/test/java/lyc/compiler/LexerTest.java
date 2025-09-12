package lyc.compiler;

import lyc.compiler.factories.LexerFactory;
import lyc.compiler.model.CompilerException;
import lyc.compiler.model.InvalidNumericException;
import lyc.compiler.model.InvalidLengthException;
import lyc.compiler.model.UnknownCharacterException;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static lyc.compiler.constants.Constants.MAX_LENGTH;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled
public class LexerTest {

  private Lexer lexer;

  /* === Tests existentes === */

  @Test
  public void comment() throws Exception {
    scan("/*This is a comment*/");
    assertThat(nextToken()).isEqualTo(ParserSym.EOF);
  }

  @Test
  public void invalidStringConstantLength() {
    assertThrows(InvalidLengthException.class, () -> {
      scan("\"%s\"".formatted(getRandomString()));
      nextToken();
    });
  }

  @Test
  public void invalidIdLength() {
    assertThrows(InvalidLengthException.class, () -> {
      scan(getRandomString());
      nextToken();
    });
  }

@Test
public void invalidIntegerConstantOverflow() {
    assertThrows(InvalidNumericException.class, () -> {
        scan("9223372036854775807");  // Número que excede Integer.MAX_VALUE
        nextToken();
    });
}

  @Test
  public void assignmentWithExpressions() throws Exception {
    scan("c=d*(e-21)/4");
    assertThat(nextToken()).isEqualTo(ParserSym.IDENTIFIER);
    assertThat(nextToken()).isEqualTo(ParserSym.ASSIG);
    assertThat(nextToken()).isEqualTo(ParserSym.IDENTIFIER);
    assertThat(nextToken()).isEqualTo(ParserSym.MULT);
    assertThat(nextToken()).isEqualTo(ParserSym.OPEN_BRACKET);
    assertThat(nextToken()).isEqualTo(ParserSym.IDENTIFIER);
    assertThat(nextToken()).isEqualTo(ParserSym.SUB);
    assertThat(nextToken()).isEqualTo(ParserSym.INTEGER_CONSTANT);
    assertThat(nextToken()).isEqualTo(ParserSym.CLOSE_BRACKET);
    assertThat(nextToken()).isEqualTo(ParserSym.DIV);
    assertThat(nextToken()).isEqualTo(ParserSym.INTEGER_CONSTANT);
    assertThat(nextToken()).isEqualTo(ParserSym.EOF);
  }

  @Test
  public void unknownCharacter() {
    assertThrows(UnknownCharacterException.class, () -> {
      scan("#");
      nextToken();
    });
  }

  /* === Tests nuevos para tokens añadidos === */

  @Test
  public void reservedWords() throws Exception {
    scan("Integer Boolean DateConverted");
    //assertThat(nextToken()).isEqualTo(ParserSym.INTEGER);
    assertThat(nextToken()).isEqualTo(ParserSym.BOOLEAN);
    //assertThat(nextToken()).isEqualTo(ParserSym.DATECONVERTED);
    assertThat(nextToken()).isEqualTo(ParserSym.EOF);
  }

  @Test
  public void punctuationTokens() throws Exception {
    scan(";,");
    //assertThat(nextToken()).isEqualTo(ParserSym.SEMI);
    assertThat(nextToken()).isEqualTo(ParserSym.COMMA);
    assertThat(nextToken()).isEqualTo(ParserSym.EOF);
  }

  @Test
  public void mixedAssignment() throws Exception {
    scan("x=123;");
    assertThat(nextToken()).isEqualTo(ParserSym.IDENTIFIER);
    assertThat(nextToken()).isEqualTo(ParserSym.ASSIG);
    assertThat(nextToken()).isEqualTo(ParserSym.INTEGER_CONSTANT);
    //assertThat(nextToken()).isEqualTo(ParserSym.SEMI);
    assertThat(nextToken()).isEqualTo(ParserSym.EOF);
  }

  @Test
  public void dateLikeInteger() throws Exception {
    scan("d=20250821;");
    assertThat(nextToken()).isEqualTo(ParserSym.IDENTIFIER);
    assertThat(nextToken()).isEqualTo(ParserSym.ASSIG);
    assertThat(nextToken()).isEqualTo(ParserSym.INTEGER_CONSTANT);
    //assertThat(nextToken()).isEqualTo(ParserSym.SEMI);
    assertThat(nextToken()).isEqualTo(ParserSym.EOF);
  }

  /* === Helpers === */

  @AfterEach
  public void resetLexer() {
    lexer = null;
  }

  private void scan(String input) {
    lexer = LexerFactory.create(input);
  }

  private int nextToken() throws IOException, CompilerException {
    return lexer.next_token().sym;
  }

  private static String getRandomString() {
    return new RandomStringGenerator.Builder()
            .filteredBy(CharacterPredicates.LETTERS)
            .withinRange('a', 'z')
            .build().generate(MAX_LENGTH * 2);
  }

}
