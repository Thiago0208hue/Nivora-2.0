package com.nivora.nivora.personalizacao;

import java.math.BigDecimal;

public record NotaCorteResponse(
        BigDecimal nota, Integer ano, String modalidade, String fonte, String link, boolean estimado) {

    public static NotaCorteResponse de(NotaCorte n) {
        return new NotaCorteResponse(n.getNota(), n.getAno(), n.getModalidade(), n.getFonte(), n.getLink(), n.isEstimado());
    }
}
