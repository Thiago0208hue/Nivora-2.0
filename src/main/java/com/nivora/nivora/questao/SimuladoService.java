package com.nivora.nivora.questao;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.questao.QuestaoDtos.*;
import com.nivora.nivora.usuario.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SimuladoService {

    private final SimuladoRepository simulados;
    private final QuestaoRepository questoes;
    private final RespostaQuestaoRepository respostas;
    private final QuestaoService questaoService;
    private final SeletorDeQuestoes seletor = new SeletorDeQuestoes(new Random());

    public SimuladoService(SimuladoRepository simulados, QuestaoRepository questoes,
                           RespostaQuestaoRepository respostas, QuestaoService questaoService) {
        this.simulados = simulados;
        this.questoes = questoes;
        this.respostas = respostas;
        this.questaoService = questaoService;
    }

    @Transactional
    public SimuladoResponse criar(String email, SimuladoRequest pedido) {
        Usuario usuario = questaoService.usuario(email);
        int total = pedido.quantidade() == null ? 10 : pedido.quantidade();
        AreaEnem area = null;
        List<Long> escolhidas = new ArrayList<>();

        if (pedido.tipo() == TipoSimulado.AREA) {
            area = QuestaoService.areaOuNull(pedido.area());
            if (area == null) throw new NegocioException(HttpStatus.BAD_REQUEST, "Informe a área do simulado.");
            List<Long> ids = questoes.idsAtivos(area, null, null, null, null);
            escolhidas.addAll(seletor.selecionar(ids, questaoService.usos(usuario.getId(), ids), total));
        } else {
            List<AreaEnem> comQuestoes = new ArrayList<>();
            Map<AreaEnem, List<Long>> porArea = new java.util.EnumMap<>(AreaEnem.class);
            for (AreaEnem a : AreaEnem.values()) {
                List<Long> ids = questoes.idsAtivos(a, null, null, null, null);
                if (!ids.isEmpty()) {
                    comQuestoes.add(a);
                    porArea.put(a, ids);
                }
            }
            if (comQuestoes.isEmpty()) {
                throw new NegocioException(HttpStatus.CONFLICT, "Ainda não há questões cadastradas.");
            }
            int base = total / comQuestoes.size();
            int resto = total % comQuestoes.size();
            for (int i = 0; i < comQuestoes.size(); i++) {
                List<Long> ids = porArea.get(comQuestoes.get(i));
                int cota = base + (i < resto ? 1 : 0);
                escolhidas.addAll(seletor.selecionar(ids, questaoService.usos(usuario.getId(), ids), cota));
            }
            seletor.embaralhar(escolhidas);
        }
        if (escolhidas.isEmpty()) {
            throw new NegocioException(HttpStatus.CONFLICT, "Não há questões para esse simulado.");
        }
        Simulado s = new Simulado(usuario, pedido.tipo(), area);
        s.getQuestoes().addAll(escolhidas);
        simulados.save(s);
        return paraResponse(s);
    }

    @Transactional(readOnly = true)
    public SimuladoResponse buscar(String email, Long id) {
        Usuario usuario = questaoService.usuario(email);
        return paraResponse(doUsuario(usuario, id));
    }

    @Transactional(readOnly = true)
    public List<SimuladoResumo> historico(String email) {
        Usuario usuario = questaoService.usuario(email);
        return simulados.findTop20ByUsuarioIdOrderByIdDesc(usuario.getId()).stream()
                .map(s -> new SimuladoResumo(s.getId(), s.getTipo().name(),
                        s.getArea() == null ? null : s.getArea().name(), s.getCriadoEm(), s.getFinalizadoEm(),
                        s.getQuestoes().size()))
                .toList();
    }

    /** Dentro do simulado a correção NÃO é revelada: só aparece ao finalizar. */
    @Transactional
    public void responder(String email, Long simuladoId, Long questaoId, String letra) {
        Usuario usuario = questaoService.usuario(email);
        Simulado s = doUsuario(usuario, simuladoId);
        if (s.isFinalizado()) throw new NegocioException(HttpStatus.CONFLICT, "Simulado já finalizado.");
        if (!s.getQuestoes().contains(questaoId)) {
            throw new NegocioException(HttpStatus.NOT_FOUND, "Questão não pertence a este simulado.");
        }
        if (respostas.existsBySimuladoIdAndQuestaoId(simuladoId, questaoId)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Questão já respondida neste simulado.");
        }
        questaoService.registrar(usuario, questaoService.buscarAtiva(questaoId), s, letra);
    }

    @Transactional
    public ResultadoSimulado finalizar(String email, Long simuladoId) {
        Usuario usuario = questaoService.usuario(email);
        Simulado s = doUsuario(usuario, simuladoId);
        s.finalizar();
        Map<Long, RespostaQuestao> dadas = respostas.findBySimuladoId(simuladoId).stream()
                .collect(Collectors.toMap(r -> r.getQuestao().getId(), r -> r, (a, b) -> a));
        List<ItemResultado> itens = new ArrayList<>();
        int acertos = 0;
        for (Long qid : s.getQuestoes()) {
            Questao q = questoes.findById(qid).orElseThrow();
            RespostaQuestao r = dadas.get(qid);
            boolean acertou = r != null && r.isCorreta();
            if (acertou) acertos++;
            itens.add(new ItemResultado(qid, q.getAssunto(), r == null ? null : r.getLetraEscolhida(),
                    q.getCorreta().getLetra(), acertou, q.getResolucao()));
        }
        int total = s.getQuestoes().size();
        return new ResultadoSimulado(s.getId(), total, acertos, total - dadas.size(), itens);
    }

    private Simulado doUsuario(Usuario usuario, Long id) {
        return simulados.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Simulado não encontrado."));
    }

    private SimuladoResponse paraResponse(Simulado s) {
        List<QuestaoResponse> lista = s.getQuestoes().stream()
                .map(id -> QuestaoService.paraResponse(questoes.findById(id).orElseThrow())).toList();
        List<String> respondidas = s.getId() == null ? List.of()
                : respostas.findBySimuladoId(s.getId()).stream()
                .map(r -> String.valueOf(r.getQuestao().getId())).toList();
        return new SimuladoResponse(s.getId(), s.getTipo().name(), s.getArea() == null ? null : s.getArea().name(),
                s.getCriadoEm(), s.isFinalizado(), lista, respondidas);
    }
}
