package org.fxapps;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface AIService {

    @SystemMessage("You are a nice and helpful assistant.")
    String input(String input);

}
