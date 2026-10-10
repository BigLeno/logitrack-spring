package com.logitrack.dto.dashboard;

import java.math.BigDecimal;

public record VeiculoKm(Integer id, String placa, String modelo, BigDecimal kmTotal) {
}
