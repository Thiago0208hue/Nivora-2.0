package com.nivora.nivora.curso;

public enum AreaEnem {
    MATEMATICA("Matemática"),
    NATUREZA("Ciências da Natureza"),
    REDACAO("Redação"),
    LINGUAGENS("Linguagens"),
    HUMANAS("Ciências Humanas");

    private final String rotulo;

    AreaEnem(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
