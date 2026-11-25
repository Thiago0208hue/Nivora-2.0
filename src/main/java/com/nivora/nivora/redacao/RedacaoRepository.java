package com.nivora.nivora.redacao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedacaoRepository extends JpaRepository<Redacao, Long> {

    List<Redacao> findTop20ByUsuarioIdOrderByIdDesc(Long usuarioId);

    Optional<Redacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    long countByUsuarioId(Long usuarioId);
}
