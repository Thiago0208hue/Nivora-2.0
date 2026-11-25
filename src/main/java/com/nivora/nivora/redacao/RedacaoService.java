package com.nivora.nivora.redacao;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.redacao.RedacaoDtos.*;
import com.nivora.nivora.usuario.Usuario;
import com.nivora.nivora.usuario.UsuarioRepository;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedacaoService {

    /** Níveis de nota por competência na correção do ENEM. */
    static final Set<Integer> NIVEIS = Set.of(0, 40, 80, 120, 160, 200);

    private final RedacaoRepository redacoes;
    private final TemaRedacaoRepository temas;
    private final UsuarioRepository usuarios;
    private final AnaliseEstrutural analise = new AnaliseEstrutural();

    public RedacaoService(RedacaoRepository redacoes, TemaRedacaoRepository temas, UsuarioRepository usuarios) {
        this.redacoes = redacoes;
        this.temas = temas;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<TemaResponse> temas() {
        return temas.findByAtivoTrueOrderById().stream()
                .map(t -> new TemaResponse(t.getId(), t.getTitulo(), t.getOrigem().getRotulo(), t.getAno(), t.getFonte()))
                .toList();
    }

    @Transactional
    public RedacaoResponse salvar(String email, RedacaoRequest pedido) {
        Usuario usuario = usuario(email);
        int[] notas = validarNotas(pedido.competencias());
        AnaliseEstrutural.Resultado r = analise.analisar(pedido.texto());
        if (r.palavras() == 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Escreva o texto da redação.");
        }
        Redacao redacao = redacoes.save(new Redacao(usuario, pedido.tema().trim(), pedido.texto().trim(),
                r.palavras(), r.paragrafos(), notas));
        return paraResponse(redacao, r.observacoes());
    }

    @Transactional(readOnly = true)
    public List<RedacaoResumo> historico(String email) {
        Usuario usuario = usuario(email);
        return redacoes.findTop20ByUsuarioIdOrderByIdDesc(usuario.getId()).stream()
                .map(r -> new RedacaoResumo(r.getId(), r.getTema(), r.getPalavras(), r.total(), r.getCriadaEm()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RedacaoResponse buscar(String email, Long id) {
        Usuario usuario = usuario(email);
        Redacao r = redacoes.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Redação não encontrada."));
        return paraResponse(r, analise.analisar(r.getTexto()).observacoes());
    }

    /** Devolve null se não houve autoavaliação; exige as cinco notas, todas em níveis válidos. */
    static int[] validarNotas(List<Integer> competencias) {
        if (competencias == null || competencias.isEmpty()) return null;
        if (competencias.size() != 5) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Informe as cinco competências ou nenhuma.");
        }
        int[] notas = new int[5];
        for (int i = 0; i < 5; i++) {
            Integer n = competencias.get(i);
            if (n == null || !NIVEIS.contains(n)) {
                throw new NegocioException(HttpStatus.BAD_REQUEST,
                        "Cada competência deve valer 0, 40, 80, 120, 160 ou 200.");
            }
            notas[i] = n;
        }
        return notas;
    }

    private RedacaoResponse paraResponse(Redacao r, List<String> observacoes) {
        List<Integer> notas = r.temAutoavaliacao()
                ? List.of(r.getC1(), r.getC2(), r.getC3(), r.getC4(), r.getC5()) : List.of();
        return new RedacaoResponse(r.getId(), r.getTema(), r.getTexto(), r.getPalavras(), r.getParagrafos(),
                notas, r.total(), observacoes, r.getCriadaEm());
    }

    private Usuario usuario(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
    }
}
