package com.logitrack.repository;

import com.logitrack.domain.Manutencao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Integer> {

    // Traz o veículo no mesmo SELECT (JOIN), evitando uma consulta extra por linha da listagem (N+1).
    @EntityGraph(attributePaths = "veiculo")
    List<Manutencao> findAllByOrderByDataInicioDescIdDesc();
}
