package com.nivora.nivora.questao;

import com.nivora.nivora.curso.AreaEnem;

/** Resultado da consulta de catálogo; o frontend recebe CatalogoResponse. */
public record CatalogoItem(AreaEnem area, String disciplina, String assunto, Integer ano, long quantidade) {
}
