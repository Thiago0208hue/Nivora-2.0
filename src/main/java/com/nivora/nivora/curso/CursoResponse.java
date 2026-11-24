package com.nivora.nivora.curso;

import java.util.LinkedHashMap;
import java.util.Map;

/** Curso para o frontend. As chaves de pesos são as áreas em minúsculas (matematica, natureza...). */
public record CursoResponse(String codigo, String nome, boolean pesosEstimados, Map<String, Integer> pesos) {

    public static CursoResponse de(Curso curso) {
        Map<String, Integer> pesos = new LinkedHashMap<>();
        for (AreaEnem area : AreaEnem.values()) {
            pesos.put(area.name().toLowerCase(), curso.pesoDaArea(area));
        }
        return new CursoResponse(curso.getCodigo(), curso.getNome(), curso.isPesosEstimados(), pesos);
    }
}
