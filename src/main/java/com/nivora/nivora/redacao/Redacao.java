package com.nivora.nivora.redacao;

import com.nivora.nivora.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "redacao")
public class Redacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 300)
    private String tema;

    @Column(nullable = false, length = 8000)
    private String texto;

    @Column(nullable = false)
    private int palavras;

    @Column(nullable = false)
    private int paragrafos;

    // Autoavaliação do próprio estudante, por competência (0, 40, 80, 120, 160 ou 200); nulas se não avaliou
    private Integer c1;
    private Integer c2;
    private Integer c3;
    private Integer c4;
    private Integer c5;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    protected Redacao() {
    }

    public Redacao(Usuario usuario, String tema, String texto, int palavras, int paragrafos, int[] notas) {
        this.usuario = usuario;
        this.tema = tema;
        this.texto = texto;
        this.palavras = palavras;
        this.paragrafos = paragrafos;
        if (notas != null) {
            c1 = notas[0];
            c2 = notas[1];
            c3 = notas[2];
            c4 = notas[3];
            c5 = notas[4];
        }
    }

    @PrePersist
    void aoCriar() {
        criadaEm = LocalDateTime.now();
    }

    public boolean temAutoavaliacao() { return c1 != null; }

    public Integer total() {
        return temAutoavaliacao() ? c1 + c2 + c3 + c4 + c5 : null;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getTema() { return tema; }
    public String getTexto() { return texto; }
    public int getPalavras() { return palavras; }
    public int getParagrafos() { return paragrafos; }
    public Integer getC1() { return c1; }
    public Integer getC2() { return c2; }
    public Integer getC3() { return c3; }
    public Integer getC4() { return c4; }
    public Integer getC5() { return c5; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
