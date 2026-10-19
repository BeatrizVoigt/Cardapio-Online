# BOCCA PRIME - Cardápio Online para Totem

Totem de autoatendimento feito em Java com MySQL, com área administrativa para o operador.

## Requisitos
- Java 17 ou superior (testado com Java 21)
- MySQL 8.x
- VS Code com a extensão Live Server (ou outro servidor estático)

## Como executar

1. Banco de dados: no MySQL Workbench, abra e execute, nesta ordem, os arquivos da pasta `sql`:
   1. `schema.sql` (cria o banco `totem_cardapio` e as tabelas)
   2. `seed.sql` (carga inicial: 4 categorias e 12 produtos)

2. Configuração: na pasta `api/src/main/resources`, copie o arquivo
   `application.properties.example` para `application.properties` e troque
   `SUA_SENHA_AQUI` pela senha do seu MySQL.

3. Back-end: abra a pasta `api` no VS Code e execute o `ApiApplication.java`
   (botão Run acima do método `main`). O servidor sobe em http://localhost:8080.

4. **Front-end:** abra a pasta principal no VS Code e use *Open with Live Server* no `index.html`.

   **Fluxo do cliente**
   - `index.html`: **cadastro do cliente** (nome, CPF, telefone e e-mail). Se o CPF já existir, o sistema entra com o cliente cadastrado. Há também a opção "Pedir sem cadastro".
   - `totem.html`: pedido (cardápio, carrinho, pagamento e senha)
   - `acompanhar.html`: situação do pedido (recebido, em preparo, pronto, entregue)

   **Área do operador** (link "Área do operador" na tela inicial)
   - `admin-clientes.html`: cadastro, edição e exclusão de clientes
   - `admin-pedidos.html`: lista de pedidos, detalhes e mudança de status
   - `admin-categorias.html` e `admin-produtos.html`: manutenção do cardápio

## Arquitetura
Camadas: model (entidades), DAO (acesso a dados com JDBC e PreparedStatement),
service (regras de negócio e transação), controller (API REST) e interface web (HTML e JavaScript).

## Decisões de projeto
- Dinheiro sempre em `BigDecimal`, nunca `double`.
- O preço do item é copiado no momento da compra (`preco_unitario`), então mudar o preço
  do produto não altera pedidos antigos.
- Pedido e itens são gravados na mesma transação (`@Transactional` em `PedidoService.criar`).
- Produto já vendido não é excluído: é apenas marcado como indisponível.
- A senha do pedido é sequencial e reinicia em 1 a cada dia.
- Credenciais do banco ficam fora do código, em `application.properties` (ignorado pelo Git).
- O cadastro de clientes é um extra (fora do escopo do case) e é a porta de entrada do fluxo. O pedido mostra o cliente na tela de acompanhamento, mas a coluna `pedido.cliente_id` (opcional) ainda não é preenchida pelo sistema.