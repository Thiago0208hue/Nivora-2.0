package com.nivora.nivora.redacao;

import com.nivora.nivora.questao.OrigemQuestao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tema_redacao")
public class TemaRedacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private OrigemQuestao origem;

    private Integer ano;

    @Column(length = 200)
    private String fonte;

    @Column(nullable = false)
    private boolean ativo = true;

    protected TemaRedacao() {
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public OrigemQuestao getOrigem() { return origem; }
    public Integer getAno() { return ano; }
    public String getFonte() { return fonte; }
    public boolean isAtivo() { return ativo; }
}
