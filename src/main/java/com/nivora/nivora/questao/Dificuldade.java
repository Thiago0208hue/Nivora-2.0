package com.nivora.nivora.questao;

public enum Dificuldade {
    FACIL("Fácil"), MEDIO("Médio"), DIFICIL("Difícil");

    private final String rotulo;

    Dificuldade(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }
}
