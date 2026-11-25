package com.nivora.nivora.desempenho;

import java.util.List;

public final class DesempenhoDtos {

    private DesempenhoDtos() {
    }

    public record Taxa(int respondidas, int acertos, int percentual) {
    }

    public record AreaDesempenho(String area, String rotulo, int respondidas, int acertos, int percentual) {
    }

    public record AssuntoDesempenho(String area, String assunto, int respondidas, int acertos, int percentual) {
    }

    public record DiaDesempenho(String data, int respondidas, int acertos, int percentual) {
    }

    public record Gamificacao(int xp, int nivel, int xpNoNivel, List<String> conquistas) {
    }

    public record DesempenhoResponse(int respondidas, int acertos, int percentual, int sequenciaDias,
                                     List<AreaDesempenho> porArea, List<AssuntoDesempenho> porAssunto,
                                     List<DiaDesempenho> ultimosDias, AssuntoDesempenho assuntoMaisFraco,
                                     Gamificacao gamificacao) {
    }
}
