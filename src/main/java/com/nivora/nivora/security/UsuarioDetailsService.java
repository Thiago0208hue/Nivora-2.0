package com.nivora.nivora.security;

import com.nivora.nivora.usuario.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Ensina o Spring Security a buscar o usuário (pelo e-mail) no banco. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return usuarios.findByEmail(email.trim().toLowerCase())
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getSenhaHash())
                        .roles(u.getPapel().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }
}
