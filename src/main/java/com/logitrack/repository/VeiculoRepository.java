package com.logitrack.repository;

import com.logitrack.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {

    List<Veiculo> findAllByOrderByPlacaAsc();
}
