package com.boccaprime.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ClienteDao {

    private final JdbcTemplate jdbc;

    public ClienteDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "SELECT id, nome, cpf, telefone, email FROM cliente ";

    // Converte uma linha da tabela em um objeto Cliente
    private final RowMapper<Cliente> mapper = (rs, linha) -> {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setCpf(rs.getString("cpf"));
        c.setTelefone(rs.getString("telefone"));
        c.setEmail(rs.getString("email"));
        return c;
    };

    public List<Cliente> listar() {
        return jdbc.query(SELECT_BASE + "ORDER BY nome", mapper);
    }

    public Cliente buscarPorId(int id) {
        List<Cliente> lista = jdbc.query(SELECT_BASE + "WHERE id = ?", mapper, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public Cliente buscarPorCpf(String cpf) {
        List<Cliente> lista = jdbc.query(SELECT_BASE + "WHERE cpf = ?", mapper, cpf);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Verifica se o CPF já existe em outro cliente (idIgnorado permite editar sem conflito)
    public boolean existeCpf(String cpf, int idIgnorado) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cliente WHERE cpf = ? AND id <> ?",
                Integer.class, cpf, idIgnorado);
        return total != null && total > 0;
    }

    public int inserir(Cliente c) {
        KeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(conexao -> {
            PreparedStatement ps = conexao.prepareStatement(
                    "INSERT INTO cliente (nome, cpf, telefone, email) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getNome());
            ps.setString(2, c.getCpf());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getEmail());
            return ps;
        }, chave);
        return chave.getKey().intValue();
    }

    public int atualizar(Cliente c) {
        return jdbc.update(
                "UPDATE cliente SET nome = ?, cpf = ?, telefone = ?, email = ? WHERE id = ?",
                c.getNome(), c.getCpf(), c.getTelefone(), c.getEmail(), c.getId());
    }

    public int excluir(int id) {
        return jdbc.update("DELETE FROM cliente WHERE id = ?", id);
    }
}