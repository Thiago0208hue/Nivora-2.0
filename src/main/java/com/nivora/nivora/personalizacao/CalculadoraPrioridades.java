package com.nivora.nivora.personalizacao;

import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.usuario.Nivel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Calcula as prioridades de estudo por área do ENEM. Não acessa banco nem web: recebe os dados
 * e devolve o resultado, o que facilita testar. As regras são heurísticas do Nivora, não critério oficial.
 */
@Component
public class CalculadoraPrioridades {

    static final double LIMIAR_ALTA = 5.0;
    static final double LIMIAR_MEDIA = 3.5;
    static final double BONUS_DIFICULDADE = 2;
    static final int MIN_QUESTOES_PARA_DESEMPENHO = 3;
    static final double NOTA_ALVO_ALTA = 700;
    static final double BONUS_NOTA_ALTA = 0.5;
    static final double PESO_ALTO = 4;
    static final int MINUTOS_PADRAO = 120;

    private static final Locale PT_BR = Locale.of("pt", "BR");

    public List<PrioridadeArea> calcular(EntradaPrioridades entrada) {
        // Pouco tempo: só o essencial vira prioridade. Muito tempo: mais áreas entram.
        int ajusteTempo = ajusteTempo(entrada.minutosDiarios());
        double limiarAlta = LIMIAR_ALTA + ajusteTempo;
        double limiarMedia = LIMIAR_MEDIA + ajusteTempo;
        // O nível soma o mesmo valor em todas as áreas: muda quantas viram prioridade, não a ordem entre elas
        double bonusNivel = bonusNivel(entrada.nivel());
        boolean notaAlta = entrada.notaAlvo() != null
                && entrada.notaAlvo().compareTo(BigDecimal.valueOf(NOTA_ALVO_ALTA)) >= 0;

        List<PrioridadeArea> resultado = new ArrayList<>();
        for (AreaEnem area : AreaEnem.values()) {
            PesoArea peso = entrada.pesos().getOrDefault(area,
                    new PesoArea(3, OrigemPeso.CURSO_ESTIMADO, "peso padrão do Nivora (sem dados do curso)"));
            double score = peso.valor() + bonusNivel;
            List<String> motivos = new ArrayList<>();
            motivos.add("Peso " + formatar(peso.valor()) + " nesta área: " + peso.detalhe() + ".");

            List<String> dificuldades = entrada.dificuldades().getOrDefault(area, List.of());
            if (!dificuldades.isEmpty()) {
                score += BONUS_DIFICULDADE;
                motivos.add("Você relatou dificuldade em: " + String.join(", ", dificuldades) + ".");
            }

            DesempenhoArea desempenho = entrada.desempenho().get(area);
            if (desempenho != null && desempenho.respondidas() >= MIN_QUESTOES_PARA_DESEMPENHO) {
                double taxa = (double) desempenho.acertos() / desempenho.respondidas();
                String resumo = Math.round(taxa * 100) + "% de acertos (" + desempenho.acertos()
                        + " de " + desempenho.respondidas() + " questões)";
                if (taxa < 0.5) {
                    score += 2;
                    motivos.add("Seu aproveitamento está baixo: " + resumo + ".");
                } else if (taxa < 0.7) {
                    score += 1;
                    motivos.add("Seu aproveitamento pode melhorar: " + resumo + ".");
                } else if (taxa >= 0.9) {
                    score -= 1;
                    motivos.add("Você está indo muito bem: " + resumo + ".");
                }
            }

            if (notaAlta && peso.valor() >= PESO_ALTO) {
                score += BONUS_NOTA_ALTA;
                motivos.add("Sua nota-alvo (" + entrada.notaAlvo().stripTrailingZeros().toPlainString()
                        + ") é alta; áreas de peso alto merecem reforço.");
            }

            score = Math.round(score * 100) / 100.0;
            NivelPrioridade nivel = score >= limiarAlta ? NivelPrioridade.ALTA
                    : score >= limiarMedia ? NivelPrioridade.MEDIA : NivelPrioridade.BAIXA;

            resultado.add(new PrioridadeArea(area.name().toLowerCase(), area.getRotulo(), score, nivel,
                    peso.valor(), peso.origem(), List.copyOf(motivos)));
        }

        // Maior score primeiro; empate decidido pelo peso; persistindo o empate, vale a ordem das áreas
        resultado.sort(Comparator.<PrioridadeArea>comparingDouble(PrioridadeArea::score).reversed()
                .thenComparing(Comparator.<PrioridadeArea>comparingDouble(PrioridadeArea::peso).reversed()));
        return resultado;
    }

    static int ajusteTempo(Integer minutos) {
        int tempo = minutos == null ? MINUTOS_PADRAO : minutos;
        if (tempo < 60) return 1;
        if (tempo >= 240) return -1;
        return 0;
    }

    static double bonusNivel(Nivel nivel) {
        if (nivel == null) return 0;
        return switch (nivel) {
            case INICIANTE -> 1;
            case BASICO -> 0.5;
            case INTERMEDIARIO -> 0;
            case AVANCADO -> -0.5;
        };
    }

    private static String formatar(double valor) {
        return valor == Math.floor(valor)
                ? String.valueOf((long) valor)
                : String.format(PT_BR, "%.1f", valor);
    }
}
