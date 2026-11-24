package com.nivora.nivora.usuario;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;

public record PerfilResponse(
        String cursoCodigo,
        String cursoNome,
        String instituicaoSigla,
        String instituicaoNome,
        String formaIngresso,
        String modalidadeIngresso,
        String meta,
        BigDecimal notaPretendida,
        Nivel nivel,
        Integer minutosDiarios,
        Set<String> dificuldades,
        boolean onboardingConcluido) {

    /** Deve ser chamado dentro de uma transação, pois curso e instituição são carregados sob demanda. */
    public static PerfilResponse de(PerfilEstudo p) {
        return new PerfilResponse(
                p.getCurso() == null ? null : p.getCurso().getCodigo(),
                p.getCurso() == null ? null : p.getCurso().getNome(),
                p.getInstituicao() == null ? null : p.getInstituicao().getSigla(),
                p.getInstituicao() == null ? null : p.getInstituicao().getNome(),
                p.getFormaIngresso(),
                p.getModalidadeIngresso(),
                p.getMeta(),
                p.getNotaPretendida(),
                p.getNivel(),
                p.getMinutosDiarios(),
                new TreeSet<>(p.getDificuldades()),
                p.isOnboardingConcluido());
    }
}
