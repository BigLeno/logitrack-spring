package com.logitrack.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/** Tudo o que a página do dashboard exibe, montado pelo DashboardService. */
public record DashboardDados(
        BigDecimal totalKm,
        Integer veiculoFiltradoId,
        List<VolumeCategoria> volumePorCategoria,
        List<ProximaManutencao> proximasManutencoes,
        VeiculoKm veiculoMaisRodado,
        BigDecimal custoPrevistoMes) {
}
