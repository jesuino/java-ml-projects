package org.fxapps.agent;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = { CodeTool.class })
public interface TestService {

    @SystemMessage("You are an assistant that generates Python code and execute it.")
    String input(String input);

}
