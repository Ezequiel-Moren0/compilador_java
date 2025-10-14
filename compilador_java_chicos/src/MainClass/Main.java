package MainClass;

import lexer.Lexer;
import parser.Parser;

public class Main {
    public static void main(String[] args) {
        String archivo = "src/MainClass/programa.txt";
        if (args.length > 0) archivo = args[0];

        Lexer lexer = new Lexer(archivo);
        Parser parser = new Parser(lexer);
        parser.programa();

        System.out.println("\nCompilación finalizada ✅");
    }
}
