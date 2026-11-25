package com.nivora.nivora.questao;

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

/** Uma resposta do estudante a uma questão, na prática livre (simulado nulo) ou em um simulado. */
@Entity
@Table(name = "resposta_questao")
public class RespostaQuestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulado_id")
    private Simulado simulado;

    @Column(name = "letra_escolhida", nullable = false, length = 1)
    private String letraEscolhida;

    @Column(nullable = false)
    private boolean correta;

    @Column(name = "respondida_em", nullable = false, updatable = false)
    private LocalDateTime respondidaEm;

    protected RespostaQuestao() {
    }

    public RespostaQuestao(Usuario usuario, Questao questao, Simulado simulado, String letraEscolhida, boolean correta) {
        this.usuario = usuario;
        this.questao = questao;
        this.simulado = simulado;
        this.letraEscolhida = letraEscolhida;
        this.correta = correta;
    }

    @PrePersist
    void aoCriar() {
        respondidaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Questao getQuestao() { return questao; }
    public String getLetraEscolhida() { return letraEscolhida; }
    public boolean isCorreta() { return correta; }
    public LocalDateTime getRespondidaEm() { return respondidaEm; }
}
