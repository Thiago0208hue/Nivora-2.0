package com.nivora.nivora.personalizacao;

import java.math.BigDecimal;
import java.util.List;

public record RecomendacaoResponse(
        List<PrioridadeArea> prioridades,
        NotaCorteResponse notaCorte,
        BigDecimal notaDesejada,
        List<String> avisos) {
}
