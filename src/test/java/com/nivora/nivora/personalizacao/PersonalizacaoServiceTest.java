package com.nivora.nivora.personalizacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.curso.Curso;
import com.nivora.nivora.curso.CursoRepository;
import com.nivora.nivora.instituicao.Instituicao;
import com.nivora.nivora.instituicao.InstituicaoRepository;
import com.nivora.nivora.usuario.Nivel;
import com.nivora.nivora.usuario.PerfilEstudo;
import com.nivora.nivora.usuario.PerfilEstudoRepository;
import com.nivora.nivora.usuario.Usuario;
import com.nivora.nivora.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PersonalizacaoServiceTest {

    private static final String EMAIL = "ana@email.com";

    @Autowired PersonalizacaoService service;
    @Autowired UsuarioRepository usuarios;
    @Autowired PerfilEstudoRepository perfis;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired PesoInstituicaoRepository pesosInstituicao;
    @Autowired NotaCorteRepository notasCorte;

    private PerfilEstudo criarPerfil(String curso, String instituicao, String modalidade) {
        Usuario usuario = usuarios.saveAndFlush(new Usuario("Ana", EMAIL, "hash"));
        PerfilEstudo perfil = new PerfilEstudo(usuario);
        if (curso != null) perfil.setCurso(cursos.findByCodigo(curso).orElseThrow());
        if (instituicao != null) perfil.setInstituicao(instituicoes.findBySigla(instituicao).orElseThrow());
        perfil.setModalidadeIngresso(modalidade);
        perfil.setNivel(Nivel.INTERMEDIARIO);
        perfil.setMinutosDiarios(120);
        return perfis.saveAndFlush(perfil);
    }

    private PrioridadeArea da(RecomendacaoResponse r, String area) {
        return r.prioridades().stream().filter(p -> p.area().equals(area)).findFirst().orElseThrow();
    }

    @Test
    void direitoPriorizaRedacaoLinguagensEHumanas() {
        criarPerfil("direito", "ufrn", "Ampla concorrência");

        RecomendacaoResponse r = service.recomendar(EMAIL, null);

        assertEquals(5, r.prioridades().size());
        assertEquals(NivelPrioridade.ALTA, da(r, "redacao").nivel());
        assertEquals(NivelPrioridade.ALTA, da(r, "humanas").nivel());
        assertEquals(NivelPrioridade.BAIXA, da(r, "matematica").nivel());
        assertEquals(OrigemPeso.CURSO_ESTIMADO, da(r, "redacao").origemPeso());
        assertTrue(r.avisos().stream().anyMatch(a -> a.contains("estimativa do Nivora")));
    }

    @Test
    void pesoCadastradoDaInstituicaoMudaARecomendacao() {
        PerfilEstudo perfil = criarPerfil("medicina", "ufrn", "Ampla concorrência");
        Curso medicina = perfil.getCurso();
        Instituicao ufrn = perfil.getInstituicao();

        // Sem dados da instituição, Medicina pesa 5 em Natureza (estimativa do curso)
        assertEquals(NivelPrioridade.ALTA, da(service.recomendar(EMAIL, null), "natureza").nivel());

        pesosInstituicao.saveAndFlush(new PesoInstituicao(medicina, ufrn, AreaEnem.NATUREZA,
                new BigDecimal("2.00"), 2025, "Dado de teste, não oficial", true));
        RecomendacaoResponse r = service.recomendar(EMAIL, null);

        PrioridadeArea natureza = da(r, "natureza");
        assertEquals(2.0, natureza.peso());
        assertEquals(NivelPrioridade.BAIXA, natureza.nivel());
        assertEquals(OrigemPeso.INSTITUICAO_ESTIMADO, natureza.origemPeso());
        assertTrue(natureza.motivos().get(0).contains("Dado de teste"));
        assertEquals(OrigemPeso.CURSO_ESTIMADO, da(r, "matematica").origemPeso());
    }

    @Test
    void usaOPesoDoAnoMaisRecente() {
        PerfilEstudo perfil = criarPerfil("medicina", "ufrn", "Ampla concorrência");
        pesosInstituicao.save(new PesoInstituicao(perfil.getCurso(), perfil.getInstituicao(), AreaEnem.NATUREZA,
                new BigDecimal("1.00"), 2023, "Teste 2023", true));
        pesosInstituicao.saveAndFlush(new PesoInstituicao(perfil.getCurso(), perfil.getInstituicao(), AreaEnem.NATUREZA,
                new BigDecimal("4.00"), 2025, "Teste 2025", true));

        assertEquals(4.0, da(service.recomendar(EMAIL, null), "natureza").peso());
    }

    @Test
    void notaDeCorteDaModalidadeEscolhidaEhUsadaComAnoEFonte() {
        PerfilEstudo perfil = criarPerfil("direito", "ufrn", "Ampla concorrência");
        notasCorte.save(new NotaCorte(perfil.getCurso(), perfil.getInstituicao(), "Ampla concorrência", 2024,
                new BigDecimal("650.00"), "Teste 2024", true));
        notasCorte.saveAndFlush(new NotaCorte(perfil.getCurso(), perfil.getInstituicao(), "Ampla concorrência", 2025,
                new BigDecimal("720.00"), "Teste 2025", true));

        RecomendacaoResponse r = service.recomendar(EMAIL, null);

        assertNotNull(r.notaCorte());
        assertEquals(2025, r.notaCorte().ano());
        assertEquals("Teste 2025", r.notaCorte().fonte());
        // Sem nota desejada, a nota de corte (720) vira a nota-alvo e reforça áreas de peso alto
        assertEquals(5.5, da(r, "redacao").score());
        assertTrue(r.avisos().stream().anyMatch(a -> a.contains("Nota de corte de referência")));
    }

    @Test
    void notaDesejadaAbaixoDoCorteGeraAviso() {
        PerfilEstudo perfil = criarPerfil("direito", "ufrn", "Ampla concorrência");
        perfil.setNotaPretendida(new BigDecimal("600"));
        perfis.saveAndFlush(perfil);
        notasCorte.saveAndFlush(new NotaCorte(perfil.getCurso(), perfil.getInstituicao(), "Ampla concorrência", 2025,
                new BigDecimal("720.00"), "Teste", true));

        RecomendacaoResponse r = service.recomendar(EMAIL, null);

        assertTrue(r.avisos().stream().anyMatch(a -> a.contains("abaixo da nota de corte")));
        assertEquals(5.0, da(r, "redacao").score());
    }

    @Test
    void semNotaDeCorteParaAModalidadeNaoInventaNada() {
        PerfilEstudo perfil = criarPerfil("direito", "ufrn", "Escola pública");
        notasCorte.saveAndFlush(new NotaCorte(perfil.getCurso(), perfil.getInstituicao(), "Ampla concorrência", 2025,
                new BigDecimal("720.00"), "Teste", true));

        RecomendacaoResponse r = service.recomendar(EMAIL, null);

        assertNull(r.notaCorte());
        assertTrue(r.avisos().stream().anyMatch(a -> a.contains("Não há nota de corte")));
    }

    @Test
    void funcionaSemInstituicaoEscolhida() {
        criarPerfil("direito", null, null);

        RecomendacaoResponse r = service.recomendar(EMAIL, null);

        assertEquals(5, r.prioridades().size());
        assertNull(r.notaCorte());
        assertTrue(r.avisos().stream().anyMatch(a -> a.contains("Nenhuma instituição")));
    }

    @Test
    void dificuldadesEDesempenhoEntramNoCalculo() {
        PerfilEstudo perfil = criarPerfil("direito", "ufrn", "Ampla concorrência");
        perfil.getDificuldades().addAll(List.of("Matemática", "Física"));
        perfis.saveAndFlush(perfil);

        RecomendacaoResponse r = service.recomendar(EMAIL,
                Map.of("matematica", new DesempenhoArea(10, 3)));

        // peso 2 + dificuldade 2 + desempenho baixo 2
        assertEquals(6.0, da(r, "matematica").score());
        assertEquals(NivelPrioridade.ALTA, da(r, "matematica").nivel());
        // peso 2 + dificuldade 2 (Física pertence a Natureza)
        assertEquals(4.0, da(r, "natureza").score());
    }

    @Test
    void perfilSemCursoExigeOnboarding() {
        criarPerfil(null, null, null);

        NegocioException erro = assertThrows(NegocioException.class, () -> service.recomendar(EMAIL, null));

        assertEquals(409, erro.getStatus().value());
    }

    @Test
    void desempenhoInvalidoEhRejeitado() {
        criarPerfil("direito", "ufrn", "Ampla concorrência");

        assertThrows(NegocioException.class,
                () -> service.recomendar(EMAIL, Map.of("matematica", new DesempenhoArea(5, 9))));
        assertThrows(NegocioException.class,
                () -> service.recomendar(EMAIL, Map.of("geografia", new DesempenhoArea(5, 1))));
    }
}
