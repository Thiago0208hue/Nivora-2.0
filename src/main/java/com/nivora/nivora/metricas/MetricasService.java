package com.nivora.nivora.metricas;

import com.nivora.nivora.questao.RespostaQuestaoRepository;
import com.nivora.nivora.questao.SimuladoRepository;
import com.nivora.nivora.redacao.RedacaoRepository;
import com.nivora.nivora.usuario.PerfilEstudoRepository;
import com.nivora.nivora.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Instrumentação de uso para a avaliação do TCC. Reaproveita o que o sistema já grava para funcionar
 * (respostas, simulados, redações) e devolve só totais. Não cria rastreamento novo nem guarda
 * dados extras sobre as pessoas.
 */
@Service
public class MetricasService {

    private final UsuarioRepository usuarios;
    private final PerfilEstudoRepository perfis;
    private final RespostaQuestaoRepository respostas;
    private final SimuladoRepository simulados;
    private final RedacaoRepository redacoes;

    public MetricasService(UsuarioRepository usuarios, PerfilEstudoRepository perfis,
                           RespostaQuestaoRepository respostas, SimuladoRepository simulados,
                           RedacaoRepository redacoes) {
        this.usuarios = usuarios;
        this.perfis = perfis;
        this.respostas = respostas;
        this.simulados = simulados;
        this.redacoes = redacoes;
    }

    @Transactional(readOnly = true)
    public MetricasResponse resumo() {
        long respondidas = respostas.count();
        return new MetricasResponse(usuarios.count(), perfis.countByOnboardingConcluidoTrue(),
                respostas.estudantesAtivosDesde(LocalDateTime.now().minusDays(7)), respondidas,
                percentual(respostas.countByCorretaTrue(), respondidas),
                simulados.countByFinalizadoEmIsNotNull(), redacoes.count(), LocalDateTime.now());
    }

    static int percentual(long acertos, long total) {
        return total == 0 ? 0 : (int) Math.round(100.0 * acertos / total);
    }
}
