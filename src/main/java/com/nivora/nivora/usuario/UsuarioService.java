package com.nivora.nivora.usuario;

import com.nivora.nivora.config.NegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PerfilEstudoRepository perfis;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PerfilEstudoRepository perfis, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.perfis = perfis;
        this.encoder = encoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest pedido) {
        String email = pedido.email().trim().toLowerCase();
        if (usuarios.existsByEmail(email)) {
            throw new NegocioException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        Usuario usuario = usuarios.save(new Usuario(pedido.nome().trim(), email, encoder.encode(pedido.senha())));
        perfis.save(new PerfilEstudo(usuario));
        return UsuarioResponse.de(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorEmail(String email) {
        return usuarios.findByEmail(email)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }
}
