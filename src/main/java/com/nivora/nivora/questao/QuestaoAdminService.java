package com.nivora.nivora.questao;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.questao.QuestaoDtos.*;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cadastro de questões (somente ADMIN). Não inventa conteúdo: valida o que a pessoa cadastra. */
@Service
public class QuestaoAdminService {

    private final QuestaoRepository questoes;

    public QuestaoAdminService(QuestaoRepository questoes) {
        this.questoes = questoes;
    }

    @Transactional
    public QuestaoResponse criar(QuestaoAdminRequest p) {
        AreaEnem area = QuestaoService.areaOuNull(p.area());
        if (area == null) throw new NegocioException(HttpStatus.BAD_REQUEST, "Informe a área.");
        if (p.origem() == OrigemQuestao.ENEM_OFICIAL
                && (p.ano() == null || p.fonte() == null || p.fonte().isBlank())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST,
                    "Questão oficial do ENEM exige ano e fonte (ex.: caderno e número da questão).");
        }
        long corretas = p.alternativas().stream().filter(AlternativaAdmin::correta).count();
        if (corretas != 1) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Marque exatamente uma alternativa correta.");
        }
        Set<String> letras = new HashSet<>();
        for (AlternativaAdmin a : p.alternativas()) {
            if (!letras.add(a.letra())) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "Letra de alternativa repetida: " + a.letra());
            }
        }
        Questao q = new Questao(area, p.disciplina(), p.assunto(), QuestaoService.dificuldadeOuNull(p.dificuldade()),
                p.ano(), p.origem(), p.fonte(), p.enunciado(), p.resolucao());
        p.alternativas().forEach(a -> q.adicionarAlternativa(a.letra(), a.texto(), a.correta()));
        questoes.save(q);
        return QuestaoService.paraResponse(q);
    }

    /** Não apaga: desativa, para preservar o histórico de respostas dos estudantes. */
    @Transactional
    public void desativar(Long id) {
        Questao q = questoes.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Questão não encontrada."));
        q.desativar();
    }
}
