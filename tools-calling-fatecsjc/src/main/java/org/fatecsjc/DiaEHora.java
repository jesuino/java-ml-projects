package org.fatecsjc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DiaEHora {

    @Tool(name = "Fornece o dia e hora atuais")
    public String forneceDiaeHora() {
        String diaEHora = LocalDateTime.now().toString();
        System.out.println("Chamando dia e hora" + diaEHora);
        
        return diaEHora;
    }
}
