package com.nivora.nivora.usuario;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

/** Dados do onboarding enviados pelo estudante. Curso e instituição são identificados pelo código. */
public record PerfilRequest(
        @Size(max = 40) String cursoCodigo,
        @Size(max = 30) String instituicaoSigla,
        @Size(max = 80) String modalidadeIngresso,
        @DecimalMin("0") @DecimalMax("1000") BigDecimal notaPretendida,
        Nivel nivel,
        @Min(15) @Max(720) Integer minutosDiarios,
        @Size(max = 12) Set<@Size(max = 40) String> dificuldades,
        Boolean onboardingConcluido) {
}
