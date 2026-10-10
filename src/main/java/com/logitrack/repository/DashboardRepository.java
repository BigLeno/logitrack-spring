package com.logitrack.repository;

import com.logitrack.dto.dashboard.ProximaManutencao;
import com.logitrack.dto.dashboard.VeiculoKm;
import com.logitrack.dto.dashboard.VolumeCategoria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Métricas do dashboard em SQL puro, executadas com JdbcTemplate.
 * Cada consulta agrega no banco e devolve só o resultado (poucas linhas), já mapeado para um DTO.
 */
@Repository
public class DashboardRepository {

    private static final String TOTAL_KM_FROTA = """
            SELECT COALESCE(SUM(km_percorrida), 0)
            FROM viagens
            """;

    private static final String TOTAL_KM_VEICULO = """
            SELECT COALESCE(SUM(km_percorrida), 0)
            FROM viagens
            WHERE veiculo_id = ?
            """;

    // Parte da lista fixa de categorias com LEFT JOIN para que uma categoria sem viagens apareça com 0.
    private static final String VOLUME_POR_CATEGORIA = """
            SELECT t.tipo, COUNT(vg.id) AS total_viagens
            FROM (VALUES ('LEVE'), ('PESADO')) AS t (tipo)
            LEFT JOIN veiculos v ON v.tipo = t.tipo
            LEFT JOIN viagens vg ON vg.veiculo_id = v.id
            GROUP BY t.tipo
            ORDER BY t.tipo
            """;

    private static final String PROXIMAS_MANUTENCOES = """
            SELECT m.id, v.placa, v.modelo, m.data_inicio, m.data_finalizacao,
                   m.tipo_servico, m.custo_estimado
            FROM manutencoes m
            JOIN veiculos v ON v.id = m.veiculo_id
            WHERE m.status = 'PENDENTE'
              AND m.data_inicio >= CURRENT_DATE
            ORDER BY m.data_inicio, m.id
            LIMIT ?
            """;

    // Agrega as viagens por veículo antes do JOIN: o JOIN é feito com uma linha por veículo.
    private static final String VEICULO_MAIS_RODADO = """
            SELECT v.id, v.placa, v.modelo, t.km_total
            FROM (
                SELECT veiculo_id, SUM(km_percorrida) AS km_total
                FROM viagens
                GROUP BY veiculo_id
            ) t
            JOIN veiculos v ON v.id = t.veiculo_id
            ORDER BY t.km_total DESC, v.placa
            LIMIT 1
            """;

    // Intervalo [primeiro dia do mês, primeiro dia do mês seguinte) direto na coluna, para usar o índice em data_inicio.
    private static final String CUSTO_PREVISTO_MES = """
            SELECT COALESCE(SUM(custo_estimado), 0)
            FROM manutencoes
            WHERE data_inicio >= date_trunc('month', CURRENT_DATE)::date
              AND data_inicio <  (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month')::date
            """;

    private final JdbcTemplate jdbcTemplate;

    public DashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BigDecimal totalKmFrota() {
        return jdbcTemplate.queryForObject(TOTAL_KM_FROTA, BigDecimal.class);
    }

    public BigDecimal totalKmVeiculo(int veiculoId) {
        return jdbcTemplate.queryForObject(TOTAL_KM_VEICULO, BigDecimal.class, veiculoId);
    }

    public List<VolumeCategoria> volumePorCategoria() {
        return jdbcTemplate.query(VOLUME_POR_CATEGORIA, (rs, linha) -> new VolumeCategoria(
                rs.getString("tipo"),
                rs.getLong("total_viagens")));
    }

    public List<ProximaManutencao> proximasManutencoes(int limite) {
        return jdbcTemplate.query(PROXIMAS_MANUTENCOES, (rs, linha) -> new ProximaManutencao(
                rs.getInt("id"),
                rs.getString("placa"),
                rs.getString("modelo"),
                rs.getObject("data_inicio", LocalDate.class),
                rs.getObject("data_finalizacao", LocalDate.class),
                rs.getString("tipo_servico"),
                rs.getBigDecimal("custo_estimado")), limite);
    }

    public Optional<VeiculoKm> veiculoMaisRodado() {
        return jdbcTemplate.query(VEICULO_MAIS_RODADO, (rs, linha) -> new VeiculoKm(
                        rs.getInt("id"),
                        rs.getString("placa"),
                        rs.getString("modelo"),
                        rs.getBigDecimal("km_total")))
                .stream()
                .findFirst();
    }

    public BigDecimal custoPrevistoMesAtual() {
        return jdbcTemplate.queryForObject(CUSTO_PREVISTO_MES, BigDecimal.class);
    }
}
