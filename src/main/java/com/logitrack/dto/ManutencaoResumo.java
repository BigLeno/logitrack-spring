package com.logitrack.dto;

import com.logitrack.domain.Manutencao;
import com.logitrack.domain.StatusManutencao;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Linha da listagem de manutenções. */
public record ManutencaoResumo(
        Integer id,
        String placa,
        String modelo,
        LocalDate dataInicio,
        LocalDate dataFinalizacao,
        String tipoServico,
        BigDecimal custoEstimado,
        StatusManutencao status) {

    public static ManutencaoResumo de(Manutencao m) {
        return new ManutencaoResumo(
                m.getId(),
                m.getVeiculo().getPlaca(),
                m.getVeiculo().getModelo(),
                m.getDataInicio(),
                m.getDataFinalizacao(),
                m.getTipoServico(),
                m.getCustoEstimado(),
                m.getStatus());
    }
}
