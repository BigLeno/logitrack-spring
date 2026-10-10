package com.logitrack.repository;

import com.logitrack.dto.dashboard.ProximaManutencao;
import com.logitrack.dto.dashboard.VeiculoKm;
import com.logitrack.dto.dashboard.VolumeCategoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração das consultas do dashboard contra um PostgreSQL real (Testcontainers),
 * com o schema criado pelas mesmas migrations do Flyway usadas pela aplicação.
 * <p>
 * Cada teste roda numa transação desfeita ao final (@JdbcTest), começa apagando viagens e
 * manutenções da carga inicial e insere só os dados de que precisa. Os 4 veículos da V1 são mantidos.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DashboardRepository.class)
@Testcontainers
class DashboardRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DashboardRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    /** "Hoje" segundo o banco, a mesma referência que o CURRENT_DATE das consultas usa. */
    private LocalDate hoje;

    @BeforeEach
    void limparDados() {
        jdbc.update("DELETE FROM viagens");
        jdbc.update("DELETE FROM manutencoes");
        hoje = jdbc.queryForObject("SELECT CURRENT_DATE", LocalDate.class);
    }

    @Nested
    @DisplayName("Total de KM")
    class TotalKm {

        @Test
        @DisplayName("sem viagens retorna zero, e não null")
        void semViagensRetornaZero() {
            assertThat(repository.totalKmFrota()).isEqualByComparingTo("0");
            assertThat(repository.totalKmVeiculo(idDoVeiculo("ABC-1234"))).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("soma a frota inteira ou só o veículo filtrado")
        void somaFrotaOuVeiculo() {
            inserirViagem("ABC-1234", "100.50");
            inserirViagem("ABC-1234", "20.00");
            inserirViagem("XYZ-9876", "300.25");

            assertThat(repository.totalKmFrota()).isEqualByComparingTo("420.75");
            assertThat(repository.totalKmVeiculo(idDoVeiculo("ABC-1234"))).isEqualByComparingTo("120.50");
            assertThat(repository.totalKmVeiculo(idDoVeiculo("KJG-1122"))).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("Volume por categoria")
    class VolumePorCategoria {

        @Test
        @DisplayName("conta viagens por tipo de veículo")
        void contaViagensPorTipo() {
            inserirViagem("ABC-1234", "10"); // LEVE
            inserirViagem("KJG-1122", "10"); // LEVE
            inserirViagem("KJG-1122", "10"); // LEVE
            inserirViagem("XYZ-9876", "10"); // PESADO

            assertThat(repository.volumePorCategoria()).containsExactly(
                    new VolumeCategoria("LEVE", 3),
                    new VolumeCategoria("PESADO", 1));
        }

        @Test
        @DisplayName("mostra a categoria sem viagens com zero")
        void categoriaSemViagensApareceComZero() {
            inserirViagem("ABC-1234", "10"); // só LEVE

            assertThat(repository.volumePorCategoria()).containsExactly(
                    new VolumeCategoria("LEVE", 1),
                    new VolumeCategoria("PESADO", 0));
        }
    }

    @Nested
    @DisplayName("Próximas manutenções")
    class ProximasManutencoes {

        @Test
        @DisplayName("lista só PENDENTE de hoje em diante, ordenadas por data e limitadas")
        void filtraOrdenaELimita() {
            inserirManutencao("ABC-1234", hoje.plusDays(9), "PENDENTE", "100", "Nona");
            inserirManutencao("ABC-1234", hoje.plusDays(3), "PENDENTE", "100", "Terceira");
            inserirManutencao("ABC-1234", hoje, "PENDENTE", "100", "Hoje");
            inserirManutencao("ABC-1234", hoje.plusDays(1), "PENDENTE", "100", "Amanhã");
            inserirManutencao("ABC-1234", hoje.plusDays(5), "PENDENTE", "100", "Quinta");
            inserirManutencao("ABC-1234", hoje.plusDays(7), "PENDENTE", "100", "Sétima");
            // fora do cronograma:
            inserirManutencao("ABC-1234", hoje.minusDays(1), "PENDENTE", "100", "Atrasada");
            inserirManutencao("ABC-1234", hoje.plusDays(2), "EM_REALIZACAO", "100", "Em realização");
            inserirManutencao("ABC-1234", hoje.plusDays(2), "CONCLUIDA", "100", "Concluída");

            List<ProximaManutencao> proximas = repository.proximasManutencoes(5);

            assertThat(proximas)
                    .extracting(ProximaManutencao::tipoServico)
                    .containsExactly("Hoje", "Amanhã", "Terceira", "Quinta", "Sétima");
        }

        @Test
        @DisplayName("traz placa e modelo do veículo")
        void trazDadosDoVeiculo() {
            inserirManutencao("LMN-4455", hoje.plusDays(1), "PENDENTE", "999.90", "Revisão");

            ProximaManutencao m = repository.proximasManutencoes(5).get(0);

            assertThat(m.placa()).isEqualTo("LMN-4455");
            assertThat(m.modelo()).isEqualTo("Scania R500");
            assertThat(m.dataInicio()).isEqualTo(hoje.plusDays(1));
            assertThat(m.custoEstimado()).isEqualByComparingTo("999.90");
            assertThat(m.diasAte()).isEqualTo(1);
        }

        @Test
        @DisplayName("sem manutenções futuras retorna lista vazia")
        void semFuturasRetornaVazio() {
            inserirManutencao("ABC-1234", hoje.minusDays(10), "PENDENTE", "100", "Antiga");

            assertThat(repository.proximasManutencoes(5)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Veículo com mais KM")
    class VeiculoMaisRodado {

        @Test
        @DisplayName("retorna o veículo com a maior soma de quilometragem")
        void maiorSoma() {
            inserirViagem("ABC-1234", "500");
            inserirViagem("ABC-1234", "600"); // 1100 no total
            inserirViagem("XYZ-9876", "1000"); // maior viagem isolada, mas menor soma

            VeiculoKm veiculo = repository.veiculoMaisRodado().orElseThrow();

            assertThat(veiculo.placa()).isEqualTo("ABC-1234");
            assertThat(veiculo.kmTotal()).isEqualByComparingTo("1100");
        }

        @Test
        @DisplayName("em caso de empate, desempata pela placa")
        void empateDesempataPelaPlaca() {
            inserirViagem("XYZ-9876", "700");
            inserirViagem("KJG-1122", "700");

            assertThat(repository.veiculoMaisRodado()).get()
                    .extracting(VeiculoKm::placa)
                    .isEqualTo("KJG-1122");
        }

        @Test
        @DisplayName("sem viagens retorna vazio")
        void semViagensRetornaVazio() {
            assertThat(repository.veiculoMaisRodado()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Projeção financeira do mês")
    class CustoPrevistoMes {

        @Test
        @DisplayName("soma do primeiro ao último dia do mês atual, qualquer status")
        void somaApenasOMesAtual() {
            LocalDate primeiroDia = hoje.withDayOfMonth(1);
            LocalDate ultimoDia = hoje.withDayOfMonth(hoje.lengthOfMonth());

            inserirManutencao("ABC-1234", primeiroDia, "CONCLUIDA", "100.00", "Primeiro dia");
            inserirManutencao("ABC-1234", ultimoDia, "PENDENTE", "200.00", "Último dia");
            inserirManutencao("ABC-1234", hoje, "EM_REALIZACAO", "50.25", "Hoje");
            // fora do mês:
            inserirManutencao("ABC-1234", primeiroDia.minusDays(1), "PENDENTE", "1000.00", "Mês anterior");
            inserirManutencao("ABC-1234", ultimoDia.plusDays(1), "PENDENTE", "1000.00", "Mês seguinte");

            assertThat(repository.custoPrevistoMesAtual()).isEqualByComparingTo("350.25");
        }

        @Test
        @DisplayName("sem manutenções no mês retorna zero")
        void semManutencoesRetornaZero() {
            assertThat(repository.custoPrevistoMesAtual()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    private int idDoVeiculo(String placa) {
        return jdbc.queryForObject("SELECT id FROM veiculos WHERE placa = ?", Integer.class, placa);
    }

    private void inserirViagem(String placa, String km) {
        jdbc.update("""
                INSERT INTO viagens (veiculo_id, data_saida, data_chegada, origem, destino, km_percorrida)
                VALUES (?, now(), now(), 'Origem', 'Destino', ?)
                """, idDoVeiculo(placa), new BigDecimal(km));
    }

    private void inserirManutencao(String placa, LocalDate dataInicio, String status, String custo, String servico) {
        jdbc.update("""
                INSERT INTO manutencoes (veiculo_id, data_inicio, tipo_servico, custo_estimado, status)
                VALUES (?, ?, ?, ?, ?)
                """, idDoVeiculo(placa), dataInicio, servico, new BigDecimal(custo), status);
    }
}
