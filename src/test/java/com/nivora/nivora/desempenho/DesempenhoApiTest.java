package com.nivora.nivora.desempenho;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@SpringBootTest
@Transactional
class DesempenhoApiTest {

    @Autowired WebApplicationContext contexto;
    @Autowired QuestaoRepository questoes;
    final ObjectMapper json = new ObjectMapper();
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private MockHttpSession entrar(String email) throws Exception {
        mvc.perform(post("/api/auth/cadastro").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ana\",\"email\":\"" + email + "\",\"senha\":\"senha-segura-1\"}"))
                .andExpect(status().isCreated());
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-segura-1\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    @Test
    void exigeLogin() throws Exception {
        mvc.perform(get("/api/desempenho")).andExpect(status().isUnauthorized());
    }

    @Test
    void semRespostasTudoZerado() throws Exception {
        MockHttpSession s = entrar("zero@email.com");
        mvc.perform(get("/api/desempenho").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respondidas").value(0))
                .andExpect(jsonPath("$.sequenciaDias").value(0))
                .andExpect(jsonPath("$.gamificacao.nivel").value(1));
    }

    @Test
    void respostasGravadasViramDesempenhoXpESequencia() throws Exception {
        MockHttpSession s = entrar("dados@email.com");
        var ids = json.readTree(mvc.perform(post("/api/questoes/praticar").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":2}"))
                .andReturn().getResponse().getContentAsString());
        long q1 = ids.get(0).get("id").asLong();
        long q2 = ids.get(1).get("id").asLong();
        String certa1 = questoes.findById(q1).orElseThrow().getCorreta().getLetra();
        String certa2 = questoes.findById(q2).orElseThrow().getCorreta().getLetra();
        String errada2 = certa2.equals("A") ? "B" : "A";

        mvc.perform(post("/api/questoes/" + q1 + "/responder").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"" + certa1 + "\"}"));
        mvc.perform(post("/api/questoes/" + q2 + "/responder").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"" + errada2 + "\"}"));

        mvc.perform(get("/api/desempenho").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respondidas").value(2))
                .andExpect(jsonPath("$.acertos").value(1))
                .andExpect(jsonPath("$.percentual").value(50))
                .andExpect(jsonPath("$.sequenciaDias").value(1))
                .andExpect(jsonPath("$.gamificacao.xp").value(5))
                .andExpect(jsonPath("$.ultimosDias.length()").value(1));
    }

    @Test
    void desempenhoDeUmEstudanteNaoApareceParaOutro() throws Exception {
        MockHttpSession a = entrar("a1@email.com");
        long q = json.readTree(mvc.perform(post("/api/questoes/praticar").session(a).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":1}"))
                .andReturn().getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(post("/api/questoes/" + q + "/responder").session(a).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"A\"}"));

        MockHttpSession b = entrar("b1@email.com");
        mvc.perform(get("/api/desempenho").session(b)).andExpect(jsonPath("$.respondidas").value(0));
    }
}
