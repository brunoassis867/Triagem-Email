package triagem;

import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws Exception {
        String chave = System.getenv("ANTHROPIC_API_KEY");
        if (chave == null || chave.isBlank()) {
            System.err.println("Falta definir a variável ANTHROPIC_API_KEY.");
            return;
        }
        String modelo = System.getenv().getOrDefault("ANTHROPIC_MODEL", "claude-haiku-4-5");
        LlmClient llm = new LlmClient(chave, modelo);

        // o banco é opcional: sem DB_URL ele só mostra o resultado na tela
        EmailRepository repo = null;
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null && !dbUrl.isBlank()) {
            repo = new EmailRepository(dbUrl,
                    System.getenv().getOrDefault("DB_USER", "root"),
                    System.getenv().getOrDefault("DB_PASSWORD", ""));
        }

        String arquivo = new String(
                Main.class.getResourceAsStream("/emails_exemplo.txt").readAllBytes(),
                StandardCharsets.UTF_8);

        int numero = 1;
        for (String email : arquivo.split("(?m)^---\\s*$")) {
            email = email.strip();
            if (email.isEmpty()) continue;

            System.out.println("--- E-mail " + numero++ + " ---");
            System.out.println(email);
            try {
                ResultadoTriagem r = llm.triar(email);
                System.out.println("Categoria: " + r.categoria());
                System.out.println("Resumo: " + r.resumo());
                System.out.println("Resposta: " + r.respostaSugerida() + "\n");
                if (repo != null) repo.salvar(email, r);
            } catch (Exception e) {
                System.err.println("Erro neste e-mail: " + e.getMessage() + "\n");
            }
        }

        if (repo != null) repo.close();
    }
}
