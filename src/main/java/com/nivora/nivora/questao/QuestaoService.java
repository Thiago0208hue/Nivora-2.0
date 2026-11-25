package com.nivora.nivora.questao;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.questao.QuestaoDtos.*;
import com.nivora.nivora.usuario.Usuario;
import com.nivora.nivora.usuario.UsuarioRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestaoService {

    static final int LIMITE_PADRAO = 10;

    private final QuestaoRepository questoes;
    private final RespostaQuestaoRepository respostas;
    private final UsuarioRepository usuarios;
    private final SeletorDeQuestoes seletor = new SeletorDeQuestoes(new Random());

    public QuestaoService(QuestaoRepository questoes, RespostaQuestaoRepository respostas,
                          UsuarioRepository usuarios) {
        this.questoes = questoes;
        this.respostas = respostas;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<CatalogoResponse> catalogo() {
        return questoes.catalogo().stream()
                .map(c -> new CatalogoResponse(c.area().name(), c.disciplina(), c.assunto(), c.ano(), c.quantidade()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestaoResponse> praticar(String email, PraticaRequest p) {
        Usuario usuario = usuario(email);
        int quantidade = p.quantidade() == null ? LIMITE_PADRAO : p.quantidade();
        List<Long> ids = questoes.idsAtivos(areaOuNull(p.area()), vazioParaNull(p.disciplina()),
                vazioParaNull(p.assunto()), p.ano(), dificuldadeOuNull(p.dificuldade()));
        List<Long> escolhidas = seletor.selecionar(ids, usos(usuario.getId(), ids), quantidade);
        return escolhidas.stream().map(id -> paraResponse(questoes.findById(id).orElseThrow())).toList();
    }

    @Transactional
    public CorrecaoResponse responder(String email, Long questaoId, String letra) {
        Usuario usuario = usuario(email);
        Questao questao = buscarAtiva(questaoId);
        return registrar(usuario, questao, null, letra);
    }

    /** Questões em que a resposta mais recente do estudante foi errada. */
    @Transactional(readOnly = true)
    public List<RevisaoResponse> revisao(String email) {
        Usuario usuario = usuario(email);
        List<Long> ids = respostas.idsComUltimoErro(usuario.getId(), PageRequest.of(0, 30));
        return ids.stream().map(id -> {
            Questao q = questoes.findById(id).orElseThrow();
            RespostaQuestao r = respostas.findFirstByUsuarioIdAndQuestaoIdOrderByIdDesc(usuario.getId(), id)
                    .orElseThrow();
            return new RevisaoResponse(paraResponse(q), r.getLetraEscolhida(), q.getCorreta().getLetra(),
                    q.getResolucao(), r.getRespondidaEm());
        }).toList();
    }

    // ---- usado também pelo SimuladoService ----

    CorrecaoResponse registrar(Usuario usuario, Questao questao, Simulado simulado, String letraInformada) {
        String letra = letraInformada.toUpperCase();
        Alternativa escolhida = questao.alternativaDaLetra(letra)
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST, "Alternativa inexistente."));
        boolean acertou = escolhida.isCorreta();
        respostas.save(new RespostaQuestao(usuario, questao, simulado, letra, acertou));
        return new CorrecaoResponse(questao.getId(), letra, acertou, questao.getCorreta().getLetra(),
                questao.getResolucao());
    }

    Map<Long, SeletorDeQuestoes.Uso> usos(Long usuarioId, List<Long> ids) {
        Map<Long, SeletorDeQuestoes.Uso> mapa = new HashMap<>();
        if (ids.isEmpty()) return mapa;
        for (var u : respostas.usoDasQuestoes(usuarioId, ids)) {
            mapa.put(u.getQuestaoId(), new SeletorDeQuestoes.Uso(u.getVezes().intValue(), u.getUltima()));
        }
        return mapa;
    }

    Questao buscarAtiva(Long id) {
        return questoes.findById(id).filter(Questao::isAtiva)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Questão não encontrada."));
    }

    Usuario usuario(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
    }

    static QuestaoResponse paraResponse(Questao q) {
        return new QuestaoResponse(q.getId(), q.getArea().name(), q.getDisciplina(), q.getAssunto(),
                q.getDificuldade() == null ? null : q.getDificuldade().name(), q.getAno(),
                q.getOrigem().getRotulo(), q.getFonte(), q.getEnunciado(),
                q.getAlternativas().stream().map(a -> new AlternativaResponse(a.getLetra(), a.getTexto())).toList());
    }

    static AreaEnem areaOuNull(String chave) {
        if (chave == null || chave.isBlank()) return null;
        return AreaEnem.daChave(chave)
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST, "Área inválida."));
    }

    static Dificuldade dificuldadeOuNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Dificuldade.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Dificuldade inválida.");
        }
    }

    static String vazioParaNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
