package com.nivora.nivora.instituicao;

public record InstituicaoResponse(String sigla, String nome) {

    public static InstituicaoResponse de(Instituicao instituicao) {
        return new InstituicaoResponse(instituicao.getSigla(), instituicao.getNome());
    }
}
