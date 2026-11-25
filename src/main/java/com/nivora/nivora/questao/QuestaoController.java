package com.nivora.nivora.questao;

import com.nivora.nivora.questao.QuestaoDtos.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questoes")
public class QuestaoController {

    private final QuestaoService service;

    public QuestaoController(QuestaoService service) {
        this.service = service;
    }

    @GetMapping("/catalogo")
    public List<CatalogoResponse> catalogo() {
        return service.catalogo();
    }

    /** POST porque os filtros vêm no corpo; não altera nada. */
    @PostMapping("/praticar")
    public List<QuestaoResponse> praticar(Principal u, @Valid @RequestBody PraticaRequest pedido) {
        return service.praticar(u.getName(), pedido);
    }

    @PostMapping("/{id}/responder")
    public CorrecaoResponse responder(Principal u, @PathVariable Long id, @Valid @RequestBody RespostaRequest r) {
        return service.responder(u.getName(), id, r.letra());
    }

    @GetMapping("/revisao")
    public List<RevisaoResponse> revisao(Principal u) {
        return service.revisao(u.getName());
    }
}
