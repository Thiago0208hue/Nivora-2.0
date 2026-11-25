package com.nivora.nivora.plano;

import java.util.List;
import java.util.Map;

/** Dias da semana e o total de minutos de estudo por área (chave em minúsculas). */
public record PlanoSemanal(List<DiaPlano> dias, Map<String, Integer> minutosPorArea) {
}
