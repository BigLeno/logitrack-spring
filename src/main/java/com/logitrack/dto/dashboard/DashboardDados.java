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

    public long totalViagens() {
        return volumePorCategoria.stream().mapToLong(VolumeCategoria::totalViagens).sum();
    }

    /** Participação da categoria no total de viagens, em % arredondado (0 quando não há viagens). */
    public long percentual(VolumeCategoria categoria) {
        long total = totalViagens();
        return total == 0 ? 0 : Math.round(categoria.totalViagens() * 100.0 / total);
    }
}
