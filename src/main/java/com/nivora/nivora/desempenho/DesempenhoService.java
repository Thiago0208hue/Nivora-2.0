package com.nivora.nivora.desempenho;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.desempenho.DesempenhoDtos.*;
import com.nivora.nivora.personalizacao.DesempenhoArea;
import com.nivora.nivora.questao.RespostaQuestaoRepository;
import com.nivora.nivora.usuario.Usuario;
import com.nivora.nivora.usuario.UsuarioRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lê as respostas gravadas e monta o desempenho do estudante; o servidor é a fonte da verdade. */
@Service
public class DesempenhoService {

    private static final int JANELA_DIAS = 365;
    private static final int DIAS_EXIBIDOS = 14;

    private final RespostaQuestaoRepository respostas;
    private final UsuarioRepository usuarios;
    private final CalculadoraProgresso calculadora = new CalculadoraProgresso();

    public DesempenhoService(RespostaQuestaoRepository respostas, UsuarioRepository usuarios) {
        this.respostas = respostas;
        this.usuarios = usuarios;
    }

    /** Desempenho por área no formato que a personalização e o plano já consomem (chave em minúsculas). */
    @Transactional(readOnly = true)
    public Map<String, DesempenhoArea> porArea(String email) {
        Usuario usuario = usuario(email);
        Map<String, DesempenhoArea> mapa = new LinkedHashMap<>();
        for (var a : respostas.agregadoPorArea(usuario.getId())) {
            mapa.put(a.getArea().name().toLowerCase(), new DesempenhoArea(a.getRespondidas().intValue(), a.getAcertos().intValue()));
        }
        return mapa;
    }

    @Transactional(readOnly = true)
    public DesempenhoResponse resumo(String email) {
        return resumo(email, LocalDate.now());
    }

    DesempenhoResponse resumo(String email, LocalDate hoje) {
        Usuario usuario = usuario(email);

        List<AreaDesempenho> areas = respostas.agregadoPorArea(usuario.getId()).stream()
                .sorted(Comparator.comparing(a -> a.getArea().ordinal()))
                .map(a -> new AreaDesempenho(a.getArea().name(), a.getArea().getRotulo(),
                        a.getRespondidas().intValue(), a.getAcertos().intValue(),
                        percentual(a.getAcertos(), a.getRespondidas())))
                .toList();
        List<AssuntoDesempenho> assuntos = respostas.agregadoPorAssunto(usuario.getId()).stream()
                .sorted(Comparator.comparing((RespostaQuestaoRepository.AssuntoAgregado a) -> a.getArea().ordinal())
                        .thenComparing(RespostaQuestaoRepository.AssuntoAgregado::getAssunto))
                .map(a -> new AssuntoDesempenho(a.getArea().name(), a.getAssunto(), a.getRespondidas().intValue(),
                        a.getAcertos().intValue(), percentual(a.getAcertos(), a.getRespondidas())))
                .toList();

        int total = areas.stream().mapToInt(AreaDesempenho::respondidas).sum();
        int acertos = areas.stream().mapToInt(AreaDesempenho::acertos).sum();

        // Atividade por dia (dentro da janela de 1 ano)
        Map<LocalDate, int[]> porDia = new TreeMap<>();
        for (var r : respostas.findByUsuarioIdAndRespondidaEmAfter(usuario.getId(),
                hoje.minusDays(JANELA_DIAS).atStartOfDay())) {
            int[] c = porDia.computeIfAbsent(r.getRespondidaEm().toLocalDate(), d -> new int[2]);
            c[0]++;
            if (r.getCorreta()) c[1]++;
        }
        Set<LocalDate> dias = new HashSet<>(porDia.keySet());
        List<DiaDesempenho> ultimos = new ArrayList<>();
        porDia.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(hoje.minusDays(DIAS_EXIBIDOS - 1)))
                .forEach(e -> ultimos.add(new DiaDesempenho(e.getKey().toString(), e.getValue()[0], e.getValue()[1],
                        percentual(e.getValue()[1], e.getValue()[0]))));

        int xp = calculadora.xp(acertos);
        Gamificacao gam = new Gamificacao(xp, calculadora.nivel(xp), calculadora.xpNoNivel(xp),
                calculadora.conquistas(xp, calculadora.maiorSequencia(dias), total));

        AssuntoDesempenho fraco = calculadora.assuntoMaisFraco(assuntos.stream()
                        .map(a -> new CalculadoraProgresso.AssuntoStats(a.assunto(), a.respondidas(), a.acertos())).toList())
                .flatMap(f -> assuntos.stream().filter(a -> a.assunto().equals(f.assunto())).findFirst())
                .orElse(null);

        return new DesempenhoResponse(total, acertos, percentual(acertos, total),
                calculadora.sequenciaAtual(dias, hoje), areas, assuntos, ultimos, fraco, gam);
    }

    private static int percentual(long acertos, long respondidas) {
        return respondidas == 0 ? 0 : (int) Math.round(100.0 * acertos / respondidas);
    }

    private Usuario usuario(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
    }
}
