package com.nivora.nivora.usuario;

import com.nivora.nivora.curso.Curso;
import com.nivora.nivora.instituicao.Instituicao;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Dados do onboarding: objetivo, disponibilidade e dificuldades do estudante. */
@Entity
@Table(name = "perfil_estudo")
public class PerfilEstudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Column(name = "forma_ingresso", length = 40)
    private String formaIngresso;

    @Column(name = "modalidade_ingresso", length = 80)
    private String modalidadeIngresso;

    @Column(length = 60)
    private String meta;

    @Column(name = "nota_pretendida", precision = 6, scale = 2)
    private BigDecimal notaPretendida;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private Nivel nivel;

    @Min(15) @Max(720)
    @Column(name = "minutos_diarios")
    private Integer minutosDiarios;

    @ElementCollection
    @CollectionTable(name = "perfil_dificuldade", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "dificuldade", length = 40)
    private Set<String> dificuldades = new HashSet<>();

    @Column(name = "onboarding_concluido", nullable = false)
    private boolean onboardingConcluido;

    protected PerfilEstudo() {
    }

    public PerfilEstudo(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public Instituicao getInstituicao() { return instituicao; }
    public void setInstituicao(Instituicao instituicao) { this.instituicao = instituicao; }

    public String getFormaIngresso() { return formaIngresso; }
    public void setFormaIngresso(String formaIngresso) { this.formaIngresso = formaIngresso; }

    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }

    public String getModalidadeIngresso() { return modalidadeIngresso; }
    public void setModalidadeIngresso(String modalidadeIngresso) { this.modalidadeIngresso = modalidadeIngresso; }

    public BigDecimal getNotaPretendida() { return notaPretendida; }
    public void setNotaPretendida(BigDecimal notaPretendida) { this.notaPretendida = notaPretendida; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }

    public Integer getMinutosDiarios() { return minutosDiarios; }
    public void setMinutosDiarios(Integer minutosDiarios) { this.minutosDiarios = minutosDiarios; }

    public Set<String> getDificuldades() { return dificuldades; }

    public boolean isOnboardingConcluido() { return onboardingConcluido; }
    public void setOnboardingConcluido(boolean onboardingConcluido) { this.onboardingConcluido = onboardingConcluido; }
}
