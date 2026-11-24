package com.nivora.nivora.personalizacao;

import java.util.List;

/** Resultado para uma área. "area" é a chave em minúsculas (matematica, natureza...). */
public record PrioridadeArea(
        String area,
        String rotulo,
        double score,
        NivelPrioridade nivel,
        double peso,
        OrigemPeso origemPeso,
        List<String> motivos) {
}
