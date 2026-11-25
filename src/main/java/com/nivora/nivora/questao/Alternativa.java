package com.nivora.nivora.questao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "alternativa")
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @Column(nullable = false, length = 1)
    private String letra;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false)
    private boolean correta;

    protected Alternativa() {
    }

    Alternativa(Questao questao, String letra, String texto, boolean correta) {
        this.questao = questao;
        this.letra = letra;
        this.texto = texto;
        this.correta = correta;
    }

    public String getLetra() { return letra; }
    public String getTexto() { return texto; }
    public boolean isCorreta() { return correta; }
}
