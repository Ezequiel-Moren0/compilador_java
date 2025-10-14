package parser;

import lexer.*;
import simbolos.*;
import errores.*;

public class Parser {
    private Lexer lexer;
    private Token tokenActual;
    private TablaSimbolos tabla;
    private ErrorHandler errores;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.tabla = new TablaSimbolos();
        this.errores = new ErrorHandler();
        avanzar(); // inicializa tokenActual
    }

    private void avanzar() {
        tokenActual = lexer.siguienteToken();
        // Si tokenActual es ERROR, reportar inmediatamente
        if (tokenActual.getType() == TokenType.ERROR) {
            errores.reportar("Error léxico", tokenActual.getLexeme(), tokenActual.getLine());
        }
    }

    private void errorSintactico(String mensaje) {
        errores.reportar("Error sintáctico", mensaje, tokenActual.getLine());
        // intentar sincronizar: consumir hasta ;
        while (tokenActual.getType() != TokenType.SEMICOLON && tokenActual.getType() != TokenType.EOF) {
            avanzar();
        }
        if (tokenActual.getType() == TokenType.SEMICOLON) avanzar();
    }

    public void programa() {
        // permite declaraciones globales y/o main()
        while (tokenActual.getType() != TokenType.EOF) {
            if (tokenActual.getType() == TokenType.MAIN || (tokenActual.getType() == TokenType.ID && tokenActual.getLexeme().equals("main"))) {
                mainBloque();
            } else {
                // admitimos declaraciones globales o sentencias sueltas
                if (tokenActual.getType() == TokenType.LONG || tokenActual.getType() == TokenType.DOUBLE || tokenActual.getType() == TokenType.CONST) {
                    declaracionGlobal();
                } else {
                    sentencia();
                }
            }
        }
        tabla.mostrar();
    }

    // -----------------------
    // Declaración global / local
    // -----------------------
    private void declaracionGlobal() {
        boolean esConst = false;
        if (tokenActual.getType() == TokenType.CONST) {
            esConst = true;
            avanzar();
        }

        if (tokenActual.getType() == TokenType.LONG || tokenActual.getType() == TokenType.DOUBLE) {
            String tipo = tokenActual.getLexeme(); // "long" o "double"
            avanzar();
            if (tokenActual.getType() == TokenType.ID) {
                String nombre = tokenActual.getLexeme();
                avanzar();

                // Arreglo opcional: [NUM_INT]
                if (tokenActual.getType() == TokenType.LBRACKET) {
                    avanzar();
                    if (tokenActual.getType() == TokenType.NUM_INT) {
                        String tamaño = tokenActual.getLexeme();
                        avanzar();
                        if (tokenActual.getType() == TokenType.RBRACKET) {
                            avanzar();
                            tipo = tipo + "[]";
                        } else {
                            errorSintactico("Se esperaba ']' después del tamaño del arreglo");
                        }
                    } else {
                        errorSintactico("Se esperaba número entero dentro del arreglo");
                    }
                }

                // Asignación opcional en la declaración
                if (tokenActual.getType() == TokenType.ASSIGN) {
                    avanzar();
                    String tipoExpr = expresionTipo();
                    // chequeo semántico simple
                    if (!tipoCompatible(tipo, tipoExpr)) {
                        errores.reportar("Error semántico", "tipo incompatible en inicialización de " + nombre + ": esperado " + tipo + " pero se obtuvo " + tipoExpr, tokenActual.getLine());
                    }
                }

                // agregar a tabla
                tabla.agregar(nombre, (esConst ? tipo + " (const)" : tipo), "-", "global", tokenActual.getLine());

                if (tokenActual.getType() == TokenType.SEMICOLON) {
                    avanzar();
                } else {
                    errorSintactico("Se esperaba ';' al final de la declaración");
                }
            } else {
                errorSintactico("Se esperaba identificador después del tipo");
            }
        } else {
            errorSintactico("Se esperaba tipo 'long' o 'double' en declaración");
        }
    }

    // -----------------------
    // Main
    // -----------------------
    private void mainBloque() {
        // puede venir como token MAIN (por palabra reservada) o como ID "main"
        if (tokenActual.getType() == TokenType.MAIN || (tokenActual.getType() == TokenType.ID && tokenActual.getLexeme().equals("main"))) {
            avanzar();
            if (tokenActual.getType() == TokenType.LPAREN) {
                avanzar();
                if (tokenActual.getType() == TokenType.RPAREN) {
                    avanzar();
                    if (tokenActual.getType() == TokenType.LBRACE) {
                        avanzar();
                        while (tokenActual.getType() != TokenType.RBRACE && tokenActual.getType() != TokenType.EOF) {
                            sentencia();
                        }
                        if (tokenActual.getType() == TokenType.RBRACE) {
                            avanzar();
                        } else {
                            errorSintactico("Se esperaba '}' al final del bloque main");
                        }
                    } else errorSintactico("Se esperaba '{' después de main()");
                } else errorSintactico("Se esperaba ')' después de main(");
            } else errorSintactico("Se esperaba '(' después de main");
        } else {
            errorSintactico("Falta definición de main()");
        }
    }

    // -----------------------
    // Sentencias
    // -----------------------
    private void sentencia() {
        switch (tokenActual.getType()) {
            case LONG:
            case DOUBLE:
            case CONST:
                declaracionGlobal();
                break;
            case ID:
                // puede ser asignación o llamada (no implementadas llamadas a funciones explícitas)
                asignacion();
                break;
            case READ:
                entrada();
                break;
            case WRITE:
                salida();
                break;
            case IF:
                estructuraIf();
                break;
            case WHILE:
                estructuraWhile();
                break;
            case SEMICOLON:
                avanzar(); // instrucción vacía
                break;
            default:
                errorSintactico("sentencia no reconocida: " + tokenActual.getLexeme());
        }
    }

    // -----------------------
    // Asignación
    // -----------------------
    private void asignacion() {
        if (tokenActual.getType() != TokenType.ID) {
            errorSintactico("Se esperaba identificador en la asignación");
            return;
        }
        String nombre = tokenActual.getLexeme();
        if (!tabla.existe(nombre)) {
            errores.reportar("Error semántico", "variable no declarada: " + nombre, tokenActual.getLine());
        }
        avanzar();

        // Si viene un índice de arreglo: nombre[NUM]
        if (tokenActual.getType() == TokenType.LBRACKET) {
            avanzar();
            if (tokenActual.getType() == TokenType.NUM_INT) {
                avanzar();
                if (tokenActual.getType() == TokenType.RBRACKET) avanzar();
                else errorSintactico("Se esperaba ']' en índice de arreglo");
            } else errorSintactico("Se esperaba número entero en índice de arreglo");
        }

        // operator: = or compound (+=, -=, *=, /=)
        // operator: = or compound (+=, -=, *=, /=)
        TokenType op = tokenActual.getType();
        if (op == TokenType.ASSIGN || op == TokenType.PLUS_ASSIGN || op == TokenType.MINUS_ASSIGN
                || op == TokenType.MULT_ASSIGN || op == TokenType.DIV_ASSIGN) {
            avanzar();
            String tipoExpr = expresionTipo();

            // chequeo semántico básico
            Simbolo s = tabla.buscar(nombre);
            String tipoVar = (s != null) ? s.getTipo() : null;

            if (tipoVar != null) {
                String baseVar = tipoVar.replace(" (const)", "");

                // ✅ Si la variable fue accedida como arreglo (tiene corchetes), quitar los []
                if (baseVar.endsWith("[]")) {
                    baseVar = baseVar.substring(0, baseVar.length() - 2);
                }

                if (!tipoCompatible(baseVar, tipoExpr) && op == TokenType.ASSIGN) {
                    errores.reportar("Error semántico",
                            "tipo incompatible en asignación a " + nombre + ": esperado " + baseVar + " pero se obtuvo " + tipoExpr,
                            tokenActual.getLine());
                }
            }

            if (tokenActual.getType() == TokenType.SEMICOLON)
                avanzar();
            else
                errorSintactico("Se esperaba ';' al final de la asignación");
        } else {
            errorSintactico("Se esperaba operador de asignación '=' o '+=' '-=' '*=' '/='");
        }

    }

    // -----------------------
    // Entrada / Salida
    // -----------------------
    private void entrada() {
        avanzar(); // read
        if (tokenActual.getType() == TokenType.LPAREN) {
            avanzar();
            if (tokenActual.getType() == TokenType.ID) {
                // Chequear existencia opcional
                if (!tabla.existe(tokenActual.getLexeme()))
                    errores.reportar("Error semántico", "variable no declarada en read(): " + tokenActual.getLexeme(), tokenActual.getLine());
                avanzar();
                if (tokenActual.getType() == TokenType.RPAREN) {
                    avanzar();
                    if (tokenActual.getType() == TokenType.SEMICOLON) avanzar();
                    else errorSintactico("Se esperaba ';' al final de read()");
                } else errorSintactico("Se esperaba ')' en read()");
            } else errorSintactico("Se esperaba identificador en read()");
        } else errorSintactico("Se esperaba '(' después de read");
    }

    private void salida() {
        avanzar(); // write
        if (tokenActual.getType() == TokenType.LPAREN) {
            avanzar();
            expresionTipo();
            if (tokenActual.getType() == TokenType.RPAREN) {
                avanzar();
                if (tokenActual.getType() == TokenType.SEMICOLON) avanzar();
                else errorSintactico("Se esperaba ';' al final de write()");
            } else errorSintactico("Se esperaba ')' en write()");
        } else errorSintactico("Se esperaba '(' después de write");
    }

    // -----------------------
    // If / While
    // -----------------------
    private void estructuraIf() {
        avanzar(); // if
        if (tokenActual.getType() == TokenType.LPAREN) {
            avanzar();
            String tipoCond = expresionTipo();
            if (!tipoCond.equals("bool")) {
                errores.reportar("Error semántico", "condición de if no booleana", tokenActual.getLine());
            }
            if (tokenActual.getType() == TokenType.RPAREN) {
                avanzar();
                if (tokenActual.getType() == TokenType.THEN) {
                    avanzar();
                    sentencia();
                    if (tokenActual.getType() == TokenType.ELSE) {
                        avanzar();
                        sentencia();
                    }
                } else errorSintactico("Se esperaba 'then'");
            } else errorSintactico("Se esperaba ')'");
        } else errorSintactico("Se esperaba '(' después de if");
    }

    private void estructuraWhile() {
        avanzar(); // while
        if (tokenActual.getType() == TokenType.LPAREN) {
            avanzar();
            String tipoCond = expresionTipo();
            if (!tipoCond.equals("bool")) {
                errores.reportar("Error semántico", "condición de while no booleana", tokenActual.getLine());
            }
            if (tokenActual.getType() == TokenType.RPAREN) {
                avanzar();
                if (tokenActual.getType() == TokenType.LBRACE) {
                    avanzar();
                    while (tokenActual.getType() != TokenType.RBRACE && tokenActual.getType() != TokenType.EOF) {
                        sentencia();
                    }
                    if (tokenActual.getType() == TokenType.RBRACE) avanzar();
                    else errorSintactico("Se esperaba '}'");
                } else errorSintactico("Se esperaba '{'");
            } else errorSintactico("Se esperaba ')'");
        } else errorSintactico("Se esperaba '(' después de while");
    }

    // -----------------------
    // Expresiones (tipo)
    // Devuelve string representando el tipo resultante: "long", "double", "string", "bool"
    // -----------------------
    private String expresionTipo() {
        // Para simplificar: analizamos expresiones de forma recursiva,
        // devolviendo el tipo resultante básico.
        String tipo = terminoTipo();
        while (tokenActual.getType() == TokenType.PLUS || tokenActual.getType() == TokenType.MINUS ||
                tokenActual.getType() == TokenType.MULT || tokenActual.getType() == TokenType.DIV ||
                tokenActual.getType() == TokenType.AND || tokenActual.getType() == TokenType.OR ||
                tokenActual.getType() == TokenType.GT || tokenActual.getType() == TokenType.LT ||
                tokenActual.getType() == TokenType.GE || tokenActual.getType() == TokenType.LE ||
                tokenActual.getType() == TokenType.EQ || tokenActual.getType() == TokenType.NEQ) {
            TokenType op = tokenActual.getType();
            avanzar();
            String tipo2 = terminoTipo();
            tipo = combinarTipos(tipo, tipo2, op);
        }
        return tipo;
    }

    private String terminoTipo() {
        // factor [maybe unary not]
        if (tokenActual.getType() == TokenType.NOT) {
            avanzar();
            String t = terminoTipo();
            // not aplica a booleanos
            if (!t.equals("bool")) {
                errores.reportar("Error semántico", "operador '!' aplicado a tipo no booleano", tokenActual.getLine());
            }
            return "bool";
        }

        if (tokenActual.getType() == TokenType.LPAREN) {
            avanzar();
            String t = expresionTipo();
            if (tokenActual.getType() == TokenType.RPAREN) avanzar();
            else errorSintactico("Se esperaba ')' en expresión");
            return t;
        }

        Token t = tokenActual;
        switch (t.getType()) {
            case ID:
                String nombre = t.getLexeme();
                if (!tabla.existe(nombre)) {
                    errores.reportar("Error semántico", "variable no declarada en expresión: " + nombre, t.getLine());
                    avanzar();
                    return "long"; // fallback
                } else {
                    Simbolo s = tabla.buscar(nombre);
                    avanzar();
                    // si es arreglo con índice: nombre [ NUM ]
                    if (tokenActual.getType() == TokenType.LBRACKET) {
                        avanzar();
                        expresionTipo(); // simple chequeo índice
                        if (tokenActual.getType() == TokenType.RBRACKET) avanzar();
                        else errorSintactico("Se esperaba ']' en índice");
                        // devolver tipo base sin []
                        String ty = s.getTipo();
                        if (ty.endsWith("[]")) ty = ty.substring(0, ty.length()-2);
                        return normalizarTipo(ty);
                    }
                    return normalizarTipo(s.getTipo());
                }
            case NUM_INT:
                avanzar();
                return "long";
            case NUM_REAL:
                avanzar();
                return "double";
            case STRING:
                avanzar();
                return "string";
            case BOOL:
                avanzar();
                return "bool";
            default:
                errorSintactico("Expresión inválida o token inesperado: " + tokenActual.getLexeme());
                avanzar();
                return "long";
        }
    }

    // -----------------------
    // Combina tipos basados en operador
    // -----------------------
    private String combinarTipos(String t1, String t2, TokenType op) {
        // operadores lógicos producen bool
        if (op == TokenType.AND || op == TokenType.OR || op == TokenType.EQ || op == TokenType.NEQ ||
                op == TokenType.GT || op == TokenType.LT || op == TokenType.GE || op == TokenType.LE) {
            // comparaciones entre números o equality entre strings
            if ((esNumerico(t1) && esNumerico(t2)) || t1.equals(t2)) {
                return "bool";
            } else {
                errores.reportar("Error semántico", "operación inválida entre tipos " + t1 + " y " + t2, tokenActual.getLine());
                return "bool";
            }
        }

        // operadores aritméticos: + - * /
        if (op == TokenType.PLUS || op == TokenType.MINUS || op == TokenType.MULT || op == TokenType.DIV) {
            if (esNumerico(t1) && esNumerico(t2)) {
                if (t1.equals("double") || t2.equals("double")) return "double";
                return "long";
            } else {
                errores.reportar("Error semántico", "operador aritmético no válido para tipos " + t1 + " y " + t2, tokenActual.getLine());
                return "long";
            }
        }

        return "long";
    }

    private boolean esNumerico(String t) {
        return t.equals("long") || t.equals("double");
    }

    private boolean tipoCompatible(String tipoVar, String tipoExpr) {
        if (tipoVar == null) return false;
        tipoVar = normalizarTipo(tipoVar);
        tipoExpr = normalizarTipo(tipoExpr);
        if (tipoVar.equals(tipoExpr)) return true;
        if (tipoVar.equals("double") && tipoExpr.equals("long")) return true; // promocion implícita
        return false;
    }

    private String normalizarTipo(String t) {
        if (t == null) return "";
        t = t.replace(" (const)", "");
        return t;
    }
}
