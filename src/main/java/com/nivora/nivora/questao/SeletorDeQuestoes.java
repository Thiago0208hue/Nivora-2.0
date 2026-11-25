package com.nivora.nivora.questao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Escolhe quais questões o estudante vai receber. Controle de repetição: primeiro as que ele
 * nunca respondeu (em ordem aleatória); depois as que respondeu há mais tempo. Não acessa banco.
 */
public class SeletorDeQuestoes {

    /** Quantas vezes e quando o estudante respondeu a questão. */
    public record Uso(int vezes, LocalDateTime ultima) {
    }

    private final Random random;

    public SeletorDeQuestoes(Random random) {
        this.random = random;
    }

    public List<Long> selecionar(List<Long> ids, Map<Long, Uso> usos, int quantidade) {
        List<Long> nuncaRespondidas = new ArrayList<>();
        List<Long> jaRespondidas = new ArrayList<>();
        for (Long id : ids) {
            (usos.containsKey(id) ? jaRespondidas : nuncaRespondidas).add(id);
        }
        embaralhar(nuncaRespondidas);
        // Embaralha antes de ordenar para que empates (mesma data) saiam em ordem aleatória
        embaralhar(jaRespondidas);
        jaRespondidas.sort(Comparator
                .<Long, LocalDateTime>comparing(id -> usos.get(id).ultima())
                .thenComparingInt(id -> usos.get(id).vezes()));

        List<Long> resultado = new ArrayList<>(nuncaRespondidas);
        resultado.addAll(jaRespondidas);
        return new ArrayList<>(resultado.subList(0, Math.min(quantidade, resultado.size())));
    }

    /** Embaralhamento de Fisher-Yates: percorre de trás para frente trocando com uma posição sorteada. */
    public <T> void embaralhar(List<T> lista) {
        for (int i = lista.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T auxiliar = lista.get(i);
            lista.set(i, lista.get(j));
            lista.set(j, auxiliar);
        }
    }
}
