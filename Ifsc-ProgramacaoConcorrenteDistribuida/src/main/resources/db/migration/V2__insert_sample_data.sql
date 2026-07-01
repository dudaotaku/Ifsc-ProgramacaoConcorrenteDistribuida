INSERT INTO produto (nome_produto, preco, descricao) VALUES
('Cheeseburger Duplo', 28.90, 'Pão brioche, duas carnes de 120g grelhadas, cheddar duplo e molho artesanal'),
('Batata Rústica Especial', 15.50, 'Batatas fritas rústicas com casca, temperadas com alecrim, alho e páprica'),
('Pizza de Calabresa 30cm', 45.00, 'Molho de tomate artesanal, mussarela, calabresa defumada fatiada e orégano'),
('Refrigerante Lata 350ml', 6.00, 'Coca-Cola ou Guaraná Antarctica gelados'),
('Petit Gâteau com Sorvete', 19.90, 'Bolo quente de chocolate cremoso com bola de sorvete de creme italiana');

INSERT INTO pedido (nome_cliente, descricao, status) VALUES
('Ana Maria', 'Cheeseburger Duplo, Refrigerante Lata 350ml', 'AGUARDANDO'),
('Carlos Andrade', 'Pizza de Calabresa 30cm, Refrigerante Lata 350ml', 'EM_PREPARO'),
('Juliana Silva', 'Batata Rústica Especial', 'PRONTO');
