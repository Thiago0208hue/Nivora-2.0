package com.nivora.nivora.personalizacao;

import com.nivora.nivora.desempenho.DesempenhoService;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personalizacao")
public class PersonalizacaoController {

    private final PersonalizacaoService service;
    private final DesempenhoService desempenho;

    public PersonalizacaoController(PersonalizacaoService service, DesempenhoService desempenho) {
        this.service = service;
        this.desempenho = desempenho;
    }

    /** O desempenho vem do banco (respostas gravadas); qualquer desempenho enviado pelo navegador é ignorado. */
    @PostMapping("/prioridades")
    public RecomendacaoResponse prioridades(Principal usuarioLogado) {
        return service.recomendar(usuarioLogado.getName(), desempenho.porArea(usuarioLogado.getName()));
    }
}
