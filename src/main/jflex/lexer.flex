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
IntegerConst   = {Digit}+
FloatConst     = ({Digit}+)?\.{Digit}+ 
StringLiteral  = \"([^\"\n\r\\]|\\.)*\"
Comment        = "#+"(.|\n)*?"+#"
%%

/* === Palabras reservadas === */
"Int"           { return symbol(ParserSym.INTEGER); }
"Boolean"       { return symbol(ParserSym.BOOLEAN); }
"Float"         { return symbol(ParserSym.FLOAT); }
"String"        { return symbol(ParserSym.STRING); }
"Init"          { return symbol(ParserSym.INIT); }
"while"          { return symbol(ParserSym.WHILE); }
"if"          { return symbol(ParserSym.IF); }
"else"          { return symbol(ParserSym.ELSE); }
"read"           { return symbol(ParserSym.READ); }
"write"          { return symbol(ParserSym.WRITE); }
"AND"            { return symbol(ParserSym.AND); }
"OR"             { return symbol(ParserSym.OR); }
"NOT"            { return symbol(ParserSym.NOT); }
"equalExpressions" { return symbol(ParserSym.EQUAL_EXP); }
"triangleAreaMaximum" { return symbol(ParserSym.TRIANG_AREA_MAX); }

/* === Operadores y símbolos === */
":="   { return symbol(ParserSym.ASSIG); }
"+"   { return symbol(ParserSym.PLUS); }
"-"   { return symbol(ParserSym.SUB); }
"*"   { return symbol(ParserSym.MULT); }
"/"   { return symbol(ParserSym.DIV); }
">"   { return symbol(ParserSym.LESS_THAN); }
"<"   { return symbol(ParserSym.GREATER_THAN); }
"<="  { return symbol(ParserSym.LESS_THAN_EQUAL); }
">="  { return symbol(ParserSym.GREATER_THAN_EQUAL); }
"=="  { return symbol(ParserSym.DOBLE_EQUAL); }
"("   { return symbol(ParserSym.OPEN_PARENTHESIS); }
")"   { return symbol(ParserSym.CLOSE_PARENTHESIS); }
"{"   { return symbol(ParserSym.OPEN_BRACE); }
"}"   { return symbol(ParserSym.CLOSE_BRACE); }
"["   { return symbol(ParserSym.OPEN_BRACKET); }
"]"   { return symbol(ParserSym.CLOSE_BRACKET); }
","   { return symbol(ParserSym.COMMA); }
";"   { return symbol(ParserSym.SEMI); }
":"   { return symbol(ParserSym.COLON); }
/* === Identificadores === */
{Identifier} {
    if (yytext().length() > MAX_LENGTH) {
        throw new InvalidLengthException("Identificador demasiado largo: " + yytext());
    }
    return symbol(ParserSym.IDENTIFIER, yytext());
}

/* === Constantes enteras === */
{IntegerConst} {
    try {
        long value = Long.parseLong(yytext());
        if (value > Integer.MAX_VALUE) {
            throw new InvalidIntegerException("Constante fuera de rango: " + yytext());
        }
        return symbol(ParserSym.INTEGER_CONSTANT, yytext());
    } catch (NumberFormatException ex) {
        throw new InvalidIntegerException("Constante inválida: " + yytext());
    }
}

/* === Constantes flotantes === */
{FloatConst} {
    try {
        double value = Double.parseDouble(yytext());
        if (value > Float.MAX_VALUE || value < -Float.MAX_VALUE) {
            throw new InvalidIntegerException("Constante flotante fuera de rango: " + yytext());
            //throw new InvalidFloatException("Constante flotante fuera de rango: " + yytext());
        }
        return symbol(ParserSym.FLOAT_CONSTANT, yytext());
    } catch (NumberFormatException ex) {
        throw new InvalidIntegerException("Constante flotante inválida: " + yytext());
        //throw new InvalidFloatException("Constante flotante inválida: " + yytext());
    }
}

/* === Constantes string === */
{StringLiteral} {
    String raw = yytext();
    String value = raw.substring(1, raw.length()-1)
                      .replace("\\n", "\n")
                      .replace("\\t", "\t")
                      .replace("\\r", "\r")
                      .replace("\\\"", "\"")
                      .replace("\\\\", "\\");
    
    if (value.length() > MAX_LENGTH) {
        throw new InvalidLengthException("String demasiado largo: " + value);
    }
    
    return symbol(ParserSym.STRING_CONSTANT, value);
}

/* === Whitespace y comentarios === */
{Whitespace}        { /* ignorar */ }
{LineTerminator}    { /* ignorar */ }
{Comment} 		    { /* ignorar */ }

/* === Caracteres desconocidos === */
[^] { throw new UnknownCharacterException(yytext()); }

