package com.nivora.nivora.redacao;

import java.util.ArrayList;
import java.util.List;

/**
 * Contagens objetivas do texto (palavras e parágrafos) e orientações de estrutura. NÃO é correção
 * nem nota: o Nivora não avalia o conteúdo da redação. Classe pura.
 */
public class AnaliseEstrutural {

    /** Referência de estrutura do Nivora (introdução, dois desenvolvimentos, conclusão); não é regra oficial. */
    public static final int PARAGRAFOS_RECOMENDADOS = 4;
    public static final int PALAVRAS_MINIMAS_SUGERIDAS = 150;

    public record Resultado(int palavras, int paragrafos, List<String> observacoes) {
    }

    public Resultado analisar(String texto) {
        String limpo = texto == null ? "" : texto.trim();
        int palavras = limpo.isEmpty() ? 0 : limpo.split("\\s+").length;
        int paragrafos = 0;
        for (String p : limpo.split("\\R+")) {
            if (!p.isBlank()) paragrafos++;
        }
        List<String> obs = new ArrayList<>();
        if (paragrafos < PARAGRAFOS_RECOMENDADOS) {
            obs.add("Seu texto tem " + paragrafos + " parágrafo(s). Uma estrutura comum é introdução, "
                    + "dois parágrafos de desenvolvimento e conclusão (4 parágrafos).");
        }
        if (palavras < PALAVRAS_MINIMAS_SUGERIDAS) {
            obs.add("Texto curto (" + palavras + " palavras) para apresentar tese, argumentos e proposta de intervenção.");
        }
        return new Resultado(palavras, paragrafos, obs);
    }
}
