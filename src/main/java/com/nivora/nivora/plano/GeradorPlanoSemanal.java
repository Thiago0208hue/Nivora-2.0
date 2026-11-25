package com.nivora.nivora.plano;

import com.nivora.nivora.personalizacao.NivelPrioridade;
import com.nivora.nivora.personalizacao.PrioridadeArea;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Monta o cronograma da semana a partir das prioridades. Não acessa banco nem web.
 *
 * Regras: cada dia tem dois blocos de estudo de áreas diferentes (mais uma revisão quando há tempo);
 * nenhuma área fica de fora da semana; áreas de prioridade alta aparecem ao menos duas vezes;
 * o restante dos blocos é dividido em proporção ao score; a soma dos blocos do dia é exatamente
 * o tempo disponível.
 */
@Component
public class GeradorPlanoSemanal {

    static final List<String> DIAS = List.of("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado");
    static final int BLOCOS_POR_DIA = 2;
    static final int MINUTOS_MINIMOS_PARA_REVISAO = 60;

    public PlanoSemanal gerar(List<PrioridadeArea> prioridades, int minutosPorDia,
                              Map<String, List<String>> focoPorArea) {
        Map<String, Integer> blocos = distribuirBlocos(prioridades, DIAS.size() * BLOCOS_POR_DIA);
        Map<String, PrioridadeArea> porArea = new LinkedHashMap<>();
        prioridades.forEach(p -> porArea.put(p.area(), p));

        int[] tempos = dividirTempo(minutosPorDia);
        List<DiaPlano> dias = new ArrayList<>();
        Map<String, Integer> minutosPorArea = new LinkedHashMap<>();
        prioridades.forEach(p -> minutosPorArea.put(p.area(), 0));

        Set<String> usadasOntem = new HashSet<>();
        for (String nomeDia : DIAS) {
            List<PrioridadeArea> doDia = escolherDuasAreas(porArea, blocos, usadasOntem);
            List<BlocoPlano> blocosDoDia = new ArrayList<>();
            for (int i = 0; i < doDia.size(); i++) {
                PrioridadeArea p = doDia.get(i);
                int minutos = tempos[i];
                blocosDoDia.add(new BlocoPlano(TipoBloco.ESTUDO, p.area(), p.rotulo(), minutos, p.nivel(),
                        focoPorArea.getOrDefault(p.area(), List.of())));
                minutosPorArea.merge(p.area(), minutos, Integer::sum);
                blocos.merge(p.area(), -1, Integer::sum);
            }
            if (tempos[2] > 0) {
                blocosDoDia.add(new BlocoPlano(TipoBloco.REVISAO, null, "Revisão", tempos[2], null, List.of()));
            }
            usadasOntem = new HashSet<>();
            doDia.forEach(p -> usadasOntem.add(p.area()));
            dias.add(new DiaPlano(nomeDia, blocosDoDia));
        }
        return new PlanoSemanal(dias, minutosPorArea);
    }

    /**
     * Quantos blocos da semana cada área recebe. Todas ganham ao menos 1 (prioridade alta, 2);
     * os blocos restantes vão um a um para a área com maior score/(blocos+1), método D'Hondt.
     * Nenhuma área passa de um bloco por dia.
     */
    static Map<String, Integer> distribuirBlocos(List<PrioridadeArea> prioridades, int totalBlocos) {
        Map<String, Integer> blocos = new LinkedHashMap<>();
        int restantes = totalBlocos;
        for (PrioridadeArea p : prioridades) {
            int minimo = p.nivel() == NivelPrioridade.ALTA ? 2 : 1;
            blocos.put(p.area(), minimo);
            restantes -= minimo;
        }
        while (restantes > 0) {
            PrioridadeArea melhor = null;
            double melhorQuociente = -1;
            for (PrioridadeArea p : prioridades) {
                int atual = blocos.get(p.area());
                if (atual >= DIAS.size()) continue;
                double quociente = Math.max(p.score(), 0.5) / (atual + 1);
                if (quociente > melhorQuociente) {
                    melhorQuociente = quociente;
                    melhor = p;
                }
            }
            blocos.merge(melhor.area(), 1, Integer::sum);
            restantes--;
        }
        return blocos;
    }

    /**
     * As duas áreas com mais blocos restantes (isso garante que ninguém fique sem lugar na semana),
     * preferindo, no empate, as que não foram estudadas ontem. A de maior score vem primeiro.
     */
    private List<PrioridadeArea> escolherDuasAreas(Map<String, PrioridadeArea> porArea,
                                                    Map<String, Integer> blocos, Set<String> usadasOntem) {
        List<PrioridadeArea> escolhidas = new ArrayList<>(porArea.values().stream()
                .filter(p -> blocos.get(p.area()) > 0)
                .sorted(Comparator.<PrioridadeArea>comparingInt(p -> -blocos.get(p.area()))
                        .thenComparingInt(p -> usadasOntem.contains(p.area()) ? 1 : 0)
                        .thenComparing(Comparator.<PrioridadeArea>comparingDouble(PrioridadeArea::score).reversed()))
                .limit(BLOCOS_POR_DIA)
                .toList());
        escolhidas.sort(Comparator.<PrioridadeArea>comparingDouble(PrioridadeArea::score).reversed());
        return escolhidas;
    }

    /** {bloco principal, bloco secundário, revisão}; a soma é sempre o tempo do dia. */
    static int[] dividirTempo(int minutos) {
        if (minutos < MINUTOS_MINIMOS_PARA_REVISAO) {
            int principal = arredondar(minutos * 0.6);
            return new int[] {principal, minutos - principal, 0};
        }
        int principal = arredondar(minutos * 0.5);
        int revisao = Math.max(10, arredondar(minutos * 0.2));
        return new int[] {principal, minutos - principal - revisao, revisao};
    }

    private static int arredondar(double minutos) {
        return (int) Math.round(minutos / 5.0) * 5;
    }
}
