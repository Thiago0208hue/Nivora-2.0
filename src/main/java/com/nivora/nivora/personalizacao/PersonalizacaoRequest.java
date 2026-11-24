package com.nivora.nivora.personalizacao;

import jakarta.validation.Valid;
import java.util.Map;

/** Desempenho por área (chave em minúsculas). Opcional; até a Etapa 8 vem do navegador. */
public record PersonalizacaoRequest(Map<String, @Valid DesempenhoArea> desempenho) {
}
