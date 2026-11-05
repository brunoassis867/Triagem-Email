package triagem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EmailRepository implements AutoCloseable {

    private final Connection conn;

    public EmailRepository(String url, String user, String senha) throws SQLException {
        conn = DriverManager.getConnection(url, user, senha);
    }

    public void salvar(String email, ResultadoTriagem r) throws SQLException {
        String sql = "INSERT INTO emails_triados (email_original, categoria, resumo, resposta_sugerida) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, r.categoria());
            ps.setString(3, r.resumo());
            ps.setString(4, r.respostaSugerida());
            ps.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}
