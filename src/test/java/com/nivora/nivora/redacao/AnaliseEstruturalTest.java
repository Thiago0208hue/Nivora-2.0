package com.nivora.nivora.redacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AnaliseEstruturalTest {

    private final AnaliseEstrutural analise = new AnaliseEstrutural();

    @Test
    void contaPalavrasEParagrafos() {
        var r = analise.analisar("Primeiro parágrafo aqui.\n\nSegundo   parágrafo.\r\nTerceiro.");
        assertEquals(6, r.palavras());
        assertEquals(3, r.paragrafos());
    }

    @Test
    void textoVazioNaoTemPalavrasNemParagrafos() {
        var r = analise.analisar("   \n ");
        assertEquals(0, r.palavras());
        assertEquals(0, r.paragrafos());
    }

    @Test
    void textoCurtoERecebeOrientacoes() {
        assertEquals(2, analise.analisar("Um texto bem curto.").observacoes().size());
    }

    @Test
    void textoComQuatroParagrafosELongoNaoRecebeOrientacoes() {
        String p = "palavra ".repeat(50);
        var r = analise.analisar(p + "\n" + p + "\n" + p + "\n" + p);
        assertTrue(r.observacoes().isEmpty());
    }
}
