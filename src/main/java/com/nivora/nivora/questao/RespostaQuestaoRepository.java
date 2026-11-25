package com.nivora.nivora.questao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RespostaQuestaoRepository extends JpaRepository<RespostaQuestao, Long> {

    interface UsoProjecao {
        Long getQuestaoId();
        Long getVezes();
        LocalDateTime getUltima();
    }

    interface AreaAgregado {
        com.nivora.nivora.curso.AreaEnem getArea();
        Long getRespondidas();
        Long getAcertos();
    }

    interface AssuntoAgregado {
        com.nivora.nivora.curso.AreaEnem getArea();
        String getAssunto();
        Long getRespondidas();
        Long getAcertos();
    }

    interface InstanteResposta {
        LocalDateTime getRespondidaEm();
        boolean getCorreta();
    }

    @Query("""
            select r.questao.area as area, count(r) as respondidas,
                   sum(case when r.correta = true then 1 else 0 end) as acertos
            from RespostaQuestao r where r.usuario.id = :usuarioId group by r.questao.area
            """)
    List<AreaAgregado> agregadoPorArea(@Param("usuarioId") Long usuarioId);

    @Query("""
            select r.questao.area as area, r.questao.assunto as assunto, count(r) as respondidas,
                   sum(case when r.correta = true then 1 else 0 end) as acertos
            from RespostaQuestao r where r.usuario.id = :usuarioId
            group by r.questao.area, r.questao.assunto
            """)
    List<AssuntoAgregado> agregadoPorAssunto(@Param("usuarioId") Long usuarioId);

    List<InstanteResposta> findByUsuarioIdAndRespondidaEmAfter(Long usuarioId, LocalDateTime desde);

    /** Quantas vezes e quando o estudante respondeu cada questão (só as que já respondeu). */
    @Query("""
            select r.questao.id as questaoId, count(r) as vezes, max(r.respondidaEm) as ultima
            from RespostaQuestao r
            where r.usuario.id = :usuarioId and r.questao.id in :ids
            group by r.questao.id
            """)
    List<UsoProjecao> usoDasQuestoes(@Param("usuarioId") Long usuarioId, @Param("ids") Collection<Long> ids);

    /** Questões cuja resposta mais recente do estudante foi errada, da mais nova para a mais antiga. */
    @Query("""
            select r.questao.id from RespostaQuestao r
            where r.usuario.id = :usuarioId and r.correta = false
              and r.id = (select max(r2.id) from RespostaQuestao r2
                          where r2.usuario.id = :usuarioId and r2.questao.id = r.questao.id)
            order by r.id desc
            """)
    List<Long> idsComUltimoErro(@Param("usuarioId") Long usuarioId, Pageable limite);

    /** Quantas questões diferentes o estudante já acertou ao menos uma vez (base do XP, evita repetir a mesma questão). */
    @Query("select count(distinct r.questao.id) from RespostaQuestao r where r.usuario.id = :usuarioId and r.correta = true")
    long questoesDistintasAcertadas(@Param("usuarioId") Long usuarioId);

    long countByCorretaTrue();

    /** Estudantes diferentes que responderam ao menos uma questão desde a data (só a contagem, sem identificar ninguém). */
    @Query("select count(distinct r.usuario.id) from RespostaQuestao r where r.respondidaEm >= :desde")
    long estudantesAtivosDesde(@Param("desde") LocalDateTime desde);

    Optional<RespostaQuestao> findFirstByUsuarioIdAndQuestaoIdOrderByIdDesc(Long usuarioId, Long questaoId);

    List<RespostaQuestao> findByUsuarioIdOrderByIdDesc(Long usuarioId, Pageable limite);

    List<RespostaQuestao> findBySimuladoId(Long simuladoId);

    boolean existsBySimuladoIdAndQuestaoId(Long simuladoId, Long questaoId);
}
