package com.nivora.nivora.curso;

import java.util.Optional;

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

    /** Aceita a chave em minúsculas ou maiúsculas (matematica, MATEMATICA). */
    public static Optional<AreaEnem> daChave(String chave) {
        if (chave == null || chave.isBlank()) return Optional.empty();
        try {
            return Optional.of(valueOf(chave.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Área do ENEM a que uma disciplina pertence (as mesmas do formulário de onboarding). */
    public static Optional<AreaEnem> daDisciplina(String disciplina) {
        return switch (disciplina) {
            case "Matemática" -> Optional.of(MATEMATICA);
            case "Física", "Química", "Biologia" -> Optional.of(NATUREZA);
            case "Português", "Literatura", "Inglês/Espanhol" -> Optional.of(LINGUAGENS);
            case "Redação" -> Optional.of(REDACAO);
            case "História", "Geografia", "Filosofia", "Sociologia" -> Optional.of(HUMANAS);
            default -> Optional.empty();
        };
    }
}
