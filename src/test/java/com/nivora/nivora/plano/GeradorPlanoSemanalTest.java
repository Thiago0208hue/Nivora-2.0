package com.nivora.nivora.plano;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.personalizacao.CalculadoraPrioridades;
import com.nivora.nivora.personalizacao.DesempenhoArea;
import com.nivora.nivora.personalizacao.EntradaPrioridades;
import com.nivora.nivora.personalizacao.NivelPrioridade;
import com.nivora.nivora.personalizacao.OrigemPeso;
import com.nivora.nivora.personalizacao.PesoArea;
import com.nivora.nivora.personalizacao.PrioridadeArea;
import com.nivora.nivora.usuario.Nivel;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GeradorPlanoSemanalTest {

    private final CalculadoraPrioridades calculadora = new CalculadoraPrioridades();
    private final GeradorPlanoSemanal gerador = new GeradorPlanoSemanal();

    /** Pesos estimados dos cursos no protótipo: matemática, natureza, redação, linguagens, humanas. */
    private static final Map<String, int[]> CURSOS = Map.of(
            "direito", new int[] {2, 2, 5, 5, 5},
            "medicina", new int[] {5, 5, 4, 3, 2},
            "computacao", new int[] {5, 4, 4, 3, 2});

    private List<PrioridadeArea> prioridades(String curso, Nivel nivel, int minutos,
                                             Map<AreaEnem, DesempenhoArea> desempenho) {
        int[] p = CURSOS.get(curso);
        Map<AreaEnem, PesoArea> pesos = new EnumMap<>(AreaEnem.class);
        AreaEnem[] areas = {AreaEnem.MATEMATICA, AreaEnem.NATUREZA, AreaEnem.REDACAO,
                AreaEnem.LINGUAGENS, AreaEnem.HUMANAS};
        for (int i = 0; i < areas.length; i++) {
            pesos.put(areas[i], new PesoArea(p[i], OrigemPeso.CURSO_ESTIMADO, "estimativa de teste"));
        }
        return calculadora.calcular(new EntradaPrioridades(pesos, Map.of(), nivel, minutos, desempenho, null));
    }

    private PlanoSemanal plano(String curso, int minutos) {
        return gerador.gerar(prioridades(curso, Nivel.INTERMEDIARIO, minutos, Map.of()), minutos, Map.of());
    }

    private Map<String, Integer> blocosPorArea(PlanoSemanal plano) {
        Map<String, Integer> contagem = new HashMap<>();
        plano.dias().forEach(d -> d.blocos().stream()
                .filter(b -> b.tipo() == TipoBloco.ESTUDO)
                .forEach(b -> contagem.merge(b.area(), 1, Integer::sum)));
        return contagem;
    }

    /** As regras que valem para qualquer perfil. */
    private void verificarRegras(PlanoSemanal plano, List<PrioridadeArea> prioridades, int minutos) {
        assertEquals(6, plano.dias().size());
        for (DiaPlano dia : plano.dias()) {
            List<BlocoPlano> estudo = dia.blocos().stream().filter(b -> b.tipo() == TipoBloco.ESTUDO).toList();
            assertEquals(2, estudo.size(), dia.dia());
            Set<String> areasDoDia = new HashSet<>();
            estudo.forEach(b -> areasDoDia.add(b.area()));
            assertEquals(2, areasDoDia.size(), "área repetida em " + dia.dia());
            assertEquals(minutos, dia.blocos().stream().mapToInt(BlocoPlano::minutos).sum(), "soma de " + dia.dia());
            dia.blocos().forEach(b -> assertTrue(b.minutos() > 0));
            boolean temRevisao = dia.blocos().stream().anyMatch(b -> b.tipo() == TipoBloco.REVISAO);
            assertEquals(minutos >= 60, temRevisao);
        }
        Map<String, Integer> contagem = blocosPorArea(plano);
        for (PrioridadeArea p : prioridades) {
            int vezes = contagem.getOrDefault(p.area(), 0);
            assertTrue(vezes >= 1, p.area() + " sumiu da semana");
            if (p.nivel() == NivelPrioridade.ALTA) {
                assertTrue(vezes >= 2, p.area() + " é prioridade alta e apareceu só " + vezes + " vez");
            }
        }
        assertEquals(minutos * 6 - plano.dias().stream().flatMap(d -> d.blocos().stream())
                .filter(b -> b.tipo() == TipoBloco.REVISAO).mapToInt(BlocoPlano::minutos).sum(),
                plano.minutosPorArea().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void direitoNaoRepeteMateriaNoDiaENaoExcluiMatematicaENatureza() {
        PlanoSemanal plano = plano("direito", 120);
        verificarRegras(plano, prioridades("direito", Nivel.INTERMEDIARIO, 120, Map.of()), 120);

        Map<String, Integer> contagem = blocosPorArea(plano);
        assertTrue(contagem.get("matematica") >= 1);
        assertTrue(contagem.get("natureza") >= 1);
        assertTrue(contagem.get("redacao") > contagem.get("matematica"));
    }

    @Test
    void medicinaDaMaisBlocosParaMatematicaENatureza() {
        PlanoSemanal plano = plano("medicina", 120);
        verificarRegras(plano, prioridades("medicina", Nivel.INTERMEDIARIO, 120, Map.of()), 120);

        Map<String, Integer> contagem = blocosPorArea(plano);
        assertTrue(contagem.get("matematica") > contagem.get("humanas"));
        assertTrue(contagem.get("natureza") > contagem.get("humanas"));
    }

    @Test
    void computacaoRespeitaAsRegras() {
        PlanoSemanal plano = plano("computacao", 120);
        verificarRegras(plano, prioridades("computacao", Nivel.INTERMEDIARIO, 120, Map.of()), 120);

        assertTrue(blocosPorArea(plano).get("matematica") >= 2);
    }

    @Test
    void somaDosBlocosEhSempreOTempoDisponivel() {
        for (String curso : CURSOS.keySet()) {
            for (int minutos : new int[] {15, 30, 45, 55, 60, 65, 90, 120, 180, 240, 300, 720}) {
                List<PrioridadeArea> prioridades = prioridades(curso, Nivel.INTERMEDIARIO, minutos, Map.of());
                verificarRegras(gerador.gerar(prioridades, minutos, Map.of()), prioridades, minutos);
            }
        }
    }

    @Test
    void todosOsNiveisEPrioridadesAltasGeramPlanosValidos() {
        for (Nivel nivel : Nivel.values()) {
            for (String curso : CURSOS.keySet()) {
                List<PrioridadeArea> prioridades = prioridades(curso, nivel, 120, Map.of());
                verificarRegras(gerador.gerar(prioridades, 120, Map.of()), prioridades, 120);
            }
        }
    }

    @Test
    void desempenhoBaixoAumentaOsBlocosDaAreaFraca() {
        int antes = blocosPorArea(plano("direito", 120)).get("matematica");

        List<PrioridadeArea> comDesempenho = prioridades("direito", Nivel.INTERMEDIARIO, 120,
                Map.of(AreaEnem.MATEMATICA, new DesempenhoArea(10, 3)));
        PlanoSemanal depois = gerador.gerar(comDesempenho, 120, Map.of());
        verificarRegras(depois, comDesempenho, 120);

        assertTrue(blocosPorArea(depois).get("matematica") > antes);
        assertTrue(depois.minutosPorArea().get("matematica") > plano("direito", 120).minutosPorArea().get("matematica"));
    }

    @Test
    void dificuldadesViramFocoNosBlocosDaArea() {
        List<PrioridadeArea> prioridades = prioridades("direito", Nivel.INTERMEDIARIO, 120, Map.of());
        PlanoSemanal plano = gerador.gerar(prioridades, 120, Map.of("matematica", List.of("Matemática")));

        plano.dias().stream().flatMap(d -> d.blocos().stream())
                .filter(b -> "matematica".equals(b.area()))
                .forEach(b -> assertEquals(List.of("Matemática"), b.foco()));
        plano.dias().stream().flatMap(d -> d.blocos().stream())
                .filter(b -> "redacao".equals(b.area()))
                .forEach(b -> assertTrue(b.foco().isEmpty()));
    }

    @Test
    void blocoPrincipalVemPrimeiroEEhOMaior() {
        PlanoSemanal plano = plano("medicina", 120);

        for (DiaPlano dia : plano.dias()) {
            assertTrue(dia.blocos().get(0).minutos() > dia.blocos().get(1).minutos());
            assertEquals(TipoBloco.REVISAO, dia.blocos().get(2).tipo());
        }
    }

    @Test
    void mesmoPerfilGeraSempreOMesmoPlano() {
        assertEquals(plano("medicina", 120), plano("medicina", 120));
    }

    @Test
    void perfisDiferentesGeramPlanosDiferentes() {
        assertNotEquals(plano("direito", 120).minutosPorArea(), plano("medicina", 120).minutosPorArea());
    }
}
