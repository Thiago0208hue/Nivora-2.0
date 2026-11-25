package com.nivora.nivora.desempenho;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Regras de progresso do Nivora (XP, nível, sequência, conquistas). São regras próprias do
 * produto, não oficiais. Classe pura: não acessa banco nem relógio.
 */
public class CalculadoraProgresso {

    public static final int XP_POR_ACERTO = 5;
    public static final int XP_POR_NIVEL = 100;
    /** Mínimo de respostas para um assunto ser apontado como "mais fraco". */
    public static final int MINIMO_PARA_ASSUNTO_FRACO = 3;
    public static final double LIMIAR_ASSUNTO_FRACO = 0.7;

    public record AssuntoStats(String assunto, int respondidas, int acertos) {
    }

    public int xp(int acertos) {
        return acertos * XP_POR_ACERTO;
    }

    public int nivel(int xp) {
        return xp / XP_POR_NIVEL + 1;
    }

    public int xpNoNivel(int xp) {
        return xp % XP_POR_NIVEL;
    }

    /** Dias seguidos com atividade até hoje; se hoje ainda não teve, a sequência de ontem continua valendo. */
    public int sequenciaAtual(Set<LocalDate> dias, LocalDate hoje) {
        LocalDate dia = dias.contains(hoje) ? hoje : hoje.minusDays(1);
        int sequencia = 0;
        while (dias.contains(dia)) {
            sequencia++;
            dia = dia.minusDays(1);
        }
        return sequencia;
    }

    public int maiorSequencia(Set<LocalDate> dias) {
        int maior = 0, atual = 0;
        LocalDate anterior = null;
        for (LocalDate dia : new TreeSet<>(dias)) {
            atual = anterior != null && anterior.plusDays(1).equals(dia) ? atual + 1 : 1;
            maior = Math.max(maior, atual);
            anterior = dia;
        }
        return maior;
    }

    /** Conquistas ficam registradas pelo melhor resultado: perder a sequência não apaga a conquista. */
    public List<String> conquistas(int xp, int maiorSequencia, int questoesRespondidas) {
        List<String> ids = new ArrayList<>();
        if (xp >= 100) ids.add("xp100");
        if (maiorSequencia >= 7) ids.add("sequencia7");
        if (questoesRespondidas >= 100) ids.add("questoes100");
        return ids;
    }

    /** Assunto com pior taxa de acerto, se tiver respostas suficientes e taxa abaixo do limiar. */
    public Optional<AssuntoStats> assuntoMaisFraco(List<AssuntoStats> assuntos) {
        return assuntos.stream()
                .filter(a -> a.respondidas() >= MINIMO_PARA_ASSUNTO_FRACO)
                .filter(a -> taxa(a) < LIMIAR_ASSUNTO_FRACO)
                .min(java.util.Comparator.comparingDouble(CalculadoraProgresso::taxa));
    }

    private static double taxa(AssuntoStats a) {
        return (double) a.acertos() / a.respondidas();
    }
}
