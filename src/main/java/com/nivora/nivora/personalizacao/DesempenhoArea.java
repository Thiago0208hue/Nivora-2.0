package com.nivora.nivora.personalizacao;

import jakarta.validation.constraints.Min;

/** Questões respondidas e acertos do estudante em uma área do ENEM. */
public record DesempenhoArea(@Min(0) int respondidas, @Min(0) int acertos) {
}
