package com.nivora.nivora.frontend;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Confere se o frontend em static/ é servido sem login (a proteção fica na API). */
@SpringBootTest
class FrontendTest {

    @Autowired WebApplicationContext contexto;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void paginaInicialEhServida() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nivora")));
    }

    @Test
    void telasDeLoginECadastroSaoServidas() throws Exception {
        mvc.perform(get("/pages/login.html")).andExpect(status().isOk());
        mvc.perform(get("/pages/cadastro.html")).andExpect(status().isOk());
    }

    @Test
    void scriptsECssSaoServidos() throws Exception {
        mvc.perform(get("/js/api.js")).andExpect(status().isOk());
        mvc.perform(get("/css/style.css")).andExpect(status().isOk());
    }
}
