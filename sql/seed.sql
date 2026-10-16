USE totem_cardapio;

INSERT INTO categoria (nome, descricao, ordem_exibicao) VALUES
('Lanches', 'Hambúrgueres artesanais', 1),
('Acompanhamentos', 'Para completar o pedido', 2),
('Bebidas', 'Geladas e refrescantes', 3),
('Sobremesas', 'Para fechar com doçura', 4);

INSERT INTO produto (categoria_id, nome, descricao, preco, imagem_url) VALUES
((SELECT id FROM categoria WHERE nome = 'Lanches'), 'X-Burger', 'Pão, hambúrguer e queijo', 22.90, 'img/x-burger.jpg'),
((SELECT id FROM categoria WHERE nome = 'Lanches'), 'X-Bacon', 'Hambúrguer, queijo e bacon crocante', 27.90, 'img/x-bacon.jpg'),
((SELECT id FROM categoria WHERE nome = 'Lanches'), 'X-Salada', 'Hambúrguer, queijo, alface e tomate', 24.90, 'img/x-salada.jpg'),
((SELECT id FROM categoria WHERE nome = 'Lanches'), 'Frango Crocante', 'Filé de frango empanado e maionese', 26.90, 'img/frango-crocante.jpg'),
((SELECT id FROM categoria WHERE nome = 'Acompanhamentos'), 'Batata Frita', 'Porção individual', 14.90, 'img/batata-frita.jpg'),
((SELECT id FROM categoria WHERE nome = 'Acompanhamentos'), 'Onion Rings', 'Anéis de cebola empanados', 16.90, 'img/onion-rings.jpg'),
((SELECT id FROM categoria WHERE nome = 'Acompanhamentos'), 'Nuggets', '10 unidades', 15.90, 'img/nuggets.jpg'),
((SELECT id FROM categoria WHERE nome = 'Bebidas'), 'Refrigerante Lata', '350 ml', 7.00, 'img/refrigerante.jpg'),
((SELECT id FROM categoria WHERE nome = 'Bebidas'), 'Suco Natural', '400 ml, sabores do dia', 9.50, 'img/suco.jpg'),
((SELECT id FROM categoria WHERE nome = 'Bebidas'), 'Água Mineral', '500 ml', 4.50, 'img/agua.jpg'),
((SELECT id FROM categoria WHERE nome = 'Sobremesas'), 'Sorvete', 'Duas bolas com calda', 10.90, 'img/sorvete.jpg'),
((SELECT id FROM categoria WHERE nome = 'Sobremesas'), 'Brownie', 'Brownie de chocolate', 12.90, 'img/brownie.jpg');