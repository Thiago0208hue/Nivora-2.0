package com.nivora.nivora.personalizacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PesoInstituicaoRepository extends JpaRepository<PesoInstituicao, Long> {

    List<PesoInstituicao> findByCursoIdAndInstituicaoId(Long cursoId, Long instituicaoId);
}
