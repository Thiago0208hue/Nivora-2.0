package com.nivora.nivora.questao;

import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.usuario.Usuario;
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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "simulado")
public class Simulado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoSimulado tipo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private AreaEnem area;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    // IDs das questões, na ordem em que aparecem no simulado
    @ElementCollection
    @CollectionTable(name = "simulado_questao", joinColumns = @JoinColumn(name = "simulado_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "questao_id", nullable = false)
    private List<Long> questoes = new ArrayList<>();

    protected Simulado() {
    }

    public Simulado(Usuario usuario, TipoSimulado tipo, AreaEnem area) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.area = area;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    public boolean isFinalizado() { return finalizadoEm != null; }

    public void finalizar() {
        if (finalizadoEm == null) finalizadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public TipoSimulado getTipo() { return tipo; }
    public AreaEnem getArea() { return area; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getFinalizadoEm() { return finalizadoEm; }
    public List<Long> getQuestoes() { return questoes; }
}
