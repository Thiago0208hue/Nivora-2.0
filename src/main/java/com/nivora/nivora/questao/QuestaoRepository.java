package com.nivora.nivora.questao;

import com.nivora.nivora.curso.AreaEnem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    /** IDs das questões ativas que atendem aos filtros (filtro nulo = qualquer valor). */
    @Query("""
            select q.id from Questao q
            where q.ativa = true
              and (:area is null or q.area = :area)
              and (:disciplina is null or q.disciplina = :disciplina)
              and (:assunto is null or q.assunto = :assunto)
              and (:ano is null or q.ano = :ano)
              and (:dificuldade is null or q.dificuldade = :dificuldade)
            """)
    List<Long> idsAtivos(@Param("area") AreaEnem area, @Param("disciplina") String disciplina,
                         @Param("assunto") String assunto, @Param("ano") Integer ano,
                         @Param("dificuldade") Dificuldade dificuldade);

    /** Combinações existentes de área, disciplina, assunto e ano, com a quantidade de questões. */
    @Query("""
            select new com.nivora.nivora.questao.CatalogoItem(q.area, q.disciplina, q.assunto, q.ano, count(q))
            from Questao q where q.ativa = true
            group by q.area, q.disciplina, q.assunto, q.ano
            order by q.area, q.disciplina, q.assunto
            """)
    List<CatalogoItem> catalogo();
}
