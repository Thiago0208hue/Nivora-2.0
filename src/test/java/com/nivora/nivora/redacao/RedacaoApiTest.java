package com.nivora.nivora.redacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivora.nivora.config.NegocioException;
import java.util.Arrays;
import java.util.List;
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
class RedacaoApiTest {

    @Autowired WebApplicationContext contexto;
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

    private String corpo(String texto, String competencias) {
        return "{\"tema\":\"Tema de teste\",\"texto\":\"" + texto + "\",\"competencias\":" + competencias + "}";
    }

    @Test
    void exigeLogin() throws Exception {
        mvc.perform(get("/api/redacao")).andExpect(status().isUnauthorized());
    }

    @Test
    void listaOsTemasDeTreino() throws Exception {
        MockHttpSession s = entrar("t@email.com");
        mvc.perform(get("/api/redacao/temas").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void salvaComAutoavaliacaoESomaOTotal() throws Exception {
        MockHttpSession s = entrar("r1@email.com");
        mvc.perform(post("/api/redacao").session(s).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Um texto curto de teste.", "[160,120,120,160,200]")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(760))
                .andExpect(jsonPath("$.palavras").value(5));
        mvc.perform(get("/api/redacao").session(s))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].total").value(760));
        mvc.perform(get("/api/desempenho").session(s))
                .andExpect(jsonPath("$.redacoes").value(1))
                .andExpect(jsonPath("$.gamificacao.xp").value(10))
                .andExpect(jsonPath("$.gamificacao.conquistas[0]").value("primeiraRedacao"));
    }

    @Test
    void autoavaliacaoEOpcional() throws Exception {
        MockHttpSession s = entrar("r2@email.com");
        mvc.perform(post("/api/redacao").session(s).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Texto sem nota.", "null")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").doesNotExist());
    }

    @Test
    void rejeitaNotaForaDosNiveisETextoVazio() throws Exception {
        MockHttpSession s = entrar("r3@email.com");
        mvc.perform(post("/api/redacao").session(s).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Texto.", "[100,120,120,160,200]"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/redacao").session(s).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Texto.", "[40,40]"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/redacao").session(s).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("   ", "null"))).andExpect(status().isBadRequest());
    }

    @Test
    void ninguemLeRedacaoDeOutro() throws Exception {
        MockHttpSession dono = entrar("d1@email.com");
        long id = json.readTree(mvc.perform(post("/api/redacao").session(dono).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo("Meu texto.", "null")))
                .andReturn().getResponse().getContentAsString()).get("id").asLong();
        MockHttpSession outro = entrar("o1@email.com");
        mvc.perform(get("/api/redacao/" + id).session(outro)).andExpect(status().isNotFound());
        mvc.perform(get("/api/redacao/" + id).session(dono)).andExpect(status().isOk());
    }

    @Test
    void validacaoDasNotas() {
        assertNull(RedacaoService.validarNotas(null));
        assertNull(RedacaoService.validarNotas(List.of()));
        assertEquals(200, RedacaoService.validarNotas(List.of(0, 40, 80, 120, 200))[4]);
        assertThrows(NegocioException.class, () -> RedacaoService.validarNotas(Arrays.asList(0, 40, 80, 120, null)));
    }
}
