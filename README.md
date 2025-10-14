# Estructura General del Compilador en Java

1. Análisis Léxico (Lexer)
Encargado de reconocer tokens válidos y detectar errores léxicos.

Entrada: Código fuente como String o archivo.
Salida: Lista de tokens + errores léxicos.

Tokens a reconocer:

Palabras reservadas (long, if, read, etc.)
Identificadores válidos
Constantes (enteras, reales, cadenas, booleanas)
Operadores y símbolos (+, =, ;, etc.)
Comentarios (multilínea y de una línea)

2. Análisis Sintáctico (Parser)
Verifica que los tokens estén organizados según las reglas gramaticales.

Entrada: Lista de tokens del lexer.
Salida: Árbol de sintaxis (opcional) + errores sintácticos.

Construcciones a reconocer:

Declaraciones de variables
Asignaciones
Entrada/salida (read, write)
Control de flujo (if, then, else, while)
Agrupaciones con {} y ()

3. Tabla de Símbolos
Registra información sobre variables y su contexto.

Campos: nombre, tipo, valor, ámbito, línea de declaración.
Estructura sugerida: HashMap<String, Symbol> donde Symbol es una clase con los campos mencionados.

4. Manejo de Errores
Debe reportar errores léxicos y sintácticos con:

Tipo de error
Línea y columna
Descripción clara
