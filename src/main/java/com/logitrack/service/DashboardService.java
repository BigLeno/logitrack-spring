package com.logitrack.service;

import com.logitrack.dto.dashboard.DashboardDados;
import com.logitrack.repository.DashboardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DashboardService {

    private static final int QUANTIDADE_PROXIMAS_MANUTENCOES = 5;

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    /**
     * @param veiculoId filtro opcional do total de KM; null soma a frota inteira.
     */
    // Uma transação só para as 5 consultas: todas leem o mesmo snapshot do banco (REPEATABLE READ),
    // então os números do painel são consistentes entre si mesmo com gravações acontecendo.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public DashboardDados carregar(Integer veiculoId) {
        BigDecimal totalKm = veiculoId == null
                ? dashboardRepository.totalKmFrota()
                : dashboardRepository.totalKmVeiculo(veiculoId);

        return new DashboardDados(
                totalKm,
                veiculoId,
                dashboardRepository.volumePorCategoria(),
                dashboardRepository.proximasManutencoes(QUANTIDADE_PROXIMAS_MANUTENCOES),
                dashboardRepository.veiculoMaisRodado().orElse(null),
                dashboardRepository.custoPrevistoMesAtual());
    }
}
