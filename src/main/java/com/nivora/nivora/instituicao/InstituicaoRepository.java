package com.nivora.nivora.instituicao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {

    Optional<Instituicao> findBySigla(String sigla);
}
