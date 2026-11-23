package com.nivora.nivora.personalizacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaCorteRepository extends JpaRepository<NotaCorte, Long> {

    List<NotaCorte> findByCursoIdAndInstituicaoIdOrderByAnoDesc(Long cursoId, Long instituicaoId);
}
