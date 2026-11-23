package com.nivora.nivora.personalizacao;

import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.curso.Curso;
import com.nivora.nivora.instituicao.Instituicao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Peso de uma área do ENEM para um curso em uma instituição, sempre com ano e fonte. */
@Entity
@Table(name = "peso_instituicao")
public class PesoInstituicao {

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AreaEnem area;

    @NotNull
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal peso;

    @NotNull
    @Column(nullable = false)
    private Integer ano;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String fonte;

    @Column(length = 300)
    private String link;

    @Column(nullable = false)
    private boolean estimado;

    protected PesoInstituicao() {
    }

    public PesoInstituicao(Curso curso, Instituicao instituicao, AreaEnem area,
                           BigDecimal peso, Integer ano, String fonte, boolean estimado) {
        this.curso = curso;
        this.instituicao = instituicao;
        this.area = area;
        this.peso = peso;
        this.ano = ano;
        this.fonte = fonte;
        this.estimado = estimado;
    }

    public Long getId() { return id; }
    public Curso getCurso() { return curso; }
    public Instituicao getInstituicao() { return instituicao; }
    public AreaEnem getArea() { return area; }
    public BigDecimal getPeso() { return peso; }
    public Integer getAno() { return ano; }
    public String getFonte() { return fonte; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isEstimado() { return estimado; }
}
