package com.nivora.nivora.plano;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.AreaEnem;
import com.nivora.nivora.personalizacao.DesempenhoArea;
import com.nivora.nivora.personalizacao.PersonalizacaoService;
import com.nivora.nivora.personalizacao.RecomendacaoResponse;
import com.nivora.nivora.usuario.PerfilEstudo;
import com.nivora.nivora.usuario.PerfilEstudoRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanoService {

    private static final int MINUTOS_PADRAO = 120;

    private final PersonalizacaoService personalizacao;
    private final PerfilEstudoRepository perfis;
    private final GeradorPlanoSemanal gerador;

    public PlanoService(PersonalizacaoService personalizacao, PerfilEstudoRepository perfis,
                        GeradorPlanoSemanal gerador) {
        this.personalizacao = personalizacao;
        this.perfis = perfis;
        this.gerador = gerador;
    }

    /** Gera o plano na hora, então ele acompanha o desempenho: mudou o desempenho, mudou o plano. */
    @Transactional(readOnly = true)
    public PlanoResponse gerarSemana(String email, Map<String, DesempenhoArea> desempenho) {
        RecomendacaoResponse recomendacao = personalizacao.recomendar(email, desempenho);
        PerfilEstudo perfil = perfis.findByUsuarioEmail(email)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Perfil não encontrado."));

        int minutos = perfil.getMinutosDiarios() != null ? perfil.getMinutosDiarios() : MINUTOS_PADRAO;
        PlanoSemanal plano = gerador.gerar(recomendacao.prioridades(), minutos, agruparFoco(perfil));
        return new PlanoResponse(minutos, plano.dias(), plano.minutosPorArea(), recomendacao.avisos());
    }

    private Map<String, List<String>> agruparFoco(PerfilEstudo perfil) {
        Map<String, List<String>> foco = new HashMap<>();
        perfil.getDificuldades().stream().sorted().forEach(disciplina ->
                AreaEnem.daDisciplina(disciplina).ifPresent(area ->
                        foco.computeIfAbsent(area.name().toLowerCase(), a -> new ArrayList<>()).add(disciplina)));
        return foco;
    }
}
