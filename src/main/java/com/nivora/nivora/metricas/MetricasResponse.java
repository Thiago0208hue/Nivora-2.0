package com.nivora.nivora.metricas;

import java.time.LocalDateTime;

/** Somente totais agregados: nenhum dado de pessoa. */
public record MetricasResponse(long usuarios, long onboardingConcluido, long ativosUltimos7Dias,
                               long questoesRespondidas, int percentualAcertoGeral,
                               long simuladosFinalizados, long redacoes, LocalDateTime geradoEm) {
}
