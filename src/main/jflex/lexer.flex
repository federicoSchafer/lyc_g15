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
Identation     = [ \t\f]
Letter         = [a-zA-Z]
Digit          = [0-9]

Identifier     = {Letter}({Letter}|{Digit})*
IntegerConst   = {Digit}+

%%

/* === Palabras reservadas === */
"Integer"       { return symbol(ParserSym.INTEGER); }
"Boolean"       { return symbol(ParserSym.BOOLEAN); }
"DateConverted" { return symbol(ParserSym.DATECONVERTED); }

/* === Operadores y símbolos === */
"="     { return symbol(ParserSym.ASSIG); }
"+"     { return symbol(ParserSym.PLUS); }
"-"     { return symbol(ParserSym.SUB); }
"*"     { return symbol(ParserSym.MULT); }
"/"     { return symbol(ParserSym.DIV); }
"("     { return symbol(ParserSym.OPEN_BRACKET); }
")"     { return symbol(ParserSym.CLOSE_BRACKET); }
","     { return symbol(ParserSym.COMMA); }
";"     { return symbol(ParserSym.SEMI); }

/* === Identificadores y constantes === */
{Identifier}    { return symbol(ParserSym.IDENTIFIER, yytext()); }
{IntegerConst}  { return symbol(ParserSym.INTEGER_CONSTANT, Integer.parseInt(yytext())); }

/* === Espacios en blanco y comentarios === */
{Identation}      { /* ignorar */ }
{LineTerminator}  { /* ignorar */ }
"//".*            { /* comentario de línea */ }
"/*"([^*]|\*+[^*/])*\*+"/"   { /* comentario multilinea */ }

/* === Error === */
[^] { throw new UnknownCharacterException(yytext()); }
