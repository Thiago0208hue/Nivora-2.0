package com.nivora.nivora.plano;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.CursoRepository;
import com.nivora.nivora.instituicao.InstituicaoRepository;
import com.nivora.nivora.personalizacao.DesempenhoArea;
import com.nivora.nivora.usuario.Nivel;
import com.nivora.nivora.usuario.PerfilEstudo;
import com.nivora.nivora.usuario.PerfilEstudoRepository;
import com.nivora.nivora.usuario.Usuario;
import com.nivora.nivora.usuario.UsuarioRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PlanoServiceTest {

    private static final String EMAIL = "ana@email.com";

    @Autowired PlanoService service;
    @Autowired UsuarioRepository usuarios;
    @Autowired PerfilEstudoRepository perfis;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;

    private PerfilEstudo criarPerfil(String curso, int minutos) {
        Usuario usuario = usuarios.saveAndFlush(new Usuario("Ana", EMAIL, "hash"));
        PerfilEstudo perfil = new PerfilEstudo(usuario);
        if (curso != null) perfil.setCurso(cursos.findByCodigo(curso).orElseThrow());
        perfil.setInstituicao(instituicoes.findBySigla("ufrn").orElseThrow());
        perfil.setNivel(Nivel.INTERMEDIARIO);
        perfil.setMinutosDiarios(minutos);
        return perfis.saveAndFlush(perfil);
    }

    private long blocos(PlanoResponse plano, String area) {
        return plano.dias().stream().flatMap(d -> d.blocos().stream()).filter(b -> area.equals(b.area())).count();
    }

    @Test
    void geraSemanaCompletaComTempoDoPerfil() {
        criarPerfil("direito", 90);

        PlanoResponse plano = service.gerarSemana(EMAIL, null);

        assertEquals(90, plano.minutosPorDia());
        assertEquals(6, plano.dias().size());
        plano.dias().forEach(d -> assertEquals(90, d.blocos().stream().mapToInt(BlocoPlano::minutos).sum()));
        assertEquals(5, plano.minutosPorArea().size());
        assertTrue(blocos(plano, "matematica") >= 1);
        assertTrue(blocos(plano, "natureza") >= 1);
        assertTrue(plano.avisos().stream().anyMatch(a -> a.contains("estimativa")));
    }

    @Test
    void dificuldadesDoPerfilAparecemComoFoco() {
        PerfilEstudo perfil = criarPerfil("direito", 120);
        perfil.getDificuldades().addAll(List.of("Matemática", "Física"));
        perfis.saveAndFlush(perfil);

        PlanoResponse plano = service.gerarSemana(EMAIL, null);

        plano.dias().stream().flatMap(d -> d.blocos().stream())
                .filter(b -> "natureza".equals(b.area()))
                .forEach(b -> assertEquals(List.of("Física"), b.foco()));
        // peso 2 + dificuldade 2 = 4: Matemática e Natureza ganham mais de um bloco
        assertTrue(blocos(plano, "matematica") >= 2);
    }

    @Test
    void planoSeAdaptaAoDesempenho() {
        criarPerfil("direito", 120);
        long antes = blocos(service.gerarSemana(EMAIL, null), "matematica");

        long depois = blocos(service.gerarSemana(EMAIL, Map.of("matematica", new DesempenhoArea(10, 2))), "matematica");

        assertTrue(depois > antes);
    }

    @Test
    void semOnboardingNaoGeraPlano() {
        criarPerfil(null, 120);

        NegocioException erro = assertThrows(NegocioException.class, () -> service.gerarSemana(EMAIL, null));

        assertEquals(409, erro.getStatus().value());
    }
}
