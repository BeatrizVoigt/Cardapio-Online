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
public class CategoriaDao {

    private final JdbcTemplate jdbc;

    public CategoriaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Converte uma linha da tabela em um objeto Categoria
    private final RowMapper<Categoria> mapper = (rs, linha) -> {
        Categoria c = new Categoria();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setDescricao(rs.getString("descricao"));
        c.setOrdemExibicao(rs.getInt("ordem_exibicao"));
        c.setAtivo(rs.getBoolean("ativo"));
        return c;
    };

    public List<Categoria> listar() {
        return jdbc.query(
                "SELECT id, nome, descricao, ordem_exibicao, ativo "
                        + "FROM categoria ORDER BY ordem_exibicao",
                mapper);
    }

    public Categoria buscarPorId(int id) {
        List<Categoria> lista = jdbc.query(
                "SELECT id, nome, descricao, ordem_exibicao, ativo "
                        + "FROM categoria WHERE id = ?",
                mapper, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public boolean existeNome(String nome, int idIgnorado) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM categoria WHERE nome = ? AND id <> ?",
                Integer.class, nome, idIgnorado);
        return total != null && total > 0;
    }

    public int contarProdutos(int categoriaId) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM produto WHERE categoria_id = ?",
                Integer.class, categoriaId);
        return total == null ? 0 : total;
    }

    public int inserir(Categoria c) {
        KeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(conexao -> {
            PreparedStatement ps = conexao.prepareStatement(
                    "INSERT INTO categoria (nome, descricao, ordem_exibicao, ativo) "
                            + "VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getNome());
            ps.setString(2, c.getDescricao());
            ps.setInt(3, c.getOrdemExibicao());
            ps.setBoolean(4, c.getAtivo());
            return ps;
        }, chave);
        return chave.getKey().intValue();
    }

    public int atualizar(Categoria c) {
        return jdbc.update(
                "UPDATE categoria SET nome = ?, descricao = ?, ordem_exibicao = ?, ativo = ? "
                        + "WHERE id = ?",
                c.getNome(), c.getDescricao(), c.getOrdemExibicao(), c.getAtivo(), c.getId());
    }

    public int excluir(int id) {
        return jdbc.update("DELETE FROM categoria WHERE id = ?", id);
    }
}