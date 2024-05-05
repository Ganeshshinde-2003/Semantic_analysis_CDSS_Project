package semantic;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class TestSemantic {
    public static String performAnalysis(String fileContent) throws IOException {
        try (StringReader reader = new StringReader(fileContent)) {
            SemanticAnalyzer semantic = new SemanticAnalyzer(fileContent);
            semantic.analyzeProgram();

            long startTime = System.currentTimeMillis();
            long endTime;

            endTime = System.currentTimeMillis();
            String result = "File has finished analyzing!\n" +
                    "Execution time: " + (endTime - startTime) + "ms\n";

            // Concatenate all error messages
            List<String> errorMessages = semantic.getAllErrorMessages();
            if (!errorMessages.isEmpty()) {
                result += errorMessages.size() + " errors reported\n";
                for (String errorMsg : errorMessages) {
                    result += errorMsg;
                }
            } else {
                result += "No errors found.";
            }

            return result;
        }
    }
}
