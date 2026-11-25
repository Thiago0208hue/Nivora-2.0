package com.nivora.nivora.questao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimuladoRepository extends JpaRepository<Simulado, Long> {

    Optional<Simulado> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Simulado> findTop20ByUsuarioIdOrderByIdDesc(Long usuarioId);
}
