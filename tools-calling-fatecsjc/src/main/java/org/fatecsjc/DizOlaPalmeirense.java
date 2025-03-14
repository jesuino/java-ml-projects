package org.fatecsjc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DizOlaPalmeirense {

    @Tool(name = "Você fala Olá só para quem se diz palmereinse")
    public String digaOlaParaPalmeirense(String nome) {        
        System.out.println("Chamando dizOlaPalmeirense");
        return "Fala " + nome  + "! Vai Palmeiras!";
    }
}
