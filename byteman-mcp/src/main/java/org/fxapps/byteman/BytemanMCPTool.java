package org.fxapps.byteman;

import io.quarkiverse.mcp.server.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.byteman.agent.submit.ScriptText;
import org.jboss.byteman.agent.submit.Submit;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP tool for submitting Byteman scripts to running Java processes.
 * Byteman allows runtime injection and modification of Java application
 * behavior.
 * 
 * This is a very simple implementation that assumes the Byteman agent is
 * running
 * on default host and port. Feel free to further enhance it as needed.
 */
@ApplicationScoped
public class BytemanMCPTool {

        @Tool(description = "Submit a Byteman script to a running Java process. " +
                        "The target process must have the Byteman agent attached. " +
                        "Returns the result of the script submission.")
        public String submitBytemanScript(String script) throws Exception {
                var scriptText = new ScriptText("llm_script", script);
                new Submit().addScripts(List.of(scriptText));
                return "Successfully submitted Byteman script: ----\n %s" + script;

        }

        @Tool(description = "List all Byteman rules installed in a running Java process. " +
                        "The target process must have the Byteman agent attached.")
        public String listBytemanRules() throws Exception {

                var rules = new Submit().getAllScripts();
                var rulesTxt = rules.stream()
                                .map(ScriptText::getText).collect(Collectors.joining("\n----------------\n"));
                return rules.isEmpty() ? "No rules installed." : rulesTxt;

        }

        /**
         * Removes all Byteman rules from a running Java process.
         *
         * @return Result message indicating success or failure
         * @throws Exception
         */
        @Tool(description = "Remove all Byteman rules from a running Java process. " +
                        "This clears all previously injected script rules.")
        public String removeAllBytemanRules() throws Exception {
                new Submit().deleteAllRules();
                return "All rules were successfully removed.";

        }

}
