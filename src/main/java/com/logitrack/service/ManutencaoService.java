package com.logitrack.service;

import com.logitrack.domain.Manutencao;
import com.logitrack.domain.StatusManutencao;
import com.logitrack.domain.Veiculo;
import com.logitrack.dto.ManutencaoForm;
import com.logitrack.dto.ManutencaoResumo;
import com.logitrack.exception.RecursoNaoEncontradoException;
import com.logitrack.exception.RegraNegocioException;
import com.logitrack.repository.ManutencaoRepository;
import com.logitrack.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final VeiculoRepository veiculoRepository;

    public ManutencaoService(ManutencaoRepository manutencaoRepository, VeiculoRepository veiculoRepository) {
        this.manutencaoRepository = manutencaoRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResumo> listar() {
        return manutencaoRepository.findAllByOrderByDataInicioDescIdDesc().stream()
                .map(ManutencaoResumo::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ManutencaoForm buscarParaEdicao(Integer id) {
        return ManutencaoForm.de(buscar(id));
    }

    /** Status que podem ser escolhidos ao editar: o atual e os que o fluxo permite a partir dele. */
    @Transactional(readOnly = true)
    public List<StatusManutencao> statusPermitidos(Integer id) {
        return buscar(id).getStatus().statusAlcancaveis();
    }

    @Transactional
    public Integer criar(ManutencaoForm form) {
        Manutencao manutencao = new Manutencao();
        preencher(manutencao, form);
        manutencao.setStatus(form.getStatus());
        return manutencaoRepository.save(manutencao).getId();
    }

    @Transactional
    public void atualizar(Integer id, ManutencaoForm form) {
        Manutencao manutencao = buscar(id);

        StatusManutencao atual = manutencao.getStatus();
        if (!atual.podeMudarPara(form.getStatus())) {
            throw new RegraNegocioException("status", "Não é permitido mudar de \""
                    + atual.getDescricao() + "\" para \"" + form.getStatus().getDescricao() + "\"");
        }

        preencher(manutencao, form);
        manutencao.setStatus(form.getStatus());
        // Sem save(): a entidade foi carregada nesta transação, então o JPA detecta as
        // alterações e gera o UPDATE sozinho no commit (dirty checking).
    }

    @Transactional
    public void excluir(Integer id) {
        manutencaoRepository.delete(buscar(id));
    }

    private Manutencao buscar(Integer id) {
        return manutencaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Manutenção " + id + " não encontrada"));
    }

    private void preencher(Manutencao manutencao, ManutencaoForm form) {
        Veiculo veiculo = veiculoRepository.findById(form.getVeiculoId())
                .orElseThrow(() -> new RegraNegocioException("veiculoId", "Veículo não encontrado"));

        manutencao.setVeiculo(veiculo);
        manutencao.setDataInicio(form.getDataInicio());
        manutencao.setDataFinalizacao(form.getDataFinalizacao());
        manutencao.setTipoServico(form.getTipoServico().trim());
        manutencao.setCustoEstimado(form.getCustoEstimado());
    }
}
