package com.nivora.nivora.plano;

import com.nivora.nivora.personalizacao.PersonalizacaoRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plano")
public class PlanoController {

    private final PlanoService service;

    public PlanoController(PlanoService service) {
        this.service = service;
    }

    /** POST porque o navegador envia o desempenho local (até a Etapa 8); nada é alterado no servidor. */
    @PostMapping("/semanal")
    public PlanoResponse semanal(Principal usuarioLogado,
                                 @Valid @RequestBody(required = false) PersonalizacaoRequest pedido) {
        return service.gerarSemana(usuarioLogado.getName(), pedido == null ? null : pedido.desempenho());
    }
}
