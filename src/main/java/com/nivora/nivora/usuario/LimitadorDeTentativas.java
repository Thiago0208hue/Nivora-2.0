package com.nivora.nivora.usuario;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Limita tentativas de login erradas: no máximo MAXIMO falhas por chave dentro da JANELA.
 * Fica na memória do servidor (some ao reiniciar e não é compartilhada entre várias instâncias;
 * para produção com mais de uma instância seria preciso um armazenamento comum).
 */
@Component
public class LimitadorDeTentativas {

    public static final int MAXIMO = 5;
    public static final long JANELA_SEGUNDOS = 15 * 60;

    private final Map<String, Deque<Instant>> falhas = new ConcurrentHashMap<>();
    private final Clock relogio;

    public LimitadorDeTentativas() {
        this(Clock.systemUTC());
    }

    LimitadorDeTentativas(Clock relogio) {
        this.relogio = relogio;
    }

    public boolean bloqueado(String chave) {
        Deque<Instant> lista = falhas.get(chave);
        if (lista == null) return false;
        synchronized (lista) {
            descartarAntigas(lista);
            if (lista.isEmpty()) falhas.remove(chave, lista);
            return lista.size() >= MAXIMO;
        }
    }

    public void registrarFalha(String chave) {
        Deque<Instant> lista = falhas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (lista) {
            descartarAntigas(lista);
            lista.addLast(relogio.instant());
        }
    }

    public void limpar(String chave) {
        falhas.remove(chave);
    }

    private void descartarAntigas(Deque<Instant> lista) {
        Instant limite = relogio.instant().minusSeconds(JANELA_SEGUNDOS);
        while (!lista.isEmpty() && lista.peekFirst().isBefore(limite)) {
            lista.removeFirst();
        }
    }
}
