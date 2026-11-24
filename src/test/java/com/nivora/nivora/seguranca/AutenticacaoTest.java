package com.nivora.nivora.seguranca;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nivora.nivora.usuario.Usuario;
import com.nivora.nivora.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class AutenticacaoTest {

    private static final String CADASTRO = """
            {"nome":"Ana","email":"ana@email.com","senha":"senha-segura-1"}""";
    private static final String LOGIN = """
            {"email":"ana@email.com","senha":"senha-segura-1"}""";

    @Autowired WebApplicationContext contexto;
    @Autowired UsuarioRepository usuarios;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private void cadastrar() throws Exception {
        mvc.perform(post("/api/auth/cadastro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isCreated());
    }

    private MockHttpSession logar() throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }

    @Test
    void cadastroGuardaSenhaCriptografada() throws Exception {
        cadastrar();
        Usuario salvo = usuarios.findByEmail("ana@email.com").orElseThrow();
        assertTrue(salvo.getSenhaHash().startsWith("$2"));
        assertNotEquals("senha-segura-1", salvo.getSenhaHash());
    }

    @Test
    void cadastroComEmailRepetidoRetorna409() throws Exception {
        cadastrar();
        mvc.perform(post("/api/auth/cadastro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroComSenhaCurtaRetorna400() throws Exception {
        mvc.perform(post("/api/auth/cadastro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ana\",\"email\":\"ana@email.com\",\"senha\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        cadastrar();
        mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@email.com\",\"senha\":\"senha-errada-1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postSemTokenCsrfRetorna403() throws Exception {
        mvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isForbidden());
    }

    @Test
    void perfilSemLoginRetorna401() throws Exception {
        mvc.perform(get("/api/perfil")).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioLogadoLeEAtualizaOProprioPerfil() throws Exception {
        cadastrar();
        MockHttpSession sessao = logar();

        mvc.perform(get("/api/perfil").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingConcluido").value(false));

        mvc.perform(put("/api/perfil").session(sessao).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"cursoCodigo":"medicina","instituicaoSigla":"ufrn","nivel":"INICIANTE",
                         "minutosDiarios":120,"dificuldades":["Matemática","Física"],
                         "onboardingConcluido":true}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursoCodigo").value("medicina"))
                .andExpect(jsonPath("$.instituicaoSigla").value("ufrn"))
                .andExpect(jsonPath("$.dificuldades.length()").value(2));
    }

    @Test
    void cursoInexistenteRetorna404() throws Exception {
        cadastrar();
        MockHttpSession sessao = logar();
        mvc.perform(put("/api/perfil").session(sessao).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cursoCodigo\":\"nao-existe\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void logoutEncerraASessao() throws Exception {
        cadastrar();
        MockHttpSession sessao = logar();
        mvc.perform(post("/api/auth/logout").session(sessao).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/perfil").session(sessao)).andExpect(status().isUnauthorized());
    }
}
