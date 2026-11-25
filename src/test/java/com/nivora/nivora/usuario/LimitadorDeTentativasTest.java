package com.nivora.nivora.usuario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LimitadorDeTentativasTest {

    /** Relógio que a gente avança à mão, para testar a janela de tempo sem esperar. */
    static class RelogioManual extends Clock {
        Instant agora = Instant.parse("2026-11-25T10:00:00Z");
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
    }

    @Test
    void bloqueiaDepoisDeCincoFalhas() {
        var l = new LimitadorDeTentativas(new RelogioManual());
        for (int i = 0; i < 4; i++) l.registrarFalha("a|1");
        assertFalse(l.bloqueado("a|1"));
        l.registrarFalha("a|1");
        assertTrue(l.bloqueado("a|1"));
    }

    @Test
    void chavesDiferentesNaoSeAfetam() {
        var l = new LimitadorDeTentativas(new RelogioManual());
        for (int i = 0; i < 5; i++) l.registrarFalha("a|1");
        assertFalse(l.bloqueado("b|1"));
        assertFalse(l.bloqueado("a|2"));
    }

    @Test
    void liberaDepoisDaJanela() {
        var relogio = new RelogioManual();
        var l = new LimitadorDeTentativas(relogio);
        for (int i = 0; i < 5; i++) l.registrarFalha("a|1");
        relogio.agora = relogio.agora.plusSeconds(LimitadorDeTentativas.JANELA_SEGUNDOS + 1);
        assertFalse(l.bloqueado("a|1"));
    }

    @Test
    void loginCertoLimpaAsFalhas() {
        var l = new LimitadorDeTentativas(new RelogioManual());
        for (int i = 0; i < 4; i++) l.registrarFalha("a|1");
        l.limpar("a|1");
        l.registrarFalha("a|1");
        assertFalse(l.bloqueado("a|1"));
    }
}
