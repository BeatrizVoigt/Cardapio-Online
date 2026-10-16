package com.boccaprime.api;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProdutoService {

    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("99999999.99");

    private final ProdutoDao dao;
    private final CategoriaDao categoriaDao;

    public ProdutoService(ProdutoDao dao, CategoriaDao categoriaDao) {
        this.dao = dao;
        this.categoriaDao = categoriaDao;
    }

    public List<Produto> listar(Integer categoriaId, String busca) {
        return dao.listar(categoriaId, busca);
    }

    public Produto buscar(int id) {
        Produto p = dao.buscarPorId(id);
        if (p == null) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }
        return p;
    }

    public Produto cadastrar(Produto p) {
        preencherPadroes(p);
        validar(p);
        int novoId = dao.inserir(p);
        return buscar(novoId);
    }

    public Produto editar(int id, Produto p) {
        buscar(id); // garante que o produto existe
        p.setId(id);
        preencherPadroes(p);
        validar(p);
        dao.atualizar(p);
        return buscar(id);
    }

    public Produto alterarDisponibilidade(int id, boolean disponivel) {
        buscar(id);
        dao.atualizarDisponibilidade(id, disponivel);
        return buscar(id);
    }

    // Devolve true se excluiu de verdade, false se apenas marcou como indisponível
    public boolean excluir(int id) {
        buscar(id);
        if (dao.contarVendas(id) > 0) {
            dao.atualizarDisponibilidade(id, false);
            return false;
        }
        dao.excluir(id);
        return true;
    }

    private void preencherPadroes(Produto p) {
        if (p.getDisponivel() == null) {
            p.setDisponivel(true);
        }
    }

    private void validar(Produto p) {
        // Nome (RN02)
        if (p.getNome() == null || p.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        p.setNome(p.getNome().trim());
        if (p.getNome().length() > 100) {
            throw new IllegalArgumentException("O nome pode ter no máximo 100 caracteres.");
        }

        // Textos opcionais
        if (p.getDescricao() != null && p.getDescricao().length() > 500) {
            throw new IllegalArgumentException("A descrição pode ter no máximo 500 caracteres.");
        }
        if (p.getImagemUrl() != null && p.getImagemUrl().length() > 255) {
            throw new IllegalArgumentException("O caminho da imagem pode ter no máximo 255 caracteres.");
        }

        // Preço (RN01)
        if (p.getPreco() == null) {
            throw new IllegalArgumentException("O preço é obrigatório.");
        }
        if (p.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero.");
        }
        if (p.getPreco().stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O preço deve ter no máximo duas casas decimais.");
        }
        if (p.getPreco().compareTo(PRECO_MAXIMO) > 0) {
            throw new IllegalArgumentException("O preço informado é muito alto.");
        }
        p.setPreco(p.getPreco().setScale(2, RoundingMode.HALF_UP));

        // Categoria
        if (p.getCategoriaId() == null) {
            throw new IllegalArgumentException("Escolha uma categoria para o produto.");
        }
        if (categoriaDao.buscarPorId(p.getCategoriaId()) == null) {
            throw new IllegalArgumentException("A categoria escolhida não existe.");
        }
    }
}