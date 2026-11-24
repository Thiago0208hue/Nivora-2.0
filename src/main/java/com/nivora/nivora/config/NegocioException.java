package com.nivora.nivora.config;

import org.springframework.http.HttpStatus;

/** Erro de regra de negócio, já com o status HTTP que deve ser devolvido. */
public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    public NegocioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
