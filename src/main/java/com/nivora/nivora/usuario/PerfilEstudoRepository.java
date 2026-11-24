package com.nivora.nivora.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilEstudoRepository extends JpaRepository<PerfilEstudo, Long> {

    Optional<PerfilEstudo> findByUsuarioId(Long usuarioId);

    Optional<PerfilEstudo> findByUsuarioEmail(String email);
}
