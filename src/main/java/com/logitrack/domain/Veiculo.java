package com.logitrack.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String placa;

    private String modelo;

    @Enumerated(EnumType.STRING)
    private TipoVeiculo tipo;

    private Integer ano;

    protected Veiculo() {
        // exigido pelo JPA
    }

    public Integer getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public String getModelo() {
        return modelo;
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }

    public Integer getAno() {
        return ano;
    }
}
