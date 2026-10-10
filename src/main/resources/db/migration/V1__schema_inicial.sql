-- 1. Criação da Tabela de Veículos
CREATE TABLE veiculos (
    id SERIAL PRIMARY KEY,
    placa VARCHAR(10) UNIQUE NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    tipo VARCHAR(20) CHECK (tipo IN ('LEVE', 'PESADO')), -- Exemplo de constraint
    ano INTEGER
);

-- 2. Criação da Tabela de Viagens
CREATE TABLE viagens (
    id SERIAL PRIMARY KEY,
    veiculo_id INTEGER REFERENCES veiculos(id) ON DELETE CASCADE,
    data_saida TIMESTAMP NOT NULL,
    data_chegada TIMESTAMP,
    origem VARCHAR(100),
    destino VARCHAR(100),
    km_percorrida DECIMAL(10,2)
);

-- 3. Criação da Tabela de Manutenções
CREATE TABLE manutencoes (
    id SERIAL PRIMARY KEY,
    veiculo_id INTEGER REFERENCES veiculos(id) ON DELETE CASCADE,
    data_inicio DATE NOT NULL,
    data_finalizacao DATE,
    tipo_servico VARCHAR(100),
    custo_estimado DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'PENDENTE' -- PENDENTE, EM_REALIZACAO, CONCLUIDA
);

-- Inserindo Veículos
INSERT INTO veiculos (placa, modelo, tipo, ano) VALUES 
('ABC-1234', 'Fiorino', 'LEVE', 2022),
('XYZ-9876', 'Volvo FH', 'PESADO', 2021),
('KJG-1122', 'Mercedes Sprinter', 'LEVE', 2020),
('LMN-4455', 'Scania R500', 'PESADO', 2023);

-- Inserindo Viagens (Para testar o Dashboard)
INSERT INTO viagens (veiculo_id, data_saida, data_chegada, origem, destino, km_percorrida) VALUES 
(1, '2024-05-01 08:00:00', '2024-05-01 18:00:00', 'São Paulo', 'Rio de Janeiro', 435.00),
(1, '2024-05-05 09:00:00', '2024-05-05 12:00:00', 'Rio de Janeiro', 'Niterói', 20.50),
(2, '2024-05-02 05:00:00', '2024-05-03 20:00:00', 'Curitiba', 'Belo Horizonte', 1000.00);

-- Inserindo Manutenções (Para testar o Cronograma e Custos)
INSERT INTO manutencoes (veiculo_id, data_inicio, data_finalizacao, tipo_servico, custo_estimado, status) VALUES 
(1, '2024-06-10', '2024-06-11', 'Troca de Óleo', 350.00, 'PENDENTE'),
(2, '2024-06-15', '2024-06-17', 'Revisão de Freios', 1500.00, 'PENDENTE'),
(3, '2024-05-20', '2024-05-20', 'Troca de Pneus', 2200.00, 'CONCLUIDA');