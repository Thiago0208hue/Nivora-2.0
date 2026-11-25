package com.nivora.nivora.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe o e-mail.") @Size(max = 150) String email,
        @NotBlank(message = "Informe a senha.") @Size(max = 128) String senha) {
}
