package com.nivora.nivora.questao;

import com.nivora.nivora.questao.QuestaoDtos.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulados")
public class SimuladoController {

    private final SimuladoService service;

    public SimuladoController(SimuladoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimuladoResponse criar(Principal u, @Valid @RequestBody SimuladoRequest pedido) {
        return service.criar(u.getName(), pedido);
    }

    @GetMapping
    public List<SimuladoResumo> historico(Principal u) {
        return service.historico(u.getName());
    }

    @GetMapping("/{id}")
    public SimuladoResponse buscar(Principal u, @PathVariable Long id) {
        return service.buscar(u.getName(), id);
    }

    @PostMapping("/{id}/questoes/{questaoId}/responder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void responder(Principal u, @PathVariable Long id, @PathVariable Long questaoId,
                          @Valid @RequestBody RespostaRequest r) {
        service.responder(u.getName(), id, questaoId, r.letra());
    }

    @PostMapping("/{id}/finalizar")
    public ResultadoSimulado finalizar(Principal u, @PathVariable Long id) {
        return service.finalizar(u.getName(), id);
    }
}
