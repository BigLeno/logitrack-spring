package com.logitrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Esboço do fluxo de recuperação de senha, apenas para demonstrar a experiência na interface.
 * Nenhum código é gerado ou enviado e nenhuma senha é alterada: o usuário é único e fica em memória.
 * Uma versão real geraria um código aleatório com validade, guardaria o hash dele e enviaria por e-mail.
 */
@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);

    // Domínio reservado para exemplos (RFC 2606): nunca corresponde a um e-mail real.
    private static final String DOMINIO_DEMONSTRACAO = "logitrack.example";

    /** Simula o envio do código e devolve o e-mail mascarado para exibir na tela. */
    public String enviarCodigo(String identificacao) {
        String emailMascarado = mascararEmail(identificacao);
        log.info("[demonstração] envio de código de recuperação simulado para {}", emailMascarado);
        return emailMascarado;
    }

    /** "gestor" -> "ge•••@logitrack.example"; um e-mail informado é mascarado da mesma forma. */
    public String mascararEmail(String identificacao) {
        String email = identificacao.contains("@") ? identificacao : identificacao + "@" + DOMINIO_DEMONSTRACAO;
        int arroba = email.indexOf('@');
        String local = email.substring(0, arroba);
        String visivel = local.isEmpty() ? "" : local.substring(0, Math.min(2, Math.max(1, local.length() - 1)));
        return visivel + "•••" + email.substring(arroba);
    }

    /** No esboço, qualquer código com 6 dígitos é aceito. */
    public boolean codigoValido(String codigo) {
        return codigo != null && codigo.matches("\\d{6}");
    }
}
