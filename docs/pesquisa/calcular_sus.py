#!/usr/bin/env python3
"""Calcula a pontuação SUS (0 a 100) por participante e a média, a partir de um CSV.

Uso:  python3 calcular_sus.py respostas.csv
CSV:  codigo,q1,q2,...,q10   (respostas de 1 a 5; use o código do participante, nunca nome/e-mail)
"""
import csv
import statistics
import sys


def pontuacao(respostas):
    if len(respostas) != 10 or any(r not in (1, 2, 3, 4, 5) for r in respostas):
        raise ValueError("São necessárias 10 respostas entre 1 e 5")
    soma = 0
    for i, r in enumerate(respostas, start=1):
        soma += (r - 1) if i % 2 == 1 else (5 - r)
    return soma * 2.5


def main(caminho):
    resultados = []
    with open(caminho, newline="", encoding="utf-8") as f:
        for linha in csv.DictReader(f):
            try:
                valores = [int(linha[f"q{i}"]) for i in range(1, 11)]
                resultados.append((linha["codigo"], pontuacao(valores)))
            except (ValueError, KeyError) as erro:
                print(f"Linha ignorada ({linha.get('codigo', '?')}): {erro}", file=sys.stderr)
    for codigo, pontos in resultados:
        print(f"{codigo}: {pontos:.1f}")
    if resultados:
        pontos = [p for _, p in resultados]
        print(f"\nParticipantes: {len(pontos)}  Média: {statistics.mean(pontos):.1f}", end="")
        if len(pontos) > 1:
            print(f"  Desvio padrão: {statistics.stdev(pontos):.1f}", end="")
        print()


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])
