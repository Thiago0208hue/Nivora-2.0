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

    Optional<RespostaQuestao> findFirstByUsuarioIdAndQuestaoIdOrderByIdDesc(Long usuarioId, Long questaoId);

    List<RespostaQuestao> findByUsuarioIdOrderByIdDesc(Long usuarioId, Pageable limite);

    List<RespostaQuestao> findBySimuladoId(Long simuladoId);

    boolean existsBySimuladoIdAndQuestaoId(Long simuladoId, Long questaoId);
}
