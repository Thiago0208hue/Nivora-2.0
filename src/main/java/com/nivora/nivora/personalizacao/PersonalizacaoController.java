package com.nivora.nivora.personalizacao;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personalizacao")
public class PersonalizacaoController {

    private final PersonalizacaoService service;

    public PersonalizacaoController(PersonalizacaoService service) {
        this.service = service;
    }

    /** POST porque o navegador envia o desempenho local; o cálculo em si não altera nada. */
    @PostMapping("/prioridades")
    public RecomendacaoResponse prioridades(Principal usuarioLogado,
                                            @Valid @RequestBody(required = false) PersonalizacaoRequest pedido) {
        return service.recomendar(usuarioLogado.getName(), pedido == null ? null : pedido.desempenho());
    }
}
