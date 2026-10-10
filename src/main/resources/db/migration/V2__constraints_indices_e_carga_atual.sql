-- =====================================================================
-- V2: integridade, índices e dados de exemplo com datas relativas
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Constraints de integridade
-- ---------------------------------------------------------------------

ALTER TABLE veiculos
    ALTER COLUMN tipo SET NOT NULL;

-- Sem veículo, viagem e manutenção não fazem sentido (e sumiriam das métricas por veículo).
ALTER TABLE viagens
    ALTER COLUMN veiculo_id SET NOT NULL,
    ADD CONSTRAINT chk_viagens_chegada_apos_saida CHECK (data_chegada >= data_saida),
    ADD CONSTRAINT chk_viagens_km_nao_negativo CHECK (km_percorrida >= 0);

ALTER TABLE manutencoes
    ALTER COLUMN veiculo_id SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ADD CONSTRAINT chk_manutencoes_status CHECK (status IN ('PENDENTE', 'EM_REALIZACAO', 'CONCLUIDA')),
    ADD CONSTRAINT chk_manutencoes_finalizacao_apos_inicio CHECK (data_finalizacao >= data_inicio),
    ADD CONSTRAINT chk_manutencoes_custo_nao_negativo CHECK (custo_estimado >= 0);

-- ---------------------------------------------------------------------
-- 2. ON DELETE CASCADE -> RESTRICT
--    Excluir um veículo não pode apagar seu histórico: isso mudaria
--    o KM total e o ranking retroativamente.
-- ---------------------------------------------------------------------

ALTER TABLE viagens
    DROP CONSTRAINT viagens_veiculo_id_fkey,
    ADD CONSTRAINT viagens_veiculo_id_fkey
        FOREIGN KEY (veiculo_id) REFERENCES veiculos (id) ON DELETE RESTRICT;

ALTER TABLE manutencoes
    DROP CONSTRAINT manutencoes_veiculo_id_fkey,
    ADD CONSTRAINT manutencoes_veiculo_id_fkey
        FOREIGN KEY (veiculo_id) REFERENCES veiculos (id) ON DELETE RESTRICT;

-- ---------------------------------------------------------------------
-- 3. Índices
--    O PostgreSQL indexa PKs e UNIQUE, mas não cria índice para FKs.
-- ---------------------------------------------------------------------

CREATE INDEX idx_viagens_veiculo_id ON viagens (veiculo_id);
CREATE INDEX idx_manutencoes_veiculo_id ON manutencoes (veiculo_id);

-- Usado pelo cronograma (data_inicio >= hoje ORDER BY data_inicio)
-- e pela projeção do mês (intervalo de datas em data_inicio).
CREATE INDEX idx_manutencoes_data_inicio ON manutencoes (data_inicio);

-- ---------------------------------------------------------------------
-- 4. Dados de exemplo com datas relativas à data da migração
--    Os dados originais são de 2024, o que deixaria vazios o cronograma
--    e a projeção do mês. Os veículos são buscados pela placa para não
--    depender dos ids gerados.
-- ---------------------------------------------------------------------

INSERT INTO viagens (veiculo_id, data_saida, data_chegada, origem, destino, km_percorrida)
SELECT v.id, d.data_saida, d.data_chegada, d.origem, d.destino, d.km
FROM (VALUES
    ('ABC-1234', (CURRENT_DATE - 40) + TIME '07:00', (CURRENT_DATE - 40) + TIME '10:30', 'São Paulo',      'Campinas',       180.00),
    ('ABC-1234', (CURRENT_DATE - 22) + TIME '08:00', (CURRENT_DATE - 22) + TIME '10:00', 'Campinas',       'Sorocaba',        95.50),
    ('ABC-1234', (CURRENT_DATE - 6)  + TIME '06:30', (CURRENT_DATE - 6)  + TIME '13:00', 'São Paulo',      'Curitiba',       410.00),
    ('XYZ-9876', (CURRENT_DATE - 35) + TIME '05:00', (CURRENT_DATE - 34) + TIME '09:00', 'Belo Horizonte', 'Brasília',       830.00),
    ('XYZ-9876', (CURRENT_DATE - 12) + TIME '04:00', (CURRENT_DATE - 11) + TIME '18:00', 'Brasília',       'Goiânia',       1150.00),
    ('KJG-1122', (CURRENT_DATE - 30) + TIME '09:00', (CURRENT_DATE - 30) + TIME '13:00', 'Rio de Janeiro', 'Petrópolis',     220.00),
    ('KJG-1122', (CURRENT_DATE - 15) + TIME '07:30', (CURRENT_DATE - 15) + TIME '13:15', 'Rio de Janeiro', 'Juiz de Fora',   310.75),
    ('KJG-1122', (CURRENT_DATE - 3)  + TIME '10:00', (CURRENT_DATE - 3)  + TIME '12:30', 'Niterói',        'Cabo Frio',      145.00),
    ('LMN-4455', (CURRENT_DATE - 45) + TIME '03:00', (CURRENT_DATE - 43) + TIME '20:00', 'Porto Alegre',   'São Paulo',     1320.00),
    ('LMN-4455', (CURRENT_DATE - 25) + TIME '05:00', (CURRENT_DATE - 24) + TIME '16:00', 'São Paulo',      'Salvador',       980.00),
    ('LMN-4455', (CURRENT_DATE - 8)  + TIME '04:30', (CURRENT_DATE - 7)  + TIME '22:00', 'Salvador',       'Recife',        1210.50)
) AS d (placa, data_saida, data_chegada, origem, destino, km)
JOIN veiculos v ON v.placa = d.placa;

INSERT INTO manutencoes (veiculo_id, data_inicio, data_finalizacao, tipo_servico, custo_estimado, status)
SELECT v.id, d.data_inicio, d.data_finalizacao, d.tipo_servico, d.custo, d.status
FROM (VALUES
    -- futuras e pendentes: alimentam o cronograma (as primeiras caem no mês atual)
    ('ABC-1234', CURRENT_DATE,      CURRENT_DATE + 1,  'Troca de Óleo',               380.00, 'PENDENTE'),
    ('XYZ-9876', CURRENT_DATE + 3,  CURRENT_DATE + 5,  'Revisão de Freios',          1850.00, 'PENDENTE'),
    ('KJG-1122', CURRENT_DATE + 6,  CURRENT_DATE + 6,  'Alinhamento e Balanceamento', 260.00, 'PENDENTE'),
    ('LMN-4455', CURRENT_DATE + 10, CURRENT_DATE + 13, 'Revisão do Motor',           6200.00, 'PENDENTE'),
    ('ABC-1234', CURRENT_DATE + 18, CURRENT_DATE + 19, 'Troca de Pneus',             2400.00, 'PENDENTE'),
    ('XYZ-9876', CURRENT_DATE + 35, CURRENT_DATE + 37, 'Troca de Embreagem',         4800.00, 'PENDENTE'),
    ('LMN-4455', CURRENT_DATE + 50, CURRENT_DATE + 51, 'Troca de Óleo',               900.00, 'PENDENTE'),
    -- já iniciadas: contam na projeção do mês, mas não no cronograma
    ('KJG-1122', date_trunc('month', CURRENT_DATE)::date, date_trunc('month', CURRENT_DATE)::date + 1, 'Troca de Pastilhas', 540.00, 'CONCLUIDA'),
    ('LMN-4455', CURRENT_DATE - 2,  CURRENT_DATE + 1,  'Reparo no Sistema Elétrico', 1300.00, 'EM_REALIZACAO')
) AS d (placa, data_inicio, data_finalizacao, tipo_servico, custo, status)
JOIN veiculos v ON v.placa = d.placa;
