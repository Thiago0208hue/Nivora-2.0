package com.nivora.nivora.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.curso.Curso;
import com.nivora.nivora.curso.CursoRepository;
import com.nivora.nivora.instituicao.Instituicao;
import com.nivora.nivora.instituicao.InstituicaoRepository;
import com.nivora.nivora.personalizacao.NotaCorte;
import com.nivora.nivora.personalizacao.NotaCorteRepository;
import com.nivora.nivora.usuario.Nivel;
import com.nivora.nivora.usuario.PerfilEstudo;
import com.nivora.nivora.usuario.PerfilEstudoRepository;
import com.nivora.nivora.usuario.Usuario;
import com.nivora.nivora.usuario.UsuarioRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PersistenciaTest {

    @Autowired UsuarioRepository usuarios;
    @Autowired PerfilEstudoRepository perfis;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired NotaCorteRepository notasCorte;

    @Test
    void cargaInicialCriaCursosEInstituicoes() {
        assertEquals(14, cursos.count());
        assertEquals(10, instituicoes.count());

        Curso direito = cursos.findByCodigo("direito").orElseThrow();
        assertEquals(5, direito.pesoDaArea(AreaEnem.REDACAO));
        assertEquals(2, direito.pesoDaArea(AreaEnem.MATEMATICA));
        assertTrue(direito.isPesosEstimados());
    }

    @Test
    void salvaPerfilComCursoInstituicaoEDificuldades() {
        Usuario usuario = usuarios.saveAndFlush(new Usuario("Ana", "ana@email.com", "hash-qualquer"));
        PerfilEstudo perfil = new PerfilEstudo(usuario);
        perfil.setCurso(cursos.findByCodigo("medicina").orElseThrow());
        perfil.setInstituicao(instituicoes.findBySigla("ufrn").orElseThrow());
        perfil.setNivel(Nivel.INICIANTE);
        perfil.setMinutosDiarios(120);
        perfil.getDificuldades().add("Matemática");
        perfil.getDificuldades().add("Física");
        perfis.saveAndFlush(perfil);

        PerfilEstudo lido = perfis.findByUsuarioId(usuario.getId()).orElseThrow();
        assertEquals(Nivel.INICIANTE, lido.getNivel());
        assertEquals(2, lido.getDificuldades().size());
        assertEquals("medicina", lido.getCurso().getCodigo());
    }

    @Test
    void emailDuplicadoEhRejeitado() {
        usuarios.saveAndFlush(new Usuario("Ana", "repetido@email.com", "hash"));
        assertThrows(DataIntegrityViolationException.class,
                () -> usuarios.saveAndFlush(new Usuario("Outra", "repetido@email.com", "hash")));
    }

    @Test
    void notaDeCorteGuardaAnoEFonte() {
        Curso curso = cursos.findByCodigo("direito").orElseThrow();
        Instituicao ufrn = instituicoes.findBySigla("ufrn").orElseThrow();
        notasCorte.saveAndFlush(new NotaCorte(curso, ufrn, "Ampla concorrência", 2025,
                new BigDecimal("700.00"), "Dado de teste, não oficial", true));

        NotaCorte lida = notasCorte.findByCursoIdAndInstituicaoIdOrderByAnoDesc(curso.getId(), ufrn.getId()).get(0);
        assertEquals(2025, lida.getAno());
        assertTrue(lida.isEstimado());
    }
}
