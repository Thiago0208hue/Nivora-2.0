package com.nivora.nivora.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 100, message = "Nome muito longo.") String nome,
        @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "E-mail muito longo.") String email,
        // BCrypt só usa os 72 primeiros bytes da senha
        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres.") String senha) {
}
