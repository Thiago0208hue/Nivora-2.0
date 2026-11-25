package com.nivora.nivora.questao;

import com.nivora.nivora.questao.QuestaoDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Protegido por hasRole("ADMIN") no SecurityConfig. */
@RestController
@RequestMapping("/api/admin/questoes")
public class QuestaoAdminController {

    private final QuestaoAdminService service;

    public QuestaoAdminController(QuestaoAdminService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestaoResponse criar(@Valid @RequestBody QuestaoAdminRequest pedido) {
        return service.criar(pedido);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        service.desativar(id);
    }
}
