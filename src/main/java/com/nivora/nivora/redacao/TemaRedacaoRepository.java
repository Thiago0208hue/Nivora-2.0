package com.nivora.nivora.redacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemaRedacaoRepository extends JpaRepository<TemaRedacao, Long> {

    List<TemaRedacao> findByAtivoTrueOrderById();
}
