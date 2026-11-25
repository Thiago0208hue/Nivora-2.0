package com.nivora.nivora.metricas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class MetricasApiTest {

    @Autowired WebApplicationContext contexto;
    @Autowired UsuarioRepository usuarios;
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
    void estudanteNaoVeMetricas() throws Exception {
        MockHttpSession s = entrar("aluno@email.com");
        mvc.perform(get("/api/admin/metricas").session(s)).andExpect(status().isForbidden());
    }

    @Test
    void adminVeSomenteTotais() throws Exception {
        MockHttpSession s = entrar("chefe@email.com");
        var u = usuarios.findByEmail("chefe@email.com").orElseThrow();
        u.tornarAdmin();
        usuarios.saveAndFlush(u);

        mvc.perform(get("/api/admin/metricas").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarios").value(1))
                .andExpect(jsonPath("$.questoesRespondidas").value(0))
                .andExpect(jsonPath("$.percentualAcertoGeral").value(0))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.nomes").doesNotExist());
    }

    @Test
    void percentualTrataZeroEArredonda() {
        assertEquals(0, MetricasService.percentual(0, 0));
        assertEquals(67, MetricasService.percentual(2, 3));
    }
}
