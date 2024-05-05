package parser;

import java.io.IOException;
import java.io.StringReader;

import ast.Program;
import visitor.PrintVisitor;

public class TestParser {
    public static String performParsing(String fileContent) throws IOException {
        try (StringReader reader = new StringReader(fileContent)) {
            Parser parser = new Parser(fileContent);
            Program program = parser.parseProgram();

            long startTime = System.currentTimeMillis();
            long endTime;

            endTime = System.currentTimeMillis();
            String result = "File has finished parsing!\n" +
                            "Execution time: " + (endTime - startTime) + "ms\n" +
                            parser.getErrors() + " errors reported\n" + parser.getErrorMessage() + "\n\n";

            // print out AST
            PrintVisitor printer = new PrintVisitor();
            printer.visit(program);

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred while parsing the file.";
        }
    }
}
