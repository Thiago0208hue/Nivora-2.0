package com.nivora.nivora.personalizacao;

import com.nivora.nivora.curso.Curso;
import com.nivora.nivora.instituicao.Instituicao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Nota de corte de um curso em uma instituição e modalidade, sempre com ano e fonte. */
@Entity
@Table(name = "nota_corte")
public class NotaCorte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String modalidade;

    @NotNull
    @Column(nullable = false)
    private Integer ano;

    @NotNull
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal nota;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String fonte;

    @Column(length = 300)
    private String link;

    @Column(nullable = false)
    private boolean estimado;

    protected NotaCorte() {
    }

    public NotaCorte(Curso curso, Instituicao instituicao, String modalidade,
                     Integer ano, BigDecimal nota, String fonte, boolean estimado) {
        this.curso = curso;
        this.instituicao = instituicao;
        this.modalidade = modalidade;
        this.ano = ano;
        this.nota = nota;
        this.fonte = fonte;
        this.estimado = estimado;
    }

    public Long getId() { return id; }
    public Curso getCurso() { return curso; }
    public Instituicao getInstituicao() { return instituicao; }
    public String getModalidade() { return modalidade; }
    public Integer getAno() { return ano; }
    public BigDecimal getNota() { return nota; }
    public String getFonte() { return fonte; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isEstimado() { return estimado; }
}
