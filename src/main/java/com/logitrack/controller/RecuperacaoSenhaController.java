package com.logitrack.controller;

import com.logitrack.service.RecuperacaoSenhaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Esboço do "Esqueci minha senha" em duas etapas: informar usuário/e-mail e digitar o código.
 * A view recuperar-senha.html mostra a etapa do código quando há "emailMascarado" no model.
 */
@Controller
@RequestMapping("/recuperar-senha")
public class RecuperacaoSenhaController {

    private static final String VIEW = "recuperar-senha";

    private final RecuperacaoSenhaService recuperacaoSenhaService;

    public RecuperacaoSenhaController(RecuperacaoSenhaService recuperacaoSenhaService) {
        this.recuperacaoSenhaService = recuperacaoSenhaService;
    }

    @GetMapping
    public String formulario() {
        return VIEW;
    }

    @PostMapping
    public String solicitarCodigo(@RequestParam(defaultValue = "") String identificacao,
                                  @RequestParam(defaultValue = "false") boolean reenvio,
                                  Model model) {
        if (identificacao.isBlank()) {
            model.addAttribute("erro", "Informe seu usuário ou e-mail.");
            return VIEW;
        }
        model.addAttribute("identificacao", identificacao.trim());
        model.addAttribute("emailMascarado", recuperacaoSenhaService.enviarCodigo(identificacao.trim()));
        model.addAttribute("reenviado", reenvio);
        return VIEW;
    }

    @PostMapping("/codigo")
    public String verificarCodigo(@RequestParam(defaultValue = "") String identificacao,
                                  @RequestParam(defaultValue = "") String codigo,
                                  Model model) {
        if (!recuperacaoSenhaService.codigoValido(codigo.trim())) {
            model.addAttribute("identificacao", identificacao);
            model.addAttribute("emailMascarado", recuperacaoSenhaService.mascararEmail(identificacao));
            model.addAttribute("erro", "Digite os 6 números do código enviado.");
            return VIEW;
        }
        return "redirect:/login?recuperacao";
    }
}
