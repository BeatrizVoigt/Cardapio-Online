package com.boccaprime.api;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteDao dao;

    public ClienteService(ClienteDao dao) {
        this.dao = dao;
    }

    public List<Cliente> listar() {
        return dao.listar();
    }

    public Cliente buscar(int id) {
        Cliente c = dao.buscarPorId(id);
        if (c == null) {
            throw new IllegalArgumentException("Cliente não encontrado.");
        }
        return c;
    }

    public Cliente cadastrar(Cliente c) {
        validar(c, 0);
        c.setId(dao.inserir(c));
        return c;
    }

    public Cliente editar(int id, Cliente c) {
        buscar(id); // garante que o cliente existe
        c.setId(id);
        validar(c, id);
        dao.atualizar(c);
        return c;
    }

    public void excluir(int id) {
        buscar(id);
        dao.excluir(id);
    }

    private void validar(Cliente c, int idIgnorado) {
        // Nome
        if (c.getNome() == null || c.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        c.setNome(c.getNome().trim());
        if (c.getNome().length() > 100) {
            throw new IllegalArgumentException("O nome pode ter no máximo 100 caracteres.");
        }

        // CPF: aceita com ou sem pontuação e guarda sempre no formato 000.000.000-00
        if (c.getCpf() == null || c.getCpf().isBlank()) {
            throw new IllegalArgumentException("O CPF é obrigatório.");
        }
        String digitos = c.getCpf().replaceAll("\\D", "");
        if (!cpfValido(digitos)) {
            throw new IllegalArgumentException("CPF inválido. Confira os números digitados.");
        }
        c.setCpf(formatarCpf(digitos));
        if (dao.existeCpf(c.getCpf(), idIgnorado)) {
            throw new IllegalArgumentException("Já existe um cliente com esse CPF.");
        }

        // Telefone (opcional)
        if (c.getTelefone() != null) {
            c.setTelefone(c.getTelefone().trim());
            if (c.getTelefone().isEmpty()) {
                c.setTelefone(null);
            } else if (c.getTelefone().length() > 20) {
                throw new IllegalArgumentException("O telefone pode ter no máximo 20 caracteres.");
            } else if (!c.getTelefone().matches("[0-9()+\\-\\s]+")) {
                throw new IllegalArgumentException("O telefone deve conter apenas números, espaços, parênteses e traço.");
            }
        }

        // E-mail (opcional)
        if (c.getEmail() != null) {
            c.setEmail(c.getEmail().trim());
            if (c.getEmail().isEmpty()) {
                c.setEmail(null);
            } else if (c.getEmail().length() > 100) {
                throw new IllegalArgumentException("O e-mail pode ter no máximo 100 caracteres.");
            } else if (!c.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new IllegalArgumentException("E-mail inválido.");
            }
        }
    }

    private String formatarCpf(String d) {
        return d.substring(0, 3) + "." + d.substring(3, 6) + "."
                + d.substring(6, 9) + "-" + d.substring(9);
    }

    // Confere os dois dígitos verificadores do CPF
    private boolean cpfValido(String d) {
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return d.charAt(9) - '0' == digitoVerificador(d, 9)
                && d.charAt(10) - '0' == digitoVerificador(d, 10);
    }

    private int digitoVerificador(String d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (d.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}