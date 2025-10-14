package lexer;

import java.io.*;
import java.util.*;

public class Lexer {
    private BufferedReader reader;
    private int line = 1;
    private int currentChar;

    private static final Set<String> palabrasReservadas = new HashSet<>(Arrays.asList(
            "long", "double", "if", "then", "else", "while", "break", "read", "write",
            "const", "true", "false", "main"
    ));

    public Lexer(String rutaArchivo) {
        try {
            reader = new BufferedReader(new FileReader(rutaArchivo));
            reader.mark(1);
            currentChar = reader.read();
        } catch (IOException e) {
            System.err.println("Error al abrir el archivo: " + e.getMessage());
            currentChar = -1;
        }
    }

    private void avanzar() throws IOException {
        currentChar = reader.read();
    }

    private boolean esLetra(int c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean esDigito(int c) {
        return Character.isDigit(c);
    }

    public Token siguienteToken() {
        try {
            while (currentChar != -1) {
                // Espacios y saltos de línea
                if (Character.isWhitespace(currentChar)) {
                    if (currentChar == '\n') line++;
                    avanzar();
                    continue;
                }

                // Comentarios y '/'
                if (currentChar == '/') {
                    avanzar();
                    if (currentChar == '/') { // comentario de una línea
                        while (currentChar != '\n' && currentChar != -1) avanzar();
                        continue;
                    } else if (currentChar == '*') { // comentario multilínea (anidado)
                        int nivel = 1;
                        while ((currentChar = reader.read()) != -1) {
                            if (currentChar == '\n') line++;
                            if (currentChar == '/') {
                                reader.mark(1);
                                int next = reader.read();
                                if (next == '*') {
                                    nivel++;
                                } else {
                                    reader.reset();
                                }
                            } else if (currentChar == '*') {
                                reader.mark(1);
                                int next = reader.read();
                                if (next == '/') {
                                    nivel--;
                                    if (nivel == 0) {
                                        avanzar();
                                        break;
                                    }
                                } else {
                                    reader.reset();
                                }
                            }
                        }
                        if (nivel > 0)
                            return new Token(TokenType.ERROR, "comentario sin cierre", line, 0);
                        continue;
                    } else if (currentChar == '=') { // /=
                        avanzar();
                        return new Token(TokenType.DIV_ASSIGN, "/=", line, 0);
                    } else {
                        return new Token(TokenType.DIV, "/", line, 0);
                    }
                }

                // Identificadores o palabras reservadas
                if (esLetra(currentChar)) {
                    StringBuilder sb = new StringBuilder();
                    do {
                        sb.append((char) currentChar);
                        avanzar();
                    } while (esLetra(currentChar) || esDigito(currentChar));

                    String lexema = sb.toString();
                    if (palabrasReservadas.contains(lexema)) {
                        if (lexema.equals("true") || lexema.equals("false"))
                            return new Token(TokenType.BOOL, lexema, line, 0);
                        return new Token(TokenType.valueOf(lexema.toUpperCase()), lexema, line, 0);
                    } else
                        return new Token(TokenType.ID, lexema, line, 0);
                }

                // Números (enteros o reales)
                if (esDigito(currentChar)) {
                    StringBuilder sb = new StringBuilder();
                    boolean esReal = false;
                    while (esDigito(currentChar)) {
                        sb.append((char) currentChar);
                        avanzar();
                    }
                    if (currentChar == '.') {
                        esReal = true;
                        sb.append('.');
                        avanzar();
                        if (!esDigito(currentChar)) {
                            return new Token(TokenType.ERROR, "formato de número real inválido", line, 0);
                        }
                        while (esDigito(currentChar)) {
                            sb.append((char) currentChar);
                            avanzar();
                        }
                    }
                    return new Token(esReal ? TokenType.NUM_REAL : TokenType.NUM_INT, sb.toString(), line, 0);
                }

                // Cadenas
                if (currentChar == '"') {
                    StringBuilder sb = new StringBuilder();
                    avanzar();
                    while (currentChar != '"' && currentChar != -1) {
                        if (currentChar == '\n') line++;
                        sb.append((char) currentChar);
                        avanzar();
                    }
                    if (currentChar == '"') {
                        avanzar();
                        return new Token(TokenType.STRING, sb.toString(), line, 0);
                    } else {
                        return new Token(TokenType.ERROR, "cadena sin cierre", line, 0);
                    }
                }

                // Operadores y signos
                switch (currentChar) {
                    case '+':
                        avanzar();
                        if (currentChar == '=') { avanzar(); return new Token(TokenType.PLUS_ASSIGN, "+=", line, 0); }
                        return new Token(TokenType.PLUS, "+", line, 0);
                    case '-':
                        avanzar();
                        if (currentChar == '=') { avanzar(); return new Token(TokenType.MINUS_ASSIGN, "-=", line, 0); }
                        return new Token(TokenType.MINUS, "-", line, 0);
                    case '*':
                        avanzar();
                        if (currentChar == '=') { avanzar(); return new Token(TokenType.MULT_ASSIGN, "*=", line, 0); }
                        return new Token(TokenType.MULT, "*", line, 0);
                    case '>':
                        avanzar();
                        if (currentChar == '=') { avanzar(); return new Token(TokenType.GE, ">=", line, 0); }
                        return new Token(TokenType.GT, ">", line, 0);
                    case '<':
                        avanzar();
                        if (currentChar == '=') { avanzar(); return new Token(TokenType.LE, "<=", line, 0); }
                        if (currentChar == '>') { avanzar(); return new Token(TokenType.NEQ, "<>", line, 0); }
                        return new Token(TokenType.LT, "<", line, 0);
                    case '=':
                        avanzar();
                        if (currentChar == '=') { avanzar(); return new Token(TokenType.EQ, "==", line, 0); }
                        return new Token(TokenType.ASSIGN, "=", line, 0);
                    case '!':
                        avanzar();
                        if (currentChar == '=') { avanzar(); return new Token(TokenType.NEQ, "!=", line, 0); }
                        return new Token(TokenType.NOT, "!", line, 0);
                    case '&':
                        avanzar();
                        if (currentChar == '&') { avanzar(); return new Token(TokenType.AND, "&&", line, 0); }
                        else return new Token(TokenType.ERROR, "símbolo '&' inválido, ¿quiziste '&&'?", line, 0);
                    case '|':
                        avanzar();
                        if (currentChar == '|') { avanzar(); return new Token(TokenType.OR, "||", line, 0); }
                        else return new Token(TokenType.ERROR, "símbolo '|' inválido, ¿quiziste '||'?", line, 0);
                    case ';': avanzar(); return new Token(TokenType.SEMICOLON, ";", line, 0);
                    case '(': avanzar(); return new Token(TokenType.LPAREN, "(", line, 0);
                    case ')': avanzar(); return new Token(TokenType.RPAREN, ")", line, 0);
                    case '{': avanzar(); return new Token(TokenType.LBRACE, "{", line, 0);
                    case '}': avanzar(); return new Token(TokenType.RBRACE, "}", line, 0);
                    case '[': avanzar(); return new Token(TokenType.LBRACKET, "[", line, 0);
                    case ']': avanzar(); return new Token(TokenType.RBRACKET, "]", line, 0);
                    case ',': avanzar(); return new Token(TokenType.COMMA, ",", line, 0);

                    default:
                        char invalido = (char) currentChar;
                        avanzar();
                        return new Token(TokenType.ERROR, "símbolo no reconocido: " + invalido, line, 0);
                }
            }
            return new Token(TokenType.EOF, "EOF", line, 0);
        } catch (IOException e) {
            return new Token(TokenType.ERROR, "error de lectura: " + e.getMessage(), line, 0);
        }
    }
}
