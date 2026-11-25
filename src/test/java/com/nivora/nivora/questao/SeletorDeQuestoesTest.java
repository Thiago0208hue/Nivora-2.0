package com.nivora.nivora.questao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SeletorDeQuestoesTest {

    private final SeletorDeQuestoes seletor = new SeletorDeQuestoes(new Random(42));

    @Test
    void naoDevolveMaisQuestoesDoQueExistem() {
        assertEquals(3, seletor.selecionar(List.of(1L, 2L, 3L), Map.of(), 10).size());
    }

    @Test
    void naoRepeteQuestoesNaMesmaSelecao() {
        List<Long> ids = List.of(1L, 2L, 3L, 4L, 5L, 6L);
        List<Long> r = seletor.selecionar(ids, Map.of(), 6);
        assertEquals(new HashSet<>(ids), new HashSet<>(r));
    }

    @Test
    void prefereQuestoesNuncaRespondidas() {
        var uso = new SeletorDeQuestoes.Uso(1, LocalDateTime.now());
        List<Long> r = seletor.selecionar(List.of(1L, 2L, 3L, 4L), Map.of(1L, uso, 2L, uso), 2);
        assertEquals(new HashSet<>(List.of(3L, 4L)), new HashSet<>(r));
    }

    @Test
    void depoisDasNovasUsaAsRespondidasHaMaisTempo() {
        LocalDateTime agora = LocalDateTime.now();
        Map<Long, SeletorDeQuestoes.Uso> usos = Map.of(
                1L, new SeletorDeQuestoes.Uso(1, agora.minusDays(1)),
                2L, new SeletorDeQuestoes.Uso(1, agora.minusDays(30)),
                3L, new SeletorDeQuestoes.Uso(1, agora.minusDays(10)));
        assertEquals(List.of(2L, 3L), seletor.selecionar(List.of(1L, 2L, 3L), usos, 2));
    }

    @Test
    void embaralharMantemOsMesmosElementos() {
        List<Integer> lista = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        seletor.embaralhar(lista);
        assertEquals(8, lista.size());
        assertTrue(lista.containsAll(List.of(1, 2, 3, 4, 5, 6, 7, 8)));
    }
}
