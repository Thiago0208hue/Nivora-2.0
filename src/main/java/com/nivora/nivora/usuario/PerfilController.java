package com.nivora.nivora.usuario;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService service;

    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    public PerfilResponse obter(Principal usuarioLogado) {
        return service.obter(usuarioLogado.getName());
    }

    @PutMapping
    public PerfilResponse atualizar(Principal usuarioLogado, @Valid @RequestBody PerfilRequest pedido) {
        return service.atualizar(usuarioLogado.getName(), pedido);
    }
}
