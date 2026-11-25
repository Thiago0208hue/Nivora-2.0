package com.nivora.nivora.config;

import jakarta.servlet.ServletException;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Transforma exceções em respostas JSON no formato {"erro": "...", "campos": {...}}. */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> aoValidar(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", "Dados inválidos.");
        corpo.put("campos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> aoLerCorpo(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.");
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, Object>> aoViolarRegra(NegocioException e) {
        return resposta(e.getStatus(), e.getMessage());
    }

    /** Qualquer falha de autenticação vira a mesma mensagem, para não revelar se o e-mail existe. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> aoFalharLogin(AuthenticationException e) {
        return resposta(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> aoViolarBanco(DataIntegrityViolationException e) {
        return resposta(HttpStatus.CONFLICT, "Dados já cadastrados ou inválidos.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> aoMisturarTipos(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> aoUsarMetodoErrado(HttpRequestMethodNotSupportedException e) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> aoNaoEncontrar(NoResourceFoundException e) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> aoNegarAcesso(AccessDeniedException e) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado.");
    }

    /** Demais erros de requisição do Spring MVC (tipo de conteúdo, parâmetro ausente...) com o status que ele já definiu. */
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<Map<String, Object>> aoErrarRequisicao(ServletException e) {
        HttpStatus status = e instanceof ErrorResponse er && HttpStatus.resolve(er.getStatusCode().value()) != null
                ? HttpStatus.resolve(er.getStatusCode().value()) : HttpStatus.BAD_REQUEST;
        return resposta(status, "Requisição inválida.");
    }

    /** Último recurso: registra o erro no log do servidor e devolve mensagem genérica, sem detalhes internos. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> aoFalharInesperadamente(Exception e) {
        LOG.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
