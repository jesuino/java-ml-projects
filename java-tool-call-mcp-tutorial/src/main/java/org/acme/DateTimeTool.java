package org.acme;

import java.time.LocalDateTime;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DateTimeTool {

    @Tool(name = "Provides the current Date and Time Tool")
    public String getCurrentDateTime() {
        System.out.println("Getting current date and time...");
        return LocalDateTime.now().toString();
    }
    
}

