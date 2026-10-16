package com.boccaprime.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

@Repository
public class PedidoDao {

    private final JdbcTemplate jdbc;

    public PedidoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_PEDIDO =
            "SELECT id, numero_senha, tipo_consumo, forma_pagamento, status, "
                    + "valor_total, criado_em, cliente_id FROM pedido ";

    // Converte uma linha da tabela pedido em um objeto Pedido
    private final RowMapper<Pedido> pedidoMapper = (rs, linha) -> {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setNumeroSenha(rs.getInt("numero_senha"));
        p.setTipoConsumo(rs.getString("tipo_consumo"));
        p.setFormaPagamento(rs.getString("forma_pagamento"));
        p.setStatus(rs.getString("status"));
        p.setValorTotal(rs.getBigDecimal("valor_total"));
        p.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        int clienteId = rs.getInt("cliente_id");
        p.setClienteId(rs.wasNull() ? null : clienteId);
        return p;
    };

    // Converte uma linha de item_pedido (com o nome do produto) em ItemPedido
    private final RowMapper<ItemPedido> itemMapper = (rs, linha) -> {
        ItemPedido i = new ItemPedido();
        i.setId(rs.getInt("id"));
        i.setPedidoId(rs.getInt("pedido_id"));
        i.setProdutoId(rs.getInt("produto_id"));
        i.setProdutoNome(rs.getString("produto_nome"));
        i.setQuantidade(rs.getInt("quantidade"));
        i.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        i.setSubtotal(rs.getBigDecimal("subtotal"));
        i.setObservacao(rs.getString("observacao"));
        return i;
    };

    // Próxima senha do dia: sequencial, volta para 1 a cada dia (RN07)
    public int proximaSenhaDoDia() {
        Integer proxima = jdbc.queryForObject(
                "SELECT COALESCE(MAX(numero_senha), 0) + 1 FROM pedido "
                        + "WHERE DATE(criado_em) = CURDATE()",
                Integer.class);
        return proxima == null ? 1 : proxima;
    }

    public int inserir(Pedido p) {
        KeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(conexao -> {
            PreparedStatement ps = conexao.prepareStatement(
                    "INSERT INTO pedido (numero_senha, tipo_consumo, forma_pagamento, "
                            + "status, valor_total, cliente_id) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getNumeroSenha());
            ps.setString(2, p.getTipoConsumo());
            ps.setString(3, p.getFormaPagamento());
            ps.setString(4, p.getStatus());
            ps.setBigDecimal(5, p.getValorTotal());
            if (p.getClienteId() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, p.getClienteId());
            }
            return ps;
        }, chave);
        return chave.getKey().intValue();
    }

    public void inserirItem(ItemPedido i) {
        jdbc.update(
                "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, "
                        + "preco_unitario, subtotal, observacao) VALUES (?, ?, ?, ?, ?, ?)",
                i.getPedidoId(), i.getProdutoId(), i.getQuantidade(),
                i.getPrecoUnitario(), i.getSubtotal(), i.getObservacao());
    }

    // Pedidos de hoje, os mais recentes primeiro (RF10)
    public List<Pedido> listarDoDia() {
        return jdbc.query(
                SELECT_PEDIDO + "WHERE DATE(criado_em) = CURDATE() ORDER BY criado_em DESC",
                pedidoMapper);
    }

    // Pedido com os seus itens, usando JOIN com produto (RF11)
    public Pedido buscarPorId(int id) {
        List<Pedido> lista = jdbc.query(SELECT_PEDIDO + "WHERE id = ?", pedidoMapper, id);
        if (lista.isEmpty()) {
            return null;
        }
        Pedido pedido = lista.get(0);
        pedido.setItens(jdbc.query(
                "SELECT ip.id, ip.pedido_id, ip.produto_id, pr.nome AS produto_nome, "
                        + "ip.quantidade, ip.preco_unitario, ip.subtotal, ip.observacao "
                        + "FROM item_pedido ip JOIN produto pr ON pr.id = ip.produto_id "
                        + "WHERE ip.pedido_id = ? ORDER BY ip.id",
                itemMapper, id));
        return pedido;
    }

    public int atualizarStatus(int id, String status) {
        return jdbc.update("UPDATE pedido SET status = ? WHERE id = ?", status, id);
    }
}