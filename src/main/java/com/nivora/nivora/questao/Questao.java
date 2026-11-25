package com.nivora.nivora.questao;

import com.nivora.nivora.curso.AreaEnem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "questao")
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AreaEnem area;

    @Column(nullable = false, length = 40)
    private String disciplina;

    @Column(nullable = false, length = 80)
    private String assunto;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 10)
    private Dificuldade dificuldade;

    private Integer ano;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private OrigemQuestao origem;

    @Column(length = 200)
    private String fonte;

    @Column(nullable = false, length = 6000)
    private String enunciado;

    @Column(length = 3000)
    private String resolucao;

    @Column(nullable = false)
    private boolean ativa = true;

    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("letra")
    private List<Alternativa> alternativas = new ArrayList<>();

    protected Questao() {
    }

    public Questao(AreaEnem area, String disciplina, String assunto, Dificuldade dificuldade, Integer ano,
                   OrigemQuestao origem, String fonte, String enunciado, String resolucao) {
        this.area = area;
        this.disciplina = disciplina;
        this.assunto = assunto;
        this.dificuldade = dificuldade;
        this.ano = ano;
        this.origem = origem;
        this.fonte = fonte;
        this.enunciado = enunciado;
        this.resolucao = resolucao;
    }

    public void adicionarAlternativa(String letra, String texto, boolean correta) {
        alternativas.add(new Alternativa(this, letra, texto, correta));
    }

    public Optional<Alternativa> alternativaDaLetra(String letra) {
        return alternativas.stream().filter(a -> a.getLetra().equals(letra)).findFirst();
    }

    public Alternativa getCorreta() {
        return alternativas.stream().filter(Alternativa::isCorreta).findFirst().orElseThrow();
    }

    public void desativar() { ativa = false; }

    public Long getId() { return id; }
    public AreaEnem getArea() { return area; }
    public String getDisciplina() { return disciplina; }
    public String getAssunto() { return assunto; }
    public Dificuldade getDificuldade() { return dificuldade; }
    public Integer getAno() { return ano; }
    public OrigemQuestao getOrigem() { return origem; }
    public String getFonte() { return fonte; }
    public String getEnunciado() { return enunciado; }
    public String getResolucao() { return resolucao; }
    public boolean isAtiva() { return ativa; }
    public List<Alternativa> getAlternativas() { return alternativas; }
}
