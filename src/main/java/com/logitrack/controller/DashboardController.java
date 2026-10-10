package com.logitrack.controller;

import com.logitrack.service.DashboardService;
import com.logitrack.service.VeiculoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final VeiculoService veiculoService;

    public DashboardController(DashboardService dashboardService, VeiculoService veiculoService) {
        this.dashboardService = dashboardService;
        this.veiculoService = veiculoService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Integer veiculoId, Model model) {
        model.addAttribute("dados", dashboardService.carregar(veiculoId));
        model.addAttribute("veiculos", veiculoService.listarOpcoes());
        return "dashboard";
    }
}
