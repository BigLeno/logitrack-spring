package com.logitrack.controller;

import com.logitrack.domain.StatusManutencao;
import com.logitrack.dto.ManutencaoForm;
import com.logitrack.exception.RegraNegocioException;
import com.logitrack.service.ManutencaoService;
import com.logitrack.service.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manutencoes")
public class ManutencaoController {

    private static final String VIEW_FORM = "manutencoes/form";

    private final ManutencaoService manutencaoService;
    private final VeiculoService veiculoService;

    public ManutencaoController(ManutencaoService manutencaoService, VeiculoService veiculoService) {
        this.manutencaoService = manutencaoService;
        this.veiculoService = veiculoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("manutencoes", manutencaoService.listar());
        return "manutencoes/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("form", new ManutencaoForm());
        return exibirFormulario(model, null, List.of(StatusManutencao.values()));
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ManutencaoForm form, BindingResult erros,
                        Model model, RedirectAttributes redirect) {
        List<StatusManutencao> opcoesStatus = List.of(StatusManutencao.values());
        if (erros.hasErrors()) {
            return exibirFormulario(model, null, opcoesStatus);
        }
        try {
            manutencaoService.criar(form);
        } catch (RegraNegocioException e) {
            erros.rejectValue(e.getCampo(), "regraNegocio", e.getMessage());
            return exibirFormulario(model, null, opcoesStatus);
        }
        redirect.addFlashAttribute("mensagem", "Manutenção agendada com sucesso.");
        return "redirect:/manutencoes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("form", manutencaoService.buscarParaEdicao(id));
        return exibirFormulario(model, id, manutencaoService.statusPermitidos(id));
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @Valid @ModelAttribute("form") ManutencaoForm form,
                            BindingResult erros, Model model, RedirectAttributes redirect) {
        if (erros.hasErrors()) {
            return exibirFormulario(model, id, manutencaoService.statusPermitidos(id));
        }
        try {
            manutencaoService.atualizar(id, form);
        } catch (RegraNegocioException e) {
            erros.rejectValue(e.getCampo(), "regraNegocio", e.getMessage());
            return exibirFormulario(model, id, manutencaoService.statusPermitidos(id));
        }
        redirect.addFlashAttribute("mensagem", "Manutenção atualizada com sucesso.");
        return "redirect:/manutencoes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            manutencaoService.excluir(id);
            redirect.addFlashAttribute("mensagem", "Manutenção excluída.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/manutencoes";
    }

    private String exibirFormulario(Model model, Integer id, List<StatusManutencao> opcoesStatus) {
        model.addAttribute("manutencaoId", id);
        model.addAttribute("veiculos", veiculoService.listarOpcoes());
        model.addAttribute("opcoesStatus", opcoesStatus);
        return VIEW_FORM;
    }
}
