package com.boccaprime.api;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class PedidoService {

    private static final Set<String> TIPOS_CONSUMO = Set.of("LOCAL", "VIAGEM");
    private static final Set<String> FORMAS_PAGAMENTO =
            Set.of("CAIXA", "CARTAO_SIMULADO", "PIX_SIMULADO");

    private final PedidoDao pedidoDao;
    private final ProdutoDao produtoDao;
    private final CategoriaDao categoriaDao;

    public PedidoService(PedidoDao pedidoDao, ProdutoDao produtoDao, CategoriaDao categoriaDao) {
        this.pedidoDao = pedidoDao;
        this.produtoDao = produtoDao;
        this.categoriaDao = categoriaDao;
    }

    public List<Pedido> listarDoDia() {
        return pedidoDao.listarDoDia();
    }

    public Pedido buscar(int id) {
        Pedido p = pedidoDao.buscarPorId(id);
        if (p == null) {
            throw new IllegalArgumentException("Pedido não encontrado.");
        }
        return p;
    }

    // Grava o pedido e TODOS os itens numa única transação (RF20, RN10).
    // Se qualquer passo falhar, o banco desfaz tudo (rollback).
    @Transactional
    public Pedido criar(Pedido entrada) {
        // RN04: pelo menos 1 item
        if (entrada.getItens() == null || entrada.getItens().isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um item ao pedido.");
        }
        if (entrada.getTipoConsumo() == null || !TIPOS_CONSUMO.contains(entrada.getTipoConsumo())) {
            throw new IllegalArgumentException("Escolha entre comer aqui ou para viagem.");
        }
        if (entrada.getFormaPagamento() == null
                || !FORMAS_PAGAMENTO.contains(entrada.getFormaPagamento())) {
            throw new IllegalArgumentException("Escolha uma forma de pagamento.");
        }

        Pedido pedido = new Pedido();
        pedido.setTipoConsumo(entrada.getTipoConsumo());
        pedido.setFormaPagamento(entrada.getFormaPagamento());
        pedido.setStatus("RECEBIDO");
        pedido.setNumeroSenha(pedidoDao.proximaSenhaDoDia()); // RN07
        // O vínculo com cliente será ligado quando fizermos o CRUD de clientes
        pedido.setClienteId(null);

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido itemEntrada : entrada.getItens()) {
            // RN03: quantidade entre 1 e 20
            if (itemEntrada.getQuantidade() == null
                    || itemEntrada.getQuantidade() < 1 || itemEntrada.getQuantidade() > 20) {
                throw new IllegalArgumentException("A quantidade de cada item deve ser de 1 a 20.");
            }
            if (itemEntrada.getProdutoId() == null) {
                throw new IllegalArgumentException("Item sem produto.");
            }

            Produto produto = produtoDao.buscarPorId(itemEntrada.getProdutoId());
            if (produto == null) {
                throw new IllegalArgumentException("Um dos produtos do pedido não existe mais.");
            }

            // RN09: produto indisponível ou de categoria inativa não pode ser pedido
            Categoria categoria = categoriaDao.buscarPorId(produto.getCategoriaId());
            if (!produto.getDisponivel() || categoria == null || !categoria.getAtivo()) {
                throw new IllegalArgumentException(
                        "O produto \"" + produto.getNome() + "\" não está disponível no momento.");
            }

            String observacao = itemEntrada.getObservacao();
            if (observacao != null) {
                observacao = observacao.trim();
                if (observacao.length() > 200) {
                    throw new IllegalArgumentException("A observação pode ter no máximo 200 caracteres.");
                }
                if (observacao.isEmpty()) {
                    observacao = null;
                }
            }

            // RN05: o preço é copiado do produto NO MOMENTO da compra
            // RN06: subtotal = quantidade x preço unitário, calculado aqui no Java
            ItemPedido item = new ItemPedido();
            item.setProdutoId(produto.getId());
            item.setQuantidade(itemEntrada.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(itemEntrada.getQuantidade())));
            item.setObservacao(observacao);

            pedido.getItens().add(item);
            total = total.add(item.getSubtotal());
        }

        pedido.setValorTotal(total); // RN06: soma dos subtotais

        int pedidoId = pedidoDao.inserir(pedido);
        for (ItemPedido item : pedido.getItens()) {
            item.setPedidoId(pedidoId);
            pedidoDao.inserirItem(item);
        }

        return buscar(pedidoId);
    }

    // RN08: RECEBIDO -> EM_PREPARO -> PRONTO -> ENTREGUE
    public Pedido avancarStatus(int id) {
        Pedido p = buscar(id);
        String proximo;
        switch (p.getStatus()) {
            case "RECEBIDO" -> proximo = "EM_PREPARO";
            case "EM_PREPARO" -> proximo = "PRONTO";
            case "PRONTO" -> proximo = "ENTREGUE";
            default -> throw new IllegalArgumentException(
                    "Este pedido já está " + (p.getStatus().equals("ENTREGUE") ? "entregue" : "cancelado")
                            + " e não pode avançar.");
        }
        pedidoDao.atualizarStatus(id, proximo);
        return buscar(id);
    }

    // RN08: só pode cancelar antes de PRONTO
    public Pedido cancelar(int id) {
        Pedido p = buscar(id);
        if (!p.getStatus().equals("RECEBIDO") && !p.getStatus().equals("EM_PREPARO")) {
            throw new IllegalArgumentException(
                    "Só é possível cancelar um pedido que ainda não está pronto.");
        }
        pedidoDao.atualizarStatus(id, "CANCELADO");
        return buscar(id);
    }
}
