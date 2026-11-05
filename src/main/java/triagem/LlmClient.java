package triagem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LlmClient {

    private static final String URL = "https://api.anthropic.com/v1/messages";

    private static final String PROMPT = """
            Você atende clientes por e-mail.
            Leia o e-mail e responda só com um JSON, sem mais nada, assim:
            {"categoria": "reclamacao, duvida ou pedido",
             "resumo": "resumo em até 2 linhas",
             "resposta_sugerida": "resposta educada e curta"}
            """;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String chave;
    private final String modelo;

    public LlmClient(String chave, String modelo) {
        this.chave = chave;
        this.modelo = modelo;
    }

    public ResultadoTriagem triar(String email) throws Exception {
        ObjectNode corpo = mapper.createObjectNode();
        corpo.put("model", modelo);
        corpo.put("max_tokens", 600);
        corpo.put("system", PROMPT);
        corpo.putArray("messages").addObject()
                .put("role", "user")
                .put("content", email);

        HttpRequest req = HttpRequest.newBuilder(URI.create(URL))
                .header("content-type", "application/json")
                .header("x-api-key", chave)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(corpo)))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("API respondeu " + res.statusCode() + ": " + res.body());
        }

        // o texto gerado vem em content[0].text
        String texto = mapper.readTree(res.body()).path("content").path(0).path("text").asText();
        return lerJson(texto);
    }

    // às vezes a LLM devolve o JSON dentro de ```json ... ```, então limpo antes
    private ResultadoTriagem lerJson(String texto) throws Exception {
        texto = texto.strip().replaceAll("^```(json)?", "").replaceAll("```$", "").strip();
        JsonNode n = mapper.readTree(texto);
        return new ResultadoTriagem(
                n.path("categoria").asText(),
                n.path("resumo").asText(),
                n.path("resposta_sugerida").asText());
    }
}
