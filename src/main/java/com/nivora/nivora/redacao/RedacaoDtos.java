package com.nivora.nivora.redacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class RedacaoDtos {

    private RedacaoDtos() {
    }

    public record TemaResponse(Long id, String titulo, String origem, Integer ano, String fonte) {
    }

    /** Autoavaliação opcional: ou vêm as cinco notas, ou nenhuma. */
    public record RedacaoRequest(@NotBlank @Size(max = 300) String tema,
                                 @NotBlank @Size(max = 8000) String texto,
                                 List<Integer> competencias) {
    }

    public record RedacaoResponse(Long id, String tema, String texto, int palavras, int paragrafos,
                                  List<Integer> competencias, Integer total, List<String> observacoes,
                                  LocalDateTime criadaEm) {
    }

    public record RedacaoResumo(Long id, String tema, int palavras, Integer total, LocalDateTime criadaEm) {
    }
}
