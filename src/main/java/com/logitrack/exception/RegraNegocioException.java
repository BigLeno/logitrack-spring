package com.logitrack.exception;

/**
 * Violação de regra de negócio. Guarda o campo do formulário relacionado
 * para que o controller exiba a mensagem junto dele.
 */
public class RegraNegocioException extends RuntimeException {

    private final String campo;

    public RegraNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
