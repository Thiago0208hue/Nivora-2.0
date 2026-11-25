package com.nivora.nivora.plano;

import com.nivora.nivora.personalizacao.NivelPrioridade;
import java.util.List;

/**
 * Um bloco de estudo do dia. Em blocos de revisão, area, prioridade e foco são nulos/vazios.
 * "foco" lista as disciplinas em que o estudante relatou dificuldade naquela área.
 */
public record BlocoPlano(
        TipoBloco tipo,
        String area,
        String rotulo,
        int minutos,
        NivelPrioridade prioridade,
        List<String> foco) {
}
