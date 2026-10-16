package com.boccaprime.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProdutoDao {

    private final JdbcTemplate jdbc;

    public ProdutoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "SELECT p.id, p.categoria_id, c.nome AS categoria_nome, p.nome, p.descricao, "
                    + "p.preco, p.imagem_url, p.disponivel "
                    + "FROM produto p JOIN categoria c ON c.id = p.categoria_id ";

    // Converte uma linha da consulta em um objeto Produto
    private final RowMapper<Produto> mapper = (rs, linha) -> {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setCategoriaId(rs.getInt("categoria_id"));
        p.setCategoriaNome(rs.getString("categoria_nome"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setImagemUrl(rs.getString("imagem_url"));
        p.setDisponivel(rs.getBoolean("disponivel"));
        return p;
    };

    // Lista com filtros opcionais: categoria e busca por nome
    public List<Produto> listar(Integer categoriaId, String busca) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (categoriaId != null) {
            sql.append("AND p.categoria_id = ? ");
            parametros.add(categoriaId);
        }
        if (busca != null && !busca.isBlank()) {
            sql.append("AND p.nome LIKE ? ");
            parametros.add("%" + busca.trim() + "%");
        }
        sql.append("ORDER BY c.ordem_exibicao, p.nome");

        return jdbc.query(sql.toString(), mapper, parametros.toArray());
    }

    public Produto buscarPorId(int id) {
        List<Produto> lista = jdbc.query(SELECT_BASE + "WHERE p.id = ?", mapper, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Quantas vezes o produto já aparece em algum pedido
    public int contarVendas(int produtoId) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM item_pedido WHERE produto_id = ?",
                Integer.class, produtoId);
        return total == null ? 0 : total;
    }

    public int inserir(Produto p) {
        KeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(conexao -> {
            PreparedStatement ps = conexao.prepareStatement(
                    "INSERT INTO produto (categoria_id, nome, descricao, preco, imagem_url, disponivel) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getCategoriaId());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getDescricao());
            ps.setBigDecimal(4, p.getPreco());
            ps.setString(5, p.getImagemUrl());
            ps.setBoolean(6, p.getDisponivel());
            return ps;
        }, chave);
        return chave.getKey().intValue();
    }

    public int atualizar(Produto p) {
        return jdbc.update(
                "UPDATE produto SET categoria_id = ?, nome = ?, descricao = ?, preco = ?, "
                        + "imagem_url = ?, disponivel = ? WHERE id = ?",
                p.getCategoriaId(), p.getNome(), p.getDescricao(), p.getPreco(),
                p.getImagemUrl(), p.getDisponivel(), p.getId());
    }

    public int atualizarDisponibilidade(int id, boolean disponivel) {
        return jdbc.update("UPDATE produto SET disponivel = ? WHERE id = ?", disponivel, id);
    }

    public int excluir(int id) {
        return jdbc.update("DELETE FROM produto WHERE id = ?", id);
    }
}