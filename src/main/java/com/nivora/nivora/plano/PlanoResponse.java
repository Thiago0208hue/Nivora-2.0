package com.nivora.nivora.plano;

import java.util.List;
import java.util.Map;

public record PlanoResponse(
        int minutosPorDia,
        List<DiaPlano> dias,
        Map<String, Integer> minutosPorArea,
        List<String> avisos) {
}
