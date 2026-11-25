package com.nivora.nivora.desempenho;

import com.nivora.nivora.desempenho.DesempenhoDtos.DesempenhoResponse;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/desempenho")
public class DesempenhoController {

    private final DesempenhoService service;

    public DesempenhoController(DesempenhoService service) {
        this.service = service;
    }

    @GetMapping
    public DesempenhoResponse resumo(Principal usuarioLogado) {
        return service.resumo(usuarioLogado.getName());
    }
}
