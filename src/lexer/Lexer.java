package lexer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private BufferedReader stream;
    private Token nextToken;
    private int nextChar;
    private int lineNumber = 1;
    private int columnNumber = 1;

    private final static Map<String, TokenType> reservedWords;
    private final static Map<Character, TokenType> punctuation;
    private final static Map<String, TokenType> operators;
    private int errors;

    static {
        reservedWords = new HashMap<String, TokenType>();
        reservedWords.put("int", TokenType.INT);
        reservedWords.put("float", TokenType.FLOAT);
        reservedWords.put("char", TokenType.CHAR);
        reservedWords.put("boolean", TokenType.BOOLEAN);
        reservedWords.put("if", TokenType.IF);
        reservedWords.put("else", TokenType.ELSE);
        reservedWords.put("while", TokenType.WHILE);
        reservedWords.put("main", TokenType.MAIN);

        punctuation = new HashMap<Character, TokenType>();
        punctuation.put('(', TokenType.LPAREN);
        punctuation.put(')', TokenType.RPAREN);
        punctuation.put('[', TokenType.LBRACKET);
        punctuation.put(']', TokenType.RBRACKET);
        punctuation.put('{', TokenType.LBRACE);
        punctuation.put('}', TokenType.RBRACE);
        punctuation.put(';', TokenType.SEMI);
        punctuation.put(',', TokenType.COMMA);
        punctuation.put('=', TokenType.ASSIGN);
        punctuation.put('-', TokenType.NEGATIVE);
        punctuation.put('!', TokenType.NOT);

        operators = new HashMap<String, TokenType>();
        operators.put("&&", TokenType.AND);
        operators.put("||", TokenType.OR);
        operators.put("==", TokenType.EQ);
        operators.put("!=", TokenType.NEQ);
        operators.put("<", TokenType.LT);
        operators.put(">", TokenType.RT);
        operators.put("<=", TokenType.LT_EQ);
        operators.put(">=", TokenType.RT_EQ);
        operators.put("+", TokenType.PLUS);
        operators.put("-", TokenType.MINUS);
        operators.put("*", TokenType.TIMES);
        operators.put("/", TokenType.DIV);
        operators.put("%", TokenType.MOD);
    }

    public Lexer(Reader reader) {
        this.stream = new BufferedReader(reader);
        nextChar = getChar();
    }

    public Lexer(String content) {
        this(new StringReader(content));
    }

    public int getErrors() {
        return errors;
    }

    private int getChar() {
        try {
            return stream.read();
        } catch (IOException e) {
            System.err.print(e.getMessage());
            System.err.println("IOException occurred in Lexer::getChar()");
            return -1;
        }
    }

    private boolean skipNewline() {
        if (nextChar == '\n') {
            lineNumber++;
            columnNumber = 1;
            nextChar = getChar();
            return true;
        }
        if (nextChar == '\r') {
            lineNumber++;
            columnNumber = 1;
            nextChar = getChar();

            if (nextChar == '\n')
                nextChar = getChar();
            return true;
        }
        return false;
    }

    public Token peek() throws IOException {
        if (nextToken == null)
            nextToken = getToken();

        return nextToken;
    }

    public Token getToken() throws IOException {
        if (nextToken != null) {
            Token token = nextToken;
            nextToken = null;
            return token;
        }

        while (Character.isWhitespace(nextChar)) {
            if (!skipNewline()) {
                columnNumber++;
                nextChar = getChar();
            }

            if (nextChar == '\t')
                columnNumber += 3;
        }

        if (Character.isLetter(nextChar)) {
            String current = Character.toString((char) nextChar);
            columnNumber++;
            nextChar = getChar();

            while (Character.isLetterOrDigit(nextChar)) {
                current += (char) nextChar;
                columnNumber++;
                nextChar = getChar();
            }

            TokenType type = reservedWords.get(current);

            if (type != null)
                return new Token(type, new TokenAttribute(), lineNumber, columnNumber - current.length());

            if (current.equals("true"))
                return new Token(TokenType.BOOLEAN_CONST, new TokenAttribute(true), lineNumber, columnNumber - current.length());
            else if (current.equals("false"))
                return new Token(TokenType.BOOLEAN_CONST, new TokenAttribute(false), lineNumber, columnNumber - current.length());

            return new Token(TokenType.ID, new TokenAttribute(current), lineNumber, columnNumber - current.length());
        }

        if (Character.isDigit(nextChar)) {
            String numString = Character.toString((char) nextChar);
            columnNumber++;
            nextChar = getChar();

            while (Character.isDigit(nextChar)) {
                numString += (char) nextChar;
                columnNumber++;
                nextChar = getChar();
            }

            if (nextChar == '.') {
                nextChar = getChar();
                columnNumber++;

                if (Character.isDigit(nextChar)) {
                    numString += '.';
                    while (Character.isDigit(nextChar)) {
                        numString += (char) nextChar;
                        columnNumber++;
                        nextChar = getChar();
                    }

                    return new Token(TokenType.FLOAT_CONST, new TokenAttribute(Float.parseFloat(numString)), lineNumber, columnNumber - numString.length());
                }

                while (!Character.isWhitespace(nextChar)) {
                    columnNumber++;
                    numString += nextChar;
                    nextChar = getChar();
                }

                return new Token(TokenType.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - numString.length() + 1);
            }

            return new Token(TokenType.INT_CONST, new TokenAttribute(Integer.parseInt(numString)), lineNumber, columnNumber - numString.length());
        }

        if (nextChar == '\'') {
            nextChar = getChar();
            columnNumber++;
            if (Character.isAlphabetic(nextChar)) {
                char current = (char) nextChar;
                stream.mark(0);
                nextChar = getChar();
                columnNumber++;

                if (nextChar == '\'') {
                    nextChar = getChar();
                    columnNumber++;
                    return new Token(TokenType.CHAR_CONST, new TokenAttribute(current), lineNumber, columnNumber - 1);
                }
                stream.reset();
            }

            return new Token(TokenType.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);
        }

        if (nextChar == -1)
            return new Token(TokenType.EOF, new TokenAttribute(), lineNumber, columnNumber);

        switch (nextChar) {
            case '&':
                columnNumber++;
                nextChar = getChar();

                if (nextChar == '&') {
                    nextChar = getChar();
                    return new Token(TokenType.AND, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else
                    return new Token(TokenType.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '|':
                columnNumber++;
                nextChar = getChar();

                if (nextChar == '|') {
                    nextChar = getChar();
                    return new Token(TokenType.OR, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else
                    return new Token(TokenType.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '=':
                columnNumber++;
                nextChar = getChar();

                if (nextChar == '=') {
                    nextChar = getChar();
                    return new Token(TokenType.EQ, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else
                    return new Token(TokenType.ASSIGN, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '!':
                columnNumber++;
                nextChar = getChar();

                if (nextChar == '=') {
                    nextChar = getChar();
                    return new Token(TokenType.NEQ, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else
                    return new Token(TokenType.NOT, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '<':
                columnNumber++;
                nextChar = getChar();

                if (nextChar == '=') {
                    nextChar = getChar();
                    return new Token(TokenType.LT_EQ, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else
                    return new Token(TokenType.LT, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '>':
                columnNumber++;
                nextChar = getChar();

                if (nextChar == '=') {
                    nextChar = getChar();
                    return new Token(TokenType.RT_EQ, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else
                    return new Token(TokenType.RT, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '+':
                columnNumber++;
                nextChar = getChar();
                return new Token(TokenType.PLUS, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '-':
                columnNumber++;
                nextChar = getChar();
                return new Token(TokenType.MINUS, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '*':
                columnNumber++;
                nextChar = getChar();
                return new Token(TokenType.TIMES, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '/':
                columnNumber++;
                nextChar = getChar();
                return new Token(TokenType.DIV, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '%':
                columnNumber++;
                nextChar = getChar();
                return new Token(TokenType.MOD, new TokenAttribute(), lineNumber, columnNumber - 1);
        }

        TokenType type = punctuation.get((char) nextChar);
        columnNumber++;
        nextChar = getChar();

        if (type != null)
            return new Token(type, new TokenAttribute(), lineNumber, columnNumber - 1);

        return new Token(TokenType.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);
    }
}
