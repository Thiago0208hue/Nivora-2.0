package com.nivora.nivora.questao;

/** Só ENEM_OFICIAL pode ser apresentada como oficial, e exige ano e fonte. */
public enum OrigemQuestao {
    ENEM_OFICIAL("ENEM oficial"),
    EXEMPLO("Exemplo do Nivora (não oficial)"),
    AUTORAL("Questão autoral");

    private final String rotulo;

    OrigemQuestao(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }
}
