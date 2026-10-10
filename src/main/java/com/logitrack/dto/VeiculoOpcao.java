package com.logitrack.dto;

import com.logitrack.domain.Veiculo;

/** Item do seletor de veículos nas telas. */
public record VeiculoOpcao(Integer id, String descricao) {

    public static VeiculoOpcao de(Veiculo veiculo) {
        return new VeiculoOpcao(veiculo.getId(), veiculo.getPlaca() + " — " + veiculo.getModelo());
    }
}
