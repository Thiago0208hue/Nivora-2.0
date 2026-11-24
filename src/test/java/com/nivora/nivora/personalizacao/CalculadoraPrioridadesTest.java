package com.nivora.nivora.personalizacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.usuario.Nivel;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CalculadoraPrioridadesTest {

    private final CalculadoraPrioridades calculadora = new CalculadoraPrioridades();

    // Pesos estimados do curso de Direito no protótipo
    private Map<AreaEnem, PesoArea> pesosDireito() {
        Map<AreaEnem, PesoArea> pesos = new EnumMap<>(AreaEnem.class);
        pesos.put(AreaEnem.MATEMATICA, peso(2));
        pesos.put(AreaEnem.NATUREZA, peso(2));
        pesos.put(AreaEnem.REDACAO, peso(5));
        pesos.put(AreaEnem.LINGUAGENS, peso(5));
        pesos.put(AreaEnem.HUMANAS, peso(5));
        return pesos;
    }

    private PesoArea peso(double valor) {
        return new PesoArea(valor, OrigemPeso.CURSO_ESTIMADO, "estimativa de teste");
    }

    private EntradaPrioridades entrada(Map<AreaEnem, PesoArea> pesos, Nivel nivel, Integer minutos,
                                       Map<AreaEnem, List<String>> dificuldades,
                                       Map<AreaEnem, DesempenhoArea> desempenho, BigDecimal notaAlvo) {
        return new EntradaPrioridades(pesos, dificuldades, nivel, minutos, desempenho, notaAlvo);
    }

    private EntradaPrioridades simples(Nivel nivel, Integer minutos) {
        return entrada(pesosDireito(), nivel, minutos, Map.of(), Map.of(), null);
    }

    private PrioridadeArea da(List<PrioridadeArea> lista, String area) {
        return lista.stream().filter(p -> p.area().equals(area)).findFirst().orElseThrow();
    }

    @Test
    void cursoComPesoAltoGeraPrioridadeAltaMesmoSemDificuldades() {
        List<PrioridadeArea> r = calculadora.calcular(simples(Nivel.INTERMEDIARIO, 120));

        assertEquals(NivelPrioridade.ALTA, da(r, "redacao").nivel());
        assertEquals(NivelPrioridade.ALTA, da(r, "linguagens").nivel());
        assertEquals(NivelPrioridade.ALTA, da(r, "humanas").nivel());
        assertEquals(NivelPrioridade.BAIXA, da(r, "matematica").nivel());
        assertEquals(NivelPrioridade.BAIXA, da(r, "natureza").nivel());
    }

    @Test
    void sempreDevolveAsCincoAreas() {
        assertEquals(5, calculadora.calcular(simples(null, null)).size());
    }

    @Test
    void dificuldadeSoma2PontosEExplicaOMotivo() {
        var dificuldades = Map.of(AreaEnem.MATEMATICA, List.of("Matemática"));
        List<PrioridadeArea> r = calculadora.calcular(
                entrada(pesosDireito(), Nivel.INTERMEDIARIO, 120, dificuldades, Map.of(), null));

        PrioridadeArea matematica = da(r, "matematica");
        assertEquals(4.0, matematica.score());
        assertEquals(NivelPrioridade.MEDIA, matematica.nivel());
        assertTrue(matematica.motivos().stream().anyMatch(m -> m.contains("dificuldade em: Matemática")));
    }

    @Test
    void nivelMudaAClassificacaoMasNaoAOrdem() {
        List<PrioridadeArea> iniciante = calculadora.calcular(simples(Nivel.INICIANTE, 120));
        List<PrioridadeArea> avancado = calculadora.calcular(simples(Nivel.AVANCADO, 120));

        assertEquals(iniciante.stream().map(PrioridadeArea::area).toList(),
                avancado.stream().map(PrioridadeArea::area).toList());
        assertEquals(6.0, da(iniciante, "redacao").score());
        assertEquals(4.5, da(avancado, "redacao").score());
        assertEquals(NivelPrioridade.MEDIA, da(avancado, "redacao").nivel());
    }

    @Test
    void poucoTempoReduzOsItensDePrioridadeAltaEMuitoTempoAmplia() {
        assertEquals(NivelPrioridade.MEDIA, da(calculadora.calcular(simples(Nivel.INTERMEDIARIO, 30)), "redacao").nivel());
        assertEquals(NivelPrioridade.ALTA, da(calculadora.calcular(simples(Nivel.INTERMEDIARIO, 120)), "redacao").nivel());
        // 240 min: limiares caem 1 ponto, então peso 4 (ex.: dificuldade em área de peso 2) vira alta
        var dificuldades = Map.of(AreaEnem.MATEMATICA, List.of("Matemática"));
        PrioridadeArea matematica = da(calculadora.calcular(
                entrada(pesosDireito(), Nivel.INTERMEDIARIO, 240, dificuldades, Map.of(), null)), "matematica");
        assertEquals(NivelPrioridade.ALTA, matematica.nivel());
    }

    @Test
    void desempenhoBaixoSobeEDesempenhoAltoDesce() {
        var desempenho = Map.of(
                AreaEnem.MATEMATICA, new DesempenhoArea(10, 3),   // 30%: +2
                AreaEnem.REDACAO, new DesempenhoArea(10, 10),     // 100%: -1
                AreaEnem.NATUREZA, new DesempenhoArea(10, 6));    // 60%: +1
        List<PrioridadeArea> r = calculadora.calcular(
                entrada(pesosDireito(), Nivel.INTERMEDIARIO, 120, Map.of(), desempenho, null));

        assertEquals(4.0, da(r, "matematica").score());
        assertEquals(3.0, da(r, "natureza").score());
        assertEquals(4.0, da(r, "redacao").score());
    }

    @Test
    void poucasQuestoesNaoAlteramAPrioridade() {
        var desempenho = Map.of(AreaEnem.MATEMATICA, new DesempenhoArea(2, 0));
        List<PrioridadeArea> r = calculadora.calcular(
                entrada(pesosDireito(), Nivel.INTERMEDIARIO, 120, Map.of(), desempenho, null));

        assertEquals(2.0, da(r, "matematica").score());
    }

    @Test
    void notaAlvoAltaReforcaApenasAreasDePesoAlto() {
        List<PrioridadeArea> r = calculadora.calcular(
                entrada(pesosDireito(), Nivel.INTERMEDIARIO, 120, Map.of(), Map.of(), new BigDecimal("720")));

        assertEquals(5.5, da(r, "redacao").score());
        assertEquals(2.0, da(r, "matematica").score());
        assertTrue(da(r, "redacao").motivos().stream().anyMatch(m -> m.contains("nota-alvo")));
    }

    @Test
    void notaAlvoBaixaNaoMudaNada() {
        List<PrioridadeArea> r = calculadora.calcular(
                entrada(pesosDireito(), Nivel.INTERMEDIARIO, 120, Map.of(), Map.of(), new BigDecimal("600")));

        assertEquals(5.0, da(r, "redacao").score());
    }

    @Test
    void pesoDaInstituicaoDefineAOrdem() {
        Map<AreaEnem, PesoArea> pesos = pesosDireito();
        pesos.put(AreaEnem.MATEMATICA, new PesoArea(5, OrigemPeso.INSTITUICAO_OFICIAL, "instituição de teste"));
        pesos.put(AreaEnem.REDACAO, new PesoArea(2, OrigemPeso.INSTITUICAO_OFICIAL, "instituição de teste"));

        List<PrioridadeArea> r = calculadora.calcular(entrada(pesos, Nivel.INTERMEDIARIO, 120, Map.of(), Map.of(), null));

        assertEquals(OrigemPeso.INSTITUICAO_OFICIAL, da(r, "matematica").origemPeso());
        assertEquals(NivelPrioridade.ALTA, da(r, "matematica").nivel());
        assertEquals(NivelPrioridade.BAIXA, da(r, "redacao").nivel());
    }

    @Test
    void funcionaSemNenhumDado() {
        List<PrioridadeArea> r = calculadora.calcular(
                new EntradaPrioridades(Map.of(), Map.of(), null, null, Map.of(), null));

        assertEquals(5, r.size());
        r.forEach(p -> {
            assertEquals(3.0, p.score());
            assertFalse(p.motivos().isEmpty());
        });
    }

    @Test
    void resultadoVemOrdenadoDoMaiorParaOMenorScore() {
        List<PrioridadeArea> r = calculadora.calcular(simples(Nivel.INTERMEDIARIO, 120));

        for (int i = 1; i < r.size(); i++) {
            assertTrue(r.get(i - 1).score() >= r.get(i).score());
        }
    }
}
