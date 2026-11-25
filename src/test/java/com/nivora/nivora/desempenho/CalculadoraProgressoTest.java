package com.nivora.nivora.desempenho;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nivora.nivora.desempenho.CalculadoraProgresso.AssuntoStats;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CalculadoraProgressoTest {

    private final CalculadoraProgresso calc = new CalculadoraProgresso();
    private final LocalDate hoje = LocalDate.of(2026, 11, 25);

    @Test
    void xpENivel() {
        assertEquals(50, calc.xp(10));
        assertEquals(1, calc.nivel(99));
        assertEquals(2, calc.nivel(100));
        assertEquals(30, calc.xpNoNivel(130));
    }

    @Test
    void sequenciaContaDiasSeguidosAteHoje() {
        Set<LocalDate> dias = Set.of(hoje, hoje.minusDays(1), hoje.minusDays(2), hoje.minusDays(5));
        assertEquals(3, calc.sequenciaAtual(dias, hoje));
    }

    @Test
    void sequenciaDeOntemAindaValeSeHojeNaoTeveAtividade() {
        assertEquals(2, calc.sequenciaAtual(Set.of(hoje.minusDays(1), hoje.minusDays(2)), hoje));
    }

    @Test
    void sequenciaZeraAposDoisDiasSemEstudar() {
        assertEquals(0, calc.sequenciaAtual(Set.of(hoje.minusDays(2)), hoje));
        assertEquals(0, calc.sequenciaAtual(Set.of(), hoje));
    }

    @Test
    void maiorSequenciaIgnoraBuracos() {
        Set<LocalDate> dias = Set.of(hoje, hoje.minusDays(1), hoje.minusDays(10), hoje.minusDays(11), hoje.minusDays(12));
        assertEquals(3, calc.maiorSequencia(dias));
    }

    @Test
    void conquistasPorMarcos() {
        assertTrue(calc.conquistas(10, 1, 5).isEmpty());
        assertEquals(List.of("xp100", "sequencia7", "questoes100"), calc.conquistas(100, 7, 100));
    }

    @Test
    void redacaoDaXpEConquista() {
        assertEquals(60, calc.xp(10, 1));
        assertEquals(List.of("primeiraRedacao"), calc.conquistas(0, 0, 0, 1));
    }

    @Test
    void assuntoMaisFracoExigeRespostasSuficientesETaxaBaixa() {
        var poucoDado = new AssuntoStats("A", 2, 0);
        var bom = new AssuntoStats("B", 10, 9);
        var ruim = new AssuntoStats("C", 10, 3);
        var pior = new AssuntoStats("D", 4, 1);
        assertEquals("D", calc.assuntoMaisFraco(List.of(poucoDado, bom, ruim, pior)).orElseThrow().assunto());
        assertTrue(calc.assuntoMaisFraco(List.of(poucoDado, bom)).isEmpty());
    }
}
