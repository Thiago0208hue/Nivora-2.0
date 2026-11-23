package com.nivora.nivora.instituicao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "instituicao")
public class Instituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String sigla;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nome;

    protected Instituicao() {
    }

    public Long getId() { return id; }
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
}
