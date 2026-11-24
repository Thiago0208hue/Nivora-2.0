package com.nivora.nivora.personalizacao;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.curso.Curso;
import com.nivora.nivora.instituicao.Instituicao;
import com.nivora.nivora.usuario.PerfilEstudo;
import com.nivora.nivora.usuario.PerfilEstudoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reúne os dados do estudante (perfil, pesos, notas de corte) e pede o cálculo à calculadora. */
@Service
public class PersonalizacaoService {

    private final PerfilEstudoRepository perfis;
    private final PesoInstituicaoRepository pesosInstituicao;
    private final NotaCorteRepository notasCorte;
    private final CalculadoraPrioridades calculadora;

    public PersonalizacaoService(PerfilEstudoRepository perfis, PesoInstituicaoRepository pesosInstituicao,
                                 NotaCorteRepository notasCorte, CalculadoraPrioridades calculadora) {
        this.perfis = perfis;
        this.pesosInstituicao = pesosInstituicao;
        this.notasCorte = notasCorte;
        this.calculadora = calculadora;
    }

    @Transactional(readOnly = true)
    public RecomendacaoResponse recomendar(String email, Map<String, DesempenhoArea> desempenhoInformado) {
        PerfilEstudo perfil = perfis.findByUsuarioEmail(email)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Perfil não encontrado."));
        Curso curso = perfil.getCurso();
        if (curso == null) {
            throw new NegocioException(HttpStatus.CONFLICT, "Conclua o onboarding para receber recomendações.");
        }
        Instituicao instituicao = perfil.getInstituicao();
        List<String> avisos = new ArrayList<>();

        Map<AreaEnem, PesoArea> pesos = resolverPesos(curso, instituicao, avisos);
        NotaCorteResponse corte = buscarNotaCorte(perfil, avisos);
        BigDecimal notaAlvo = perfil.getNotaPretendida() != null
                ? perfil.getNotaPretendida()
                : corte == null ? null : corte.nota();
        avisarNotaAbaixoDoCorte(perfil.getNotaPretendida(), corte, avisos);

        EntradaPrioridades entrada = new EntradaPrioridades(pesos, agruparDificuldades(perfil),
                perfil.getNivel(), perfil.getMinutosDiarios(), converterDesempenho(desempenhoInformado), notaAlvo);
        avisarTempo(perfil.getMinutosDiarios(), avisos);

        if (pesos.values().stream().anyMatch(p -> p.origem() != OrigemPeso.INSTITUICAO_OFICIAL)) {
            avisos.add("Parte dos pesos é estimativa do Nivora, não peso oficial de edital. Consulte sempre o edital da instituição.");
        }
        return new RecomendacaoResponse(calculadora.calcular(entrada), corte, perfil.getNotaPretendida(), avisos);
    }

    /** Peso da instituição (ano mais recente) quando existir; senão, a estimativa do curso. */
    private Map<AreaEnem, PesoArea> resolverPesos(Curso curso, Instituicao instituicao, List<String> avisos) {
        Map<AreaEnem, PesoArea> pesos = new EnumMap<>(AreaEnem.class);
        for (AreaEnem area : AreaEnem.values()) {
            pesos.put(area, new PesoArea(curso.pesoDaArea(area), OrigemPeso.CURSO_ESTIMADO,
                    "estimativa do Nivora para " + curso.getNome()));
        }
        if (instituicao == null) {
            avisos.add("Nenhuma instituição escolhida: usando os pesos estimados do curso.");
            return pesos;
        }

        Map<AreaEnem, PesoInstituicao> maisRecentes = new EnumMap<>(AreaEnem.class);
        for (PesoInstituicao peso : pesosInstituicao.findByCursoIdAndInstituicaoId(curso.getId(), instituicao.getId())) {
            PesoInstituicao atual = maisRecentes.get(peso.getArea());
            if (atual == null || peso.getAno() > atual.getAno()) {
                maisRecentes.put(peso.getArea(), peso);
            }
        }
        if (maisRecentes.isEmpty()) {
            avisos.add("Ainda não há pesos de " + instituicao.getNome() + " para " + curso.getNome()
                    + " cadastrados; usando os pesos estimados do curso.");
        }
        maisRecentes.forEach((area, peso) -> pesos.put(area, new PesoArea(
                Math.max(0, Math.min(5, peso.getPeso().doubleValue())),
                peso.isEstimado() ? OrigemPeso.INSTITUICAO_ESTIMADO : OrigemPeso.INSTITUICAO_OFICIAL,
                instituicao.getNome() + " " + peso.getAno() + (peso.isEstimado() ? ", estimativa" : "")
                        + " (fonte: " + peso.getFonte() + ")")));
        return pesos;
    }

    /** Nota de corte mais recente da modalidade escolhida; sem correspondência, não usa nenhuma. */
    private NotaCorteResponse buscarNotaCorte(PerfilEstudo perfil, List<String> avisos) {
        if (perfil.getInstituicao() == null || perfil.getModalidadeIngresso() == null) {
            return null;
        }
        NotaCorteResponse corte = notasCorte
                .findByCursoIdAndInstituicaoIdOrderByAnoDesc(perfil.getCurso().getId(), perfil.getInstituicao().getId())
                .stream()
                .filter(n -> n.getModalidade().equalsIgnoreCase(perfil.getModalidadeIngresso()))
                .findFirst()
                .map(NotaCorteResponse::de)
                .orElse(null);
        if (corte == null) {
            avisos.add("Não há nota de corte cadastrada para a modalidade \"" + perfil.getModalidadeIngresso()
                    + "\"; consulte o edital da instituição.");
        } else {
            avisos.add("Nota de corte de referência: " + corte.nota().toPlainString() + " (" + corte.ano() + ", "
                    + corte.modalidade() + ")" + (corte.estimado() ? ", estimativa" : "")
                    + " — fonte: " + corte.fonte() + ".");
        }
        return corte;
    }

    private void avisarNotaAbaixoDoCorte(BigDecimal notaDesejada, NotaCorteResponse corte, List<String> avisos) {
        if (notaDesejada != null && corte != null && notaDesejada.compareTo(corte.nota()) < 0) {
            avisos.add("Sua nota desejada (" + notaDesejada.toPlainString()
                    + ") está abaixo da nota de corte de referência (" + corte.nota().toPlainString() + ").");
        }
    }

    private void avisarTempo(Integer minutos, List<String> avisos) {
        int ajuste = CalculadoraPrioridades.ajusteTempo(minutos);
        if (ajuste > 0) {
            avisos.add("Com pouco tempo por dia, o Nivora destaca só o essencial como prioridade alta.");
        } else if (ajuste < 0) {
            avisos.add("Com bastante tempo por dia, o Nivora destaca mais áreas como prioridade.");
        }
    }

    private Map<AreaEnem, List<String>> agruparDificuldades(PerfilEstudo perfil) {
        Map<AreaEnem, List<String>> porArea = new EnumMap<>(AreaEnem.class);
        perfil.getDificuldades().stream().sorted().forEach(disciplina ->
                AreaEnem.daDisciplina(disciplina).ifPresent(area ->
                        porArea.computeIfAbsent(area, a -> new ArrayList<>()).add(disciplina)));
        return porArea;
    }

    private Map<AreaEnem, DesempenhoArea> converterDesempenho(Map<String, DesempenhoArea> informado) {
        Map<AreaEnem, DesempenhoArea> convertido = new EnumMap<>(AreaEnem.class);
        if (informado == null) {
            return convertido;
        }
        informado.forEach((chave, desempenho) -> {
            AreaEnem area;
            try {
                area = AreaEnem.valueOf(chave.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "Área desconhecida: " + chave);
            }
            if (desempenho.acertos() > desempenho.respondidas()) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "Acertos não podem superar as questões respondidas.");
            }
            convertido.put(area, desempenho);
        });
        return convertido;
    }
}
