package com.nivora.nivora.usuario;

import com.nivora.nivora.config.NegocioException;
import com.nivora.nivora.curso.CursoRepository;
import com.nivora.nivora.instituicao.InstituicaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final PerfilEstudoRepository perfis;
    private final CursoRepository cursos;
    private final InstituicaoRepository instituicoes;

    public PerfilService(PerfilEstudoRepository perfis, CursoRepository cursos, InstituicaoRepository instituicoes) {
        this.perfis = perfis;
        this.cursos = cursos;
        this.instituicoes = instituicoes;
    }

    @Transactional(readOnly = true)
    public PerfilResponse obter(String email) {
        return PerfilResponse.de(buscar(email));
    }

    @Transactional
    public PerfilResponse atualizar(String email, PerfilRequest pedido) {
        PerfilEstudo perfil = buscar(email);

        perfil.setCurso(pedido.cursoCodigo() == null ? null : cursos.findByCodigo(pedido.cursoCodigo())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Curso não encontrado.")));
        perfil.setInstituicao(pedido.instituicaoSigla() == null ? null : instituicoes.findBySigla(pedido.instituicaoSigla())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Instituição não encontrada.")));

        perfil.setFormaIngresso(pedido.formaIngresso());
        perfil.setModalidadeIngresso(pedido.modalidadeIngresso());
        perfil.setMeta(pedido.meta());
        perfil.setNotaPretendida(pedido.notaPretendida());
        perfil.setNivel(pedido.nivel());
        perfil.setMinutosDiarios(pedido.minutosDiarios());
        perfil.getDificuldades().clear();
        if (pedido.dificuldades() != null) {
            perfil.getDificuldades().addAll(pedido.dificuldades());
        }
        if (pedido.onboardingConcluido() != null) {
            perfil.setOnboardingConcluido(pedido.onboardingConcluido());
        }
        return PerfilResponse.de(perfil);
    }

    private PerfilEstudo buscar(String email) {
        return perfis.findByUsuarioEmail(email)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Perfil não encontrado."));
    }
}
