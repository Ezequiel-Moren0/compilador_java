package lexer;

public enum TokenType {
    // Palabras reservadas
    LONG, DOUBLE, IF, THEN, ELSE, WHILE, BREAK, READ, WRITE,
    CONST, TRUE, FALSE, MAIN,

    // Operadores aritméticos y de asignación
    PLUS, MINUS, MULT, DIV,
    PLUS_ASSIGN, MINUS_ASSIGN, MULT_ASSIGN, DIV_ASSIGN,

    // Operadores relacionales y lógicos
    GT, LT, GE, LE, EQ, NEQ, AND, OR, NOT,

    // Asignación simple y signos
    ASSIGN, SEMICOLON,
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET, COMMA,

    // Identificadores y constantes
    ID, NUM_INT, NUM_REAL, STRING, BOOL,

    COMMENT, EOF, ERROR
}
