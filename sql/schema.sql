CREATE DATABASE IF NOT EXISTS totem_cardapio
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE totem_cardapio;

CREATE TABLE categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(60) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    ordem_exibicao INT NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE produto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    categoria_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    preco DECIMAL(10,2) NOT NULL CHECK (preco > 0),
    imagem_url VARCHAR(255),
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_produto_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria(id) ON DELETE RESTRICT
);

CREATE TABLE cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(100),
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_senha INT NOT NULL,
    tipo_consumo ENUM('LOCAL','VIAGEM') NOT NULL,
    forma_pagamento ENUM('CAIXA','CARTAO_SIMULADO','PIX_SIMULADO') NOT NULL,
    status ENUM('RECEBIDO','EM_PREPARO','PRONTO','ENTREGUE','CANCELADO')
        NOT NULL DEFAULT 'RECEBIDO',
    valor_total DECIMAL(10,2) NOT NULL,
    cliente_id INT NULL,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente(id) ON DELETE SET NULL
);

CREATE TABLE item_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    observacao VARCHAR(200),
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id)
        REFERENCES produto(id) ON DELETE RESTRICT
);