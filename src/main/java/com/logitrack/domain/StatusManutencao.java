package com.logitrack.domain;

import java.util.Arrays;
import java.util.List;

public enum StatusManutencao {

    PENDENTE("Pendente"),
    EM_REALIZACAO("Em realização"),
    CONCLUIDA("Concluída");

    private final String descricao;

    StatusManutencao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Fluxo permitido: PENDENTE -> EM_REALIZACAO -> CONCLUIDA, e PENDENTE -> CONCLUIDA.
     * Manter o mesmo status é sempre permitido (edição de outros campos).
     */
    public boolean podeMudarPara(StatusManutencao novo) {
        if (this == novo) {
            return true;
        }
        return switch (this) {
            case PENDENTE -> novo == EM_REALIZACAO || novo == CONCLUIDA;
            case EM_REALIZACAO -> novo == CONCLUIDA;
            case CONCLUIDA -> false;
        };
    }

    /** Manutenção concluída é histórico (custo e cronograma realizados): não pode ser excluída. */
    public boolean permiteExclusao() {
        return this != CONCLUIDA;
    }

    public List<StatusManutencao> statusAlcancaveis() {
        return Arrays.stream(values()).filter(this::podeMudarPara).toList();
    }
}
