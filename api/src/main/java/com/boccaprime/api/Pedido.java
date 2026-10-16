package com.boccaprime.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private Integer id;
    private Integer numeroSenha;
    private String tipoConsumo;     // LOCAL ou VIAGEM
    private String formaPagamento;  // CAIXA, CARTAO_SIMULADO ou PIX_SIMULADO
    private String status;          // RECEBIDO, EM_PREPARO, PRONTO, ENTREGUE ou CANCELADO
    private BigDecimal valorTotal;
    private LocalDateTime criadoEm;
    private Integer clienteId;      // opcional
    private List<ItemPedido> itens = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getNumeroSenha() { return numeroSenha; }
    public void setNumeroSenha(Integer numeroSenha) { this.numeroSenha = numeroSenha; }

    public String getTipoConsumo() { return tipoConsumo; }
    public void setTipoConsumo(String tipoConsumo) { this.tipoConsumo = tipoConsumo; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }
}