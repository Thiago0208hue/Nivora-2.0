package com.nivora.nivora.seguranca;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivora.nivora.questao.QuestaoRepository;
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

/** Revisão de segurança da Etapa 10: cabeçalhos, CSRF, autenticação, limite de login, erros e integridade. */
@SpringBootTest
@Transactional
class SegurancaApiTest {

    @Autowired WebApplicationContext contexto;
    @Autowired QuestaoRepository questoes;
    final ObjectMapper json = new ObjectMapper();
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private void cadastrar(String email) throws Exception {
        mvc.perform(post("/api/auth/cadastro").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ana\",\"email\":\"" + email + "\",\"senha\":\"senha-segura-1\"}"))
                .andExpect(status().isCreated());
    }

    private MockHttpSession entrar(String email) throws Exception {
        cadastrar(email);
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-segura-1\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private void loginErrado(String email, String senha) throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"));
    }

    @Test
    void respostasTragamCabecalhosDeSeguranca() throws Exception {
        mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy",
                        org.hamcrest.Matchers.containsString("script-src 'self'")))
                .andExpect(header().string("Content-Security-Policy",
                        org.hamcrest.Matchers.containsString("frame-ancestors 'none'")));
    }

    @Test
    void postSemTokenCsrfEBarrado() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@email.com\",\"senha\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void todasAsRotasDaApiExigemLogin() throws Exception {
        String[] rotas = {"/api/auth/me", "/api/perfil", "/api/cursos", "/api/instituicoes", "/api/questoes/catalogo",
                "/api/questoes/revisao", "/api/simulados", "/api/desempenho", "/api/redacao", "/api/redacao/temas"};
        for (String rota : rotas) {
            mvc.perform(get(rota)).andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/plano/semanal").with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/personalizacao/prioridades").with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutEncerraASessao() throws Exception {
        MockHttpSession s = entrar("sai@email.com");
        mvc.perform(get("/api/auth/me").session(s)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").session(s).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").session(s)).andExpect(status().isUnauthorized());
    }

    @Test
    void senhaNuncaVoltaNaResposta() throws Exception {
        cadastrar("sem-senha@email.com");
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"sem-senha@email.com\",\"senha\":\"senha-segura-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void loginComEmailInexistenteEComSenhaErradaTemAMesmaMensagem() throws Exception {
        cadastrar("existe@email.com");
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"existe@email.com\",\"senha\":\"errada-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha inválidos."));
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nao-existe@email.com\",\"senha\":\"errada-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha inválidos."));
    }

    @Test
    void depoisDeCincoFalhasOLoginEBloqueadoMesmoComASenhaCerta() throws Exception {
        cadastrar("forca@email.com");
        for (int i = 0; i < 5; i++) loginErrado("forca@email.com", "senha-errada-" + i);
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"forca@email.com\",\"senha\":\"senha-segura-1\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void corpoInvalidoEJsonMalFormadoViramErro400EmJson() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{nao é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Corpo da requisição inválido."));
        mvc.perform(post("/api/auth/cadastro").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\",\"email\":\"x\",\"senha\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").exists());
    }

    @Test
    void errosDeRotaViramJsonSemDetalhesInternos() throws Exception {
        MockHttpSession s = entrar("erros@email.com");
        mvc.perform(get("/api/simulados/abc").session(s))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Parâmetro inválido."));
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(get("/api/auth/login").session(s))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/api/nao-existe").session(s))
                .andExpect(status().isNotFound());
    }

    @Test
    void redacaoComTextoEnormeERejeitada() throws Exception {
        MockHttpSession s = entrar("grande@email.com");
        String enorme = "a".repeat(8001);
        mvc.perform(post("/api/redacao").session(s).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tema\":\"t\",\"texto\":\"" + enorme + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void repetirAMesmaQuestaoCertaNaoInflaOXp() throws Exception {
        MockHttpSession s = entrar("xp@email.com");
        long id = json.readTree(mvc.perform(post("/api/questoes/praticar").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":1}"))
                .andReturn().getResponse().getContentAsString()).get(0).get("id").asLong();
        String certa = questoes.findById(id).orElseThrow().getCorreta().getLetra();
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/questoes/" + id + "/responder").session(s).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"" + certa + "\"}"))
                    .andExpect(status().isOk());
        }
        mvc.perform(get("/api/desempenho").session(s))
                .andExpect(jsonPath("$.respondidas").value(3))
                .andExpect(jsonPath("$.gamificacao.xp").value(5));
    }

    @Test
    void desempenhoEnviadoPeloNavegadorEIgnorado() throws Exception {
        MockHttpSession s = entrar("falso@email.com");
        mvc.perform(post("/api/personalizacao/prioridades").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"desempenho\":{\"matematica\":{\"respondidas\":1000,\"acertos\":1000}}}"))
                .andExpect(status().isConflict()); // sem onboarding, em vez de aceitar o desempenho falso
    }
}
