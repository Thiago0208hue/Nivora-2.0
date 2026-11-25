package com.nivora.nivora.redacao;

import com.nivora.nivora.redacao.RedacaoDtos.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/redacao")
public class RedacaoController {

    private final RedacaoService service;

    public RedacaoController(RedacaoService service) {
        this.service = service;
    }

    @GetMapping("/temas")
    public List<TemaResponse> temas() {
        return service.temas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RedacaoResponse salvar(Principal u, @Valid @RequestBody RedacaoRequest pedido) {
        return service.salvar(u.getName(), pedido);
    }

    @GetMapping
    public List<RedacaoResumo> historico(Principal u) {
        return service.historico(u.getName());
    }

    @GetMapping("/{id}")
    public RedacaoResponse buscar(Principal u, @PathVariable Long id) {
        return service.buscar(u.getName(), id);
    }
}
