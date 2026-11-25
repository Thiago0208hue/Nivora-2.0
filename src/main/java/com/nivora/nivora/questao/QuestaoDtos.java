package com.nivora.nivora.questao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/** Registros de entrada e saída do módulo de questões. */
public final class QuestaoDtos {

    private QuestaoDtos() {
    }

    public record AlternativaResponse(String letra, String texto) {
    }

    /** Questão SEM gabarito: o gabarito só sai depois de o estudante responder. */
    public record QuestaoResponse(Long id, String area, String disciplina, String assunto, String dificuldade,
                                  Integer ano, String origem, String fonte, String enunciado,
                                  List<AlternativaResponse> alternativas) {
    }

    public record CatalogoResponse(String area, String disciplina, String assunto, Integer ano, long quantidade) {
    }

    public record PraticaRequest(String area, String disciplina, String assunto, Integer ano, String dificuldade,
                                 @Min(1) @Max(50) Integer quantidade) {
    }

    public record RespostaRequest(@NotBlank @Pattern(regexp = "[A-Ea-e]") String letra) {
    }

    public record CorrecaoResponse(Long questaoId, String letraEscolhida, boolean acertou, String letraCorreta,
                                   String resolucao) {
    }

    /** Questão já respondida, com gabarito (usada na revisão de erros). */
    public record RevisaoResponse(QuestaoResponse questao, String letraEscolhida, String letraCorreta,
                                  String resolucao, LocalDateTime respondidaEm) {
    }

    public record SimuladoRequest(@NotNull TipoSimulado tipo, String area, @Min(1) @Max(90) Integer quantidade) {
    }

    public record SimuladoResponse(Long id, String tipo, String area, LocalDateTime criadoEm, boolean finalizado,
                                   List<QuestaoResponse> questoes, List<String> respondidas) {
    }

    public record SimuladoResumo(Long id, String tipo, String area, LocalDateTime criadoEm,
                                 LocalDateTime finalizadoEm, int total) {
    }

    public record ItemResultado(Long questaoId, String assunto, String letraEscolhida, String letraCorreta,
                                boolean acertou, String resolucao) {
    }

    public record ResultadoSimulado(Long id, int total, int acertos, int emBranco, List<ItemResultado> itens) {
    }

    public record AlternativaAdmin(@NotBlank @Pattern(regexp = "[A-E]") String letra,
                                   @NotBlank @Size(max = 1000) String texto, boolean correta) {
    }

    public record QuestaoAdminRequest(@NotBlank String area, @NotBlank @Size(max = 40) String disciplina,
                                      @NotBlank @Size(max = 80) String assunto, String dificuldade,
                                      @Min(1998) @Max(2100) Integer ano, @NotNull OrigemQuestao origem,
                                      @Size(max = 200) String fonte,
                                      @NotBlank @Size(max = 6000) String enunciado,
                                      @Size(max = 3000) String resolucao,
                                      @NotNull @Size(min = 2, max = 5) List<@Valid AlternativaAdmin> alternativas) {
    }
}
