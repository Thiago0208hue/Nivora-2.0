package com.nivora.nivora.curso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Curso com os pesos padrão de cada área do ENEM (escala 1 a 5).
 * Enquanto pesosEstimados for true, os pesos são recomendação do Nivora, não dado oficial.
 */
@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private boolean generico;

    @Column(name = "pesos_estimados", nullable = false)
    private boolean pesosEstimados = true;

    @Min(1) @Max(5)
    @Column(name = "peso_matematica", nullable = false)
    private int pesoMatematica;

    @Min(1) @Max(5)
    @Column(name = "peso_natureza", nullable = false)
    private int pesoNatureza;

    @Min(1) @Max(5)
    @Column(name = "peso_redacao", nullable = false)
    private int pesoRedacao;

    @Min(1) @Max(5)
    @Column(name = "peso_linguagens", nullable = false)
    private int pesoLinguagens;

    @Min(1) @Max(5)
    @Column(name = "peso_humanas", nullable = false)
    private int pesoHumanas;

    protected Curso() {
    }

    public int pesoDaArea(AreaEnem area) {
        return switch (area) {
            case MATEMATICA -> pesoMatematica;
            case NATUREZA -> pesoNatureza;
            case REDACAO -> pesoRedacao;
            case LINGUAGENS -> pesoLinguagens;
            case HUMANAS -> pesoHumanas;
        };
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public boolean isGenerico() { return generico; }
    public boolean isPesosEstimados() { return pesosEstimados; }
}
