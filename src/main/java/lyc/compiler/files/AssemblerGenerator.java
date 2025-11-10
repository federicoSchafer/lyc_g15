package lyc.compiler.files;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class AssemblerGenerator {
    private final Nodo raiz;
    private final SymbolTableGenerator symbolTable;
    private static int labelCounter = 0;
    private static final Set<String> dataVariablesAgregadas = new HashSet<>();
    private static Stack<String> labelElseStack =  new Stack<>();
    private static Stack<String> thenLabelStack =  new Stack<>();
    private static Stack<String> endLabelStack =  new Stack<>();

    public AssemblerGenerator(Nodo raiz, SymbolTableGenerator symbolTable) {
        this.raiz = raiz;
        this.symbolTable = symbolTable;
    }

    public String generate() {
        StringBuilder asm = new StringBuilder();

        asm.append(".MODEL LARGE\n");
        asm.append(".386\n");
        asm.append(".STACK 200h\n");
        StringBuilder dataCode = new StringBuilder(generateDataSection());

        // Generar el código de operaciones desde el árbol
        StringBuilder code = new StringBuilder(generateCodeFromNode(this.raiz, dataCode));

        asm.append(dataCode);
        asm.append(".CODE\n");
        asm.append("mov AX,@DATA\n");
        asm.append("mov DS,AX\n");
        asm.append(code);

        asm.append("mov ax,4c00h\n");
        asm.append("int 21h\n");
        asm.append("End\n");

        return asm.toString();
    }

    private static String generateCodeFromNode(Nodo node, StringBuilder dataCode) {
        if (node == null) return "";

        String valor = node.getValor();

        // Caso hoja (variable o constante)
        if (node.esHoja()) {
            if(valor.startsWith("@")){
                if (!dataVariablesAgregadas.contains(valor)) {
                    dataCode.append(String.format("%-15s dd ?\n", valor));
                    dataVariablesAgregadas.add(valor);
                }
                return String.format("fld %s\n", valor);
            }
            return String.format("fld _%s\n", valor);
        }

        if (node.tieneUnHijo()) {
            StringBuilder code = new StringBuilder();
            code.append(generateCodeFromNode(node.getIzquierdo(), dataCode));

            switch (valor) {
                case "-":
                    code.append("fchs\n");
                    break;
                case "+": // no hace nada
                    break;
                case "NOT":
                    code.append("; Negación lógica\n");
                    code.append("fldz\n");
                    code.append("fcomp\n");
                    code.append("fstsw ax\n");
                    code.append("sahf\n");
                    code.append("jne");
                    break;
                case "Read":
                    String varName = node.getIzquierdo().getValor();
                    code.append("; READ ").append(varName).append("\n");
                    code.append("push ").append(varName).append("\n");
                    code.append("push fmtIn\n");
                    code.append("call scanf\n");
                    code.append("add esp, 8\n");
                    break;
                case "Write":
                    code.append("; WRITE expresión\n");
                    code.append(generateCodeFromNode(node.getIzquierdo(), dataCode)); // genera el valor en ST(0)
                    code.append("fstp temp\n"); // guarda el valor en una variable temporal
                    code.append("push temp\n");
                    code.append("push fmtOut\n");
                    code.append("call printf\n");
                    code.append("add esp, 8\n");
                    break;
                default:
                    System.err.println("Operador unario desconocido: " + valor);
            }
            return code.toString();
        }

        Nodo izq = node.getIzquierdo();
        Nodo der = node.getDerecho();

        StringBuilder code = new StringBuilder();

        switch (valor) {
            case "=": {
                code.append(generateCodeFromNode(der, dataCode));
                code.append(String.format("fstp %s\n", izq.getValor()));
                break;
            }
            case "+": {
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fadd\n");
                break;
            }
            case "-": {
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fsub\n");
                break;
            }
            case "*": {
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fmul\n");
                break;
            }
            case "/": {
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fdiv\n");
                break;
            }
            case "<":{
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fxch\n");
                code.append("fcomp\n");
                code.append("fstsw ax\n");
                code.append("sahf\n");
                code.append("jge ");
                break;
            }
            case ">":{
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fxch\n");
                code.append("fcomp\n");
                code.append("fstsw ax\n");
                code.append("sahf\n");
                code.append("jle ");
                break;
            }
            case "<=":{
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fxch\n");
                code.append("fcomp\n");
                code.append("fstsw ax\n");
                code.append("sahf\n");
                code.append("jg ");
                break;
            }
            case ">=":{
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fxch\n");
                code.append("fcomp\n");
                code.append("fstsw ax\n");
                code.append("sahf\n");
                code.append("jl ");
                break;
            }
            case "==":{
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fxch\n");
                code.append("fcomp\n");
                code.append("fstsw ax\n");
                code.append("sahf\n");
                code.append("jne ");
                break;
            }
            case "!=":{
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                code.append("fxch\n");
                code.append("fcomp\n");
                code.append("fstsw ax\n");
                code.append("sahf\n");
                code.append("je ");
                break;
            }
            case "OR": {
                code.append(generateCodeFromNode(izq, dataCode));
                String fullText = code.toString();
                int lastNewline = fullText.lastIndexOf('\n');
                String lastLine;
                if (lastNewline != -1) {
                    lastLine = fullText.substring(lastNewline + 1);
                    code.delete(lastNewline + 1, code.length());
                } else {
                    lastLine = fullText;
                    code.setLength(0);
                }
                switch (lastLine) {
                    case "jne ": {
                        code.append("je ");
                        break;
                    }
                    case "je ":{
                        code.append("jne ");
                        break;
                    }
                    case "jl ": {
                        code.append("jge ");
                        break;
                    }
                    case "jg ": {
                        code.append("jle ");
                        break;
                    }
                    case "jge ": {
                        code.append("jl ");
                        break;
                    }
                    case "jle ": {
                        code.append("jg ");
                        break;
                    }
                }
                code.append(thenLabelStack.pop()).append("\n");
                code.append(generateCodeFromNode(der, dataCode));
                break;
            }
            case "AND": {
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(endLabelStack.pop()).append("\n");
                code.append(generateCodeFromNode(der, dataCode));
                break;
            }
            case "IF": {
                String thenLabel = "";
                String elseLabel = "";
                String endIfLabel = "";
                if(izq.getValor().equals("OR")) {
                    thenLabel = newLabel("Then");
                    thenLabelStack.push(thenLabel);
                }
                else if (izq.getValor().equals("AND")){
                    if(der.getValor().equals("Cuerpo")){
                        elseLabel = newLabel("Else");
                        endLabelStack.push(elseLabel);
                    }
                    else{
                        endIfLabel = newLabel("EndIf");
                        endLabelStack.push(endIfLabel);
                    }
                }

                code.append(generateCodeFromNode(izq, dataCode));
                if(!der.getValor().equals("Cuerpo")) {
                    if(endIfLabel.isEmpty()){
                        endIfLabel = newLabel("EndIf");
                    }
                    if(thenLabel.isEmpty()) {
                        thenLabel = newLabel("Then");
                    }
                    code.append(endIfLabel).append("\n");
                    code.append(thenLabel).append(":\n");
                    code.append(generateCodeFromNode(der, dataCode));
                    code.append(endIfLabel).append(":\n");
                }
                else{
                    if(elseLabel.isEmpty()){
                        elseLabel = newLabel("Else");
                    }
                    if(thenLabel.isEmpty()){
                        thenLabel = newLabel("Then");
                    }
                    code.append(elseLabel).append("\n");
                    labelElseStack.push(elseLabel);
                    code.append(thenLabel).append(":\n");
                    code.append(generateCodeFromNode(der, dataCode));
                }
                break;
            }
            case "Cuerpo":{
                code.append(generateCodeFromNode(izq, dataCode));
                String endIfLabel = newLabel("EndIf");
                code.append("jmp ").append(endIfLabel).append("\n");
                code.append(labelElseStack.pop()).append(":\n");
                code.append(generateCodeFromNode(der, dataCode));
                code.append(endIfLabel).append(":\n");
                break;
            }
            case "WHILE": {
                String startWhileLabel = newLabel("While");
                String thenLabel = newLabel("Then");
                String endWhileLabel = newLabel("EndWhile");
                if(izq.getValor().equals("OR")){
                    thenLabelStack.push(thenLabel);
                }
                if(izq.getValor().equals("AND")){
                    endLabelStack.push(endWhileLabel);
                }
                code.append(startWhileLabel).append(":\n");
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(endWhileLabel).append("\n");
                code.append(thenLabel).append(":\n");
                code.append(generateCodeFromNode(der, dataCode));
                code.append("jmp ").append(startWhileLabel).append("\n");
                code.append(endWhileLabel).append(":\n");
                break;
            }
            case "EqualExpr":
            case "TriangleAreaMaximum":
            case "Ar":
            case "I":
            case "S":
            case "E": {
                code.append(generateCodeFromNode(izq, dataCode));
                code.append(generateCodeFromNode(der, dataCode));
                break;
            }
            default:
                System.err.println("Operador desconocido: " + valor);
        }

        return code.toString();
    }

    private String generateDataSection() {
        StringBuilder data = new StringBuilder();
        data.append(".DATA\n");

        for (SymbolTableGenerator.SymbolEntry entry : symbolTable.getEntries()) {
            String name = entry.getName();
            String value = entry.getValue();

            // Si el valor está vacío es una variable
            if (value == null || value.isEmpty()) {
                data.append(String.format("%-15s dd ?\n", name));
            } else {
                // Si tiene valor es una constante
                data.append(String.format("%-15s dd %s\n", name, value));
            }
        }

        return data.toString();
    }

    private static String newLabel(String name){
        String label = name + "_" + labelCounter;
        labelCounter ++;
        return label;
    }
}
