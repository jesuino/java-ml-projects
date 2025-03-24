package org.fxapps.ollamamcp;

import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class App {
    private static final String[] SERVER_PARAMS = {
            "jdbc@quarkiverse/quarkus-mcp-servers"
    };

    public static void main(String[] args) {

        var params = ServerParameters.builder("jbang")
                .args(SERVER_PARAMS)
                .build();
        var transport = new StdioClientTransport(params);
        var client = McpClient.sync(transport).build();
        client.initialize();

        System.out.println("Available tools:");
        client.listTools()
                .tools()
                .stream()
                .map(tool -> tool.name() + ": " + tool.description())
                .forEach(System.out::println);

        System.out.println("Tools Parameters:");
        client.listTools()
                .tools()
                .stream()
                .forEach(tool -> {
                    System.out.println(tool.name() + "\n-");
                    var properties = tool.inputSchema().properties();
                    properties.forEach((k, v) -> {
                        System.out.println(k + ": " + v);
                    });
                    if (properties.isEmpty()) {
                        System.out.println("no params");
                    }
                    System.out.println("-----");
                });

        var sql = """
                CREATE TABLE users (
                    id INT PRIMARY KEY,
                    name VARCHAR(100),
                    age INT
                );
                """;
        var result = client.callTool(new CallToolRequest("write_query",
                Map.of("query", sql))).content();
        if (result.size() > 0 &&
                result.get(0) instanceof TextContent content) {
            System.out.println(content.text());
        }

        client.close();

    }
}
