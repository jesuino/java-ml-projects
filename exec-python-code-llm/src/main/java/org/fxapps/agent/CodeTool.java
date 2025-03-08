package org.fxapps.agent;

import java.io.StringWriter;

import org.python.util.PythonInterpreter;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CodeTool {
    @Tool("Execute the given Python code")
    public String execCode(String code) {
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            StringWriter output = new StringWriter();
            System.out.println("Running the following code: \n" + "*".repeat(10) + "\n" + code + "\n"+  "*".repeat(10) );
            pyInterp.setOut(output);
            pyInterp.exec(code);
            var result = output.toString().trim();
            System.out.println("Result is: " + result);
            return result;
        } catch (Exception e) {
            String error = "Error running code: " + e.getMessage();
            System.out.println(error);
            return error;
        }

    }
}
