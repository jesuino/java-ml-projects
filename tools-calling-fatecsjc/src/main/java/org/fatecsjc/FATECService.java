package org.fatecsjc;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = {DiaEHora.class, DizOlaPalmeirense.class})
public interface FATECService {

    @SystemMessage("Você é um simpático assistente e pode consultar o dia e hora usando Tools")
    String input(String input);

}