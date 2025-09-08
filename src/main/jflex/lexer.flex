package lyc.compiler;

import java_cup.runtime.Symbol;
import lyc.compiler.ParserSym;
import lyc.compiler.model.*;
import static lyc.compiler.constants.Constants.*;

%%

%public
%class Lexer
%unicode
%cup
%line
%column
%throws CompilerException
%eofval{
  return symbol(ParserSym.EOF);
%eofval}

%{
  private Symbol symbol(int type) {
      return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
      return new Symbol(type, yyline, yycolumn, value);
  }
%}

/* === Expresiones regulares === */
LineTerminator = \r|\n|\r\n
Whitespace     = [ \t\f]
Letter         = [a-zA-Z]
Digit          = [0-9]

Identifier     = {Letter}({Letter}|{Digit})*
IntegerConst   = -?{Digit}+
StringLiteral  = \"([^\"\\r\\n]|\\.)*\"  // acepta escapes simples y largos

%%

/* === Palabras reservadas === */
"Integer"        { return symbol(ParserSym.INTEGER); }
"Boolean"        { return symbol(ParserSym.BOOLEAN); }
"DateConverted"  { return symbol(ParserSym.DATECONVERTED); }

/* === Operadores y símbolos === */
"="   { return symbol(ParserSym.ASSIG); }
"+"   { return symbol(ParserSym.PLUS); }
"-"   { return symbol(ParserSym.SUB); }
"*"   { return symbol(ParserSym.MULT); }
"/"   { return symbol(ParserSym.DIV); }
"("   { return symbol(ParserSym.OPEN_BRACKET); }
")"   { return symbol(ParserSym.CLOSE_BRACKET); }
","   { return symbol(ParserSym.COMMA); }
";"   { return symbol(ParserSym.SEMI); }

/* === Identificadores === */
{Identifier} {
    if (yytext().length() > MAX_LENGTH) {
        throw new InvalidLengthException("Identificador demasiado largo: " + yytext());
    }
    return symbol(ParserSym.IDENTIFIER, yytext());
}

/* === Constantes enteras (positivas) === */
{IntegerConst} {
    try {
        long value = Long.parseLong(yytext());
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new InvalidIntegerException("Constante fuera de rango: " + yytext());
        }
        return symbol(ParserSym.INTEGER_CONSTANT, (int)value);
    } catch (NumberFormatException ex) {
        throw new InvalidIntegerException("Constante inválida: " + yytext());
    }
}


/* === Constantes string === */
{StringLiteral} {
    String raw = yytext();
    String value = raw.substring(1, raw.length()-1)
                      .replace("\\n","\n")
                      .replace("\\t","\t")
                      .replace("\\r","\r")
                      .replace("\\\"","\"")
                      .replace("\\\\","\\");
    if (value.length() > MAX_LENGTH) {
        throw new InvalidLengthException("String demasiado largo: " + value);
    }
    return symbol(ParserSym.STRING_CONSTANT, value);
}


/* === Whitespace y comentarios === */
{Whitespace}       { /* ignorar */ }
{LineTerminator}   { /* ignorar */ }
"//".*             { /* comentario de línea */ }
"/*"([^*]|\*+[^*/])*\*+"/"  { /* comentario multilinea */ }

/* === Caracteres desconocidos === */
[^] { throw new UnknownCharacterException(yytext()); }
