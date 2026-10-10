package com.logitrack.service;

import com.logitrack.dto.VeiculoOpcao;
import com.logitrack.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<VeiculoOpcao> listarOpcoes() {
        return veiculoRepository.findAllByOrderByPlacaAsc().stream()
                .map(VeiculoOpcao::de)
                .toList();
    }
}
