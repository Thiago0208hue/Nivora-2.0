package com.nivora.nivora.seguranca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.usuario.CadastroRequest;
import com.nivora.nivora.usuario.PerfilEstudoRepository;
import com.nivora.nivora.usuario.Usuario;
import com.nivora.nivora.usuario.UsuarioRepository;
import com.nivora.nivora.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock PerfilEstudoRepository perfis;
    @Mock PasswordEncoder encoder;
    @InjectMocks UsuarioService service;

    @Test
    void emailRepetidoNaoSalvaNada() {
        when(usuarios.existsByEmail("ana@email.com")).thenReturn(true);

        NegocioException erro = assertThrows(NegocioException.class,
                () -> service.cadastrar(new CadastroRequest("Ana", "ANA@email.com ", "senha-segura-1")));

        assertEquals(HttpStatus.CONFLICT, erro.getStatus());
        verify(usuarios, never()).save(any());
    }

    @Test
    void cadastroNormalizaEmailESalvaHashEmVezDaSenha() {
        when(usuarios.existsByEmail("ana@email.com")).thenReturn(false);
        when(encoder.encode("senha-segura-1")).thenReturn("hash-da-senha");
        when(usuarios.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        var resposta = service.cadastrar(new CadastroRequest(" Ana ", "ANA@email.com ", "senha-segura-1"));

        assertEquals("ana@email.com", resposta.email());
        assertEquals("Ana", resposta.nome());
        verify(encoder).encode("senha-segura-1");
    }
}
