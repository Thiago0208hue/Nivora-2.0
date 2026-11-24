package com.nivora.nivora.personalizacao;

import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.usuario.Nivel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Tudo o que a calculadora precisa. Qualquer dado pode faltar (null ou mapa vazio). */
public record EntradaPrioridades(
        Map<AreaEnem, PesoArea> pesos,
        Map<AreaEnem, List<String>> dificuldades,
        Nivel nivel,
        Integer minutosDiarios,
        Map<AreaEnem, DesempenhoArea> desempenho,
        BigDecimal notaAlvo) {
}
