package com.logitrack.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProximaManutencao(
        Integer id,
        String placa,
        String modelo,
        LocalDate dataInicio,
        LocalDate dataFinalizacao,
        String tipoServico,
        BigDecimal custoEstimado,
        int diasAte) {
}
