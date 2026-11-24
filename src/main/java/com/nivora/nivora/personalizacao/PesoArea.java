package com.nivora.nivora.personalizacao;

/** Peso de uma área (escala 0 a 5), com a origem e uma descrição legível da fonte. */
public record PesoArea(double valor, OrigemPeso origem, String detalhe) {
}
