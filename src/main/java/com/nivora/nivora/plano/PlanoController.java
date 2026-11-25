package com.nivora.nivora.plano;

import com.nivora.nivora.desempenho.DesempenhoService;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plano")
public class PlanoController {

    private final PlanoService service;
    private final DesempenhoService desempenho;

    public PlanoController(PlanoService service, DesempenhoService desempenho) {
        this.service = service;
        this.desempenho = desempenho;
    }

    /** O desempenho vem do banco; nada é alterado no servidor (POST só por convenção de "gerar"). */
    @PostMapping("/semanal")
    public PlanoResponse semanal(Principal usuarioLogado) {
        return service.gerarSemana(usuarioLogado.getName(), desempenho.porArea(usuarioLogado.getName()));
    }
}
