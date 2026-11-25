package com.nivora.nivora.usuario;

import com.nivora.nivora.config.NegocioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarios;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextoRepository;
    private final LimitadorDeTentativas limitador;

    public AuthController(UsuarioService usuarios, AuthenticationManager authenticationManager,
                          SecurityContextRepository contextoRepository, LimitadorDeTentativas limitador) {
        this.limitador = limitador;
        this.usuarios = usuarios;
        this.authenticationManager = authenticationManager;
        this.contextoRepository = contextoRepository;
    }

    /** Entrega o token CSRF (e grava o cookie XSRF-TOKEN). O frontend chama antes do primeiro POST. */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest pedido) {
        return usuarios.cadastrar(pedido);
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest pedido,
                                 HttpServletRequest request, HttpServletResponse response) {
        String email = pedido.email().trim().toLowerCase();
        // A chave junta e-mail e IP: barra tentativas em massa sem deixar um atacante trancar a conta de outra pessoa de qualquer lugar
        String chave = email + "|" + request.getRemoteAddr();
        if (limitador.bloqueado(chave)) {
            throw new NegocioException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Aguarde alguns minutos e tente novamente.");
        }
        Authentication autenticacao;
        try {
            autenticacao = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, pedido.senha()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            limitador.registrarFalha(chave);
            throw e;
        }
        limitador.limpar(chave);

        // Protege contra fixação de sessão: troca o id de uma sessão já existente
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        contextoRepository.saveContext(contexto, request, response);

        return usuarios.buscarPorEmail(email);
    }

    /** Dados de quem está logado; devolve 401 se não houver sessão. */
    @GetMapping("/me")
    public UsuarioResponse eu(Principal usuarioLogado) {
        return usuarios.buscarPorEmail(usuarioLogado.getName());
    }
}
