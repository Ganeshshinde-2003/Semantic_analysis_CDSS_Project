package lexer;

import java.io.StringReader;
import java.io.IOException;

public class TestLexer {
    public static String analyzeFile(String fileContent) throws IOException {
        StringBuilder result = new StringBuilder();

        StringReader stringReader = new StringReader(fileContent);
        Lexer lexer = new Lexer(stringReader);

        result.append("Tokenizing file content...\n");
        long startTime = System.currentTimeMillis();
        int numTokens = 0;
        Token token;
        do {
            token = lexer.getToken();
            numTokens++;

            if (token.getType() == TokenType.UNKNOWN) {
                result.append(token.getType()).append(" (")
                        .append(token.getLineNumber()).append(",").append(token.getColumnNumber()).append(")\n");
                continue;
            }

            result.append(token.getType()).append(" (").append(token.getLineNumber()).append(",").append(token.getColumnNumber()).append(")");

            if (token.getType() == TokenType.ID)
                result.append(": ").append(token.getAttribute().getIdVal());
            else if (token.getType() == TokenType.INT_CONST)
                result.append(": ").append(token.getAttribute().getIntVal());
            else if (token.getType() == TokenType.FLOAT_CONST)
                result.append(": ").append(token.getAttribute().getFloatVal());
            else if (token.getType() == TokenType.CHAR_CONST)
                result.append(": ").append(token.getAttribute().getCharVal());
            else if (token.getType() == TokenType.BOOLEAN_CONST)
                result.append(": ").append(token.getAttribute().getBooleanVal());

            result.append("\n");
        } while (token.getType() != TokenType.EOF);

        long endTime = System.currentTimeMillis();

        result.append("---\n");
        result.append("Number of tokens: ").append(numTokens).append("\n");
        result.append("Execution time: ").append(endTime - startTime).append("ms\n");

        return result.toString();
    }
}
