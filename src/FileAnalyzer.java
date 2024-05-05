import com.sun.net.httpserver.HttpServer;

import lexer.TestLexer;
import parser.TestParser;
import semantic.TestSemantic;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.util.stream.Collectors;
import java.io.*;
import java.net.InetSocketAddress;

public class FileAnalyzer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/analyze", new AnalyzeHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }

    static class AnalyzeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                // Set CORS headers
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

                try {
                    // Read request body
                    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    String requestBody = reader.lines().collect(Collectors.joining("\n"));

                    String[] parts = requestBody.split("\r\n\r\n");
                    int startFileContentIndex = parts[0].indexOf("\n\n") + 2;
                    int endFileContentIndex = parts[0].lastIndexOf("\n------");
                    String fileContent = parts[0].substring(startFileContentIndex, endFileContentIndex);

                    // Remove last 4 lines
                    String[] lines = fileContent.split("\n");
                    StringBuilder trimmedFileContentBuilder = new StringBuilder();
                    for (int i = 0; i < lines.length - 4; i++) {
                        trimmedFileContentBuilder.append(lines[i]).append("\n");
                    }
                    String trimmedFileContent = trimmedFileContentBuilder.toString().trim();
                    String[] lines2 = fileContent.split("\n");
                    String analysisType = lines2[lines2.length - 1].trim();

                    String response = "";
                    switch (analysisType) {
                        case "lexical":
                            response = analyzeLexical(trimmedFileContent);
                            break;
                        case "semantic":
                            response = analyzeSemantic(trimmedFileContent);
                            break;
                        case "parser":
                            response = analyzeParser(trimmedFileContent);
                            break;
                        default:
                            response = "Invalid analysis type";
                            break;
                    }

                    // Send response
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(response.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    // Handle exceptions
                    e.printStackTrace();
                    String errorMessage = "Error occurred: " + e.getMessage();
                    exchange.sendResponseHeaders(500, errorMessage.getBytes().length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(errorMessage.getBytes());
                    outputStream.close();
                }
            }
        }

        private String analyzeLexical(String fileContent) throws IOException {
            String analysisResult = TestLexer.analyzeFile(fileContent);
            return "Lexical analysis result for file content: " + analysisResult;
        }

        private String analyzeSemantic(String fileContent) throws IOException {
            String analysisResult = TestSemantic.performAnalysis(fileContent);
            return "Semantic analysis result for file content: " + analysisResult;
        }

        private String analyzeParser(String fileContent) throws IOException {
            String analysisResult = TestParser.performParsing(fileContent);
            return "Parser analysis result for file content: " + analysisResult;
        }
    }

}
