package com.logitrack.dto;

import com.logitrack.domain.Manutencao;
import com.logitrack.domain.StatusManutencao;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados do formulário de criação/edição. Classe mutável (getters e setters)
 * porque o Spring preenche os campos a partir dos parâmetros do POST.
 */
public class ManutencaoForm {

    @NotNull(message = "Selecione o veículo")
    private Integer veiculoId;

    @NotNull(message = "Informe a data de início")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFinalizacao;

    @NotBlank(message = "Informe o tipo de serviço")
    @Size(max = 100, message = "Use no máximo 100 caracteres")
    private String tipoServico;

    @NotNull(message = "Informe o custo estimado")
    @PositiveOrZero(message = "O custo não pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Use até 8 dígitos e 2 casas decimais")
    private BigDecimal custoEstimado;

    @NotNull(message = "Selecione o status")
    private StatusManutencao status = StatusManutencao.PENDENTE;

    // Validação que envolve dois campos. O Bean Validation chama este getter e, se der false,
    // registra o erro na propriedade "periodoValido", exibida junto da data de finalização.
    @AssertTrue(message = "A finalização prevista não pode ser anterior à data de início")
    public boolean isPeriodoValido() {
        return dataInicio == null || dataFinalizacao == null || !dataFinalizacao.isBefore(dataInicio);
    }

    public static ManutencaoForm de(Manutencao m) {
        ManutencaoForm form = new ManutencaoForm();
        form.setVeiculoId(m.getVeiculo().getId());
        form.setDataInicio(m.getDataInicio());
        form.setDataFinalizacao(m.getDataFinalizacao());
        form.setTipoServico(m.getTipoServico());
        form.setCustoEstimado(m.getCustoEstimado());
        form.setStatus(m.getStatus());
        return form;
    }

    public Integer getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(Integer veiculoId) {
        this.veiculoId = veiculoId;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public String getTipoServico() {
        return tipoServico;
    }

    public void setTipoServico(String tipoServico) {
        this.tipoServico = tipoServico;
    }

    public BigDecimal getCustoEstimado() {
        return custoEstimado;
    }

    public void setCustoEstimado(BigDecimal custoEstimado) {
        this.custoEstimado = custoEstimado;
    }

    public StatusManutencao getStatus() {
        return status;
    }

    public void setStatus(StatusManutencao status) {
        this.status = status;
    }
}
