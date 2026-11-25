package com.nivora.nivora.questao;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nivora.nivora.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/** Usa as 8 questões de EXEMPLO da migração V5. */
@SpringBootTest
@Transactional
class QuestoesApiTest {

    @Autowired WebApplicationContext contexto;
    @Autowired UsuarioRepository usuarios;
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

    private JsonNode praticar(MockHttpSession s, int qtd) throws Exception {
        String corpo = mvc.perform(post("/api/questoes/praticar").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":" + qtd + "}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    @Test
    void questoesExigemLogin() throws Exception {
        mvc.perform(get("/api/questoes/catalogo")).andExpect(status().isUnauthorized());
    }

    @Test
    void praticarNaoVazaGabarito() throws Exception {
        MockHttpSession s = entrar("a@email.com");
        mvc.perform(post("/api/questoes/praticar").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].correta").doesNotExist())
                .andExpect(jsonPath("$[0].alternativas[0].correta").doesNotExist());
    }

    @Test
    void responderCorrigeERegistra_eErroAparecenaRevisao() throws Exception {
        MockHttpSession s = entrar("b@email.com");
        long id = praticar(s, 1).get(0).get("id").asLong();
        String correta = questoes.findById(id).orElseThrow().getCorreta().getLetra();
        String errada = correta.equals("A") ? "B" : "A";

        mvc.perform(post("/api/questoes/" + id + "/responder").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"" + errada + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acertou").value(false))
                .andExpect(jsonPath("$.letraCorreta").value(correta));

        mvc.perform(get("/api/questoes/revisao").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questao.id").value(id));

        mvc.perform(post("/api/questoes/" + id + "/responder").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"" + correta + "\"}"))
                .andExpect(jsonPath("$.acertou").value(true));
        mvc.perform(get("/api/questoes/revisao").session(s)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void letraInvalidaERejeitada() throws Exception {
        MockHttpSession s = entrar("c@email.com");
        long id = praticar(s, 1).get(0).get("id").asLong();
        mvc.perform(post("/api/questoes/" + id + "/responder").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"Z\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void simuladoSoRevelaGabaritoAoFinalizar() throws Exception {
        MockHttpSession s = entrar("d@email.com");
        String criado = mvc.perform(post("/api/simulados").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"COMPLETO\",\"quantidade\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questoes.length()").value(4))
                .andReturn().getResponse().getContentAsString();
        JsonNode sim = json.readTree(criado);
        long simId = sim.get("id").asLong();
        long q1 = sim.get("questoes").get(0).get("id").asLong();
        String correta = questoes.findById(q1).orElseThrow().getCorreta().getLetra();

        mvc.perform(post("/api/simulados/" + simId + "/questoes/" + q1 + "/responder").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"" + correta + "\"}"))
                .andExpect(status().isNoContent());
        // segunda resposta à mesma questão no mesmo simulado é recusada
        mvc.perform(post("/api/simulados/" + simId + "/questoes/" + q1 + "/responder").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"" + correta + "\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/simulados/" + simId + "/finalizar").session(s).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.acertos").value(1))
                .andExpect(jsonPath("$.emBranco").value(3));

        mvc.perform(post("/api/simulados/" + simId + "/questoes/" + q1 + "/responder").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"letra\":\"A\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void estudanteNaoVeSimuladoDeOutro() throws Exception {
        MockHttpSession dono = entrar("e@email.com");
        long id = json.readTree(mvc.perform(post("/api/simulados").session(dono).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"COMPLETO\",\"quantidade\":2}"))
                .andReturn().getResponse().getContentAsString()).get("id").asLong();
        MockHttpSession outro = entrar("f@email.com");
        mvc.perform(get("/api/simulados/" + id).session(outro)).andExpect(status().isNotFound());
    }

    @Test
    void estudanteNaoAcessaAdmin() throws Exception {
        MockHttpSession s = entrar("g@email.com");
        mvc.perform(delete("/api/admin/questoes/1").session(s).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void adminCadastraQuestaoEValidaRegras() throws Exception {
        MockHttpSession s = entrar("admin@email.com");
        var u = usuarios.findByEmail("admin@email.com").orElseThrow();
        u.tornarAdmin();
        usuarios.saveAndFlush(u);

        String semAno = """
                {"area":"MATEMATICA","disciplina":"Matemática","assunto":"Teste","origem":"ENEM_OFICIAL",
                 "enunciado":"x","alternativas":[{"letra":"A","texto":"1","correta":true},{"letra":"B","texto":"2","correta":false}]}""";
        mvc.perform(post("/api/admin/questoes").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(semAno)).andExpect(status().isBadRequest());

        String duasCorretas = """
                {"area":"MATEMATICA","disciplina":"Matemática","assunto":"Teste","origem":"AUTORAL",
                 "enunciado":"x","alternativas":[{"letra":"A","texto":"1","correta":true},{"letra":"B","texto":"2","correta":true}]}""";
        mvc.perform(post("/api/admin/questoes").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(duasCorretas)).andExpect(status().isBadRequest());

        String ok = """
                {"area":"MATEMATICA","disciplina":"Matemática","assunto":"Teste","origem":"AUTORAL",
                 "enunciado":"x","alternativas":[{"letra":"A","texto":"1","correta":true},{"letra":"B","texto":"2","correta":false}]}""";
        mvc.perform(post("/api/admin/questoes").session(s).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(ok))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origem").value("Questão autoral"));
    }
}
