package com.nivora.nivora.instituicao;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstituicaoService {

    private final InstituicaoRepository instituicoes;

    public InstituicaoService(InstituicaoRepository instituicoes) {
        this.instituicoes = instituicoes;
    }

    @Transactional(readOnly = true)
    public List<InstituicaoResponse> listar() {
        return instituicoes.findAll(Sort.by("id")).stream().map(InstituicaoResponse::de).toList();
    }
}
