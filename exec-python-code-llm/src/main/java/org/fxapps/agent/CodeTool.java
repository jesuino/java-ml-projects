package org.fxapps.agent;

import java.io.StringWriter;
import org.python.util.PythonInterpreter;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CodeTool {

    @Tool("Execute the given Python code")
    public String execCode(String code) {
        try (var pyInterp = new PythonInterpreter()) {            

            System.out.println("Running the following code: \n" + "*".repeat(10) + "\n" + code + "\n" + "*".repeat(10));
            var result = _execCode(code, pyInterp);

            // if result is empty we try to execute again this time printing the last expression
            if (result.trim().isEmpty()) {
                code  += "\nprint(_)";
                result = _execCode(code, pyInterp);
            }
            System.out.println("Result is: " + result);
            return result;
        } catch (Exception e) {
            String error = "Error running code: " + e.getMessage();
            System.out.println(error);
            return error;
        }

    }

    private String _execCode(String code, PythonInterpreter pyInterp) {
        var output = new StringWriter();
        pyInterp.setOut(output);
        pyInterp.exec(code);
        var result = output.toString().trim();
        return result;
    }
}
