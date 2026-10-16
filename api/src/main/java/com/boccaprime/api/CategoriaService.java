package com.boccaprime.api;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaDao dao;

    public CategoriaService(CategoriaDao dao) {
        this.dao = dao;
    }

    public List<Categoria> listar() {
        return dao.listar();
    }

    public Categoria buscar(int id) {
        Categoria c = dao.buscarPorId(id);
        if (c == null) {
            throw new IllegalArgumentException("Categoria não encontrada.");
        }
        return c;
    }

    public Categoria cadastrar(Categoria c) {
        preencherPadroes(c);
        validar(c, 0);
        c.setId(dao.inserir(c));
        return c;
    }

    public Categoria editar(int id, Categoria c) {
        buscar(id); // garante que a categoria existe
        c.setId(id);
        preencherPadroes(c);
        validar(c, id);
        dao.atualizar(c);
        return c;
    }

    public void excluir(int id) {
        buscar(id);
        int total = dao.contarProdutos(id);
        if (total > 0) {
            throw new IllegalArgumentException(
                    "Não é possível excluir: esta categoria tem " + total
                            + " produto(s) vinculado(s).");
        }
        dao.excluir(id);
    }

    // Se a tela não mandar ordem ou ativo, usa valores padrão
    private void preencherPadroes(Categoria c) {
        if (c.getOrdemExibicao() == null) {
            c.setOrdemExibicao(0);
        }
        if (c.getAtivo() == null) {
            c.setAtivo(true);
        }
    }

    private void validar(Categoria c, int idIgnorado) {
        if (c.getNome() == null || c.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }
        c.setNome(c.getNome().trim());

        if (c.getNome().length() > 60) {
            throw new IllegalArgumentException("O nome pode ter no máximo 60 caracteres.");
        }
        if (c.getDescricao() != null && c.getDescricao().length() > 255) {
            throw new IllegalArgumentException("A descrição pode ter no máximo 255 caracteres.");
        }
        if (c.getOrdemExibicao() < 0) {
            throw new IllegalArgumentException("A ordem de exibição não pode ser negativa.");
        }
        if (dao.existeNome(c.getNome(), idIgnorado)) {
            throw new IllegalArgumentException("Já existe uma categoria com esse nome.");
        }
    }
}