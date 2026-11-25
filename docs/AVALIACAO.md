# Avaliação acadêmica (TCC)

> Este documento é um **roteiro técnico**, não aprovação ética. Qualquer coleta com participantes precisa seguir a orientação do(a) orientador(a) e do comitê de ética da instituição (CEP/Plataforma Brasil, quando aplicável) **antes** de começar.

## 1. Método: Design Science Research (DSR)
| Etapa | No projeto |
|---|---|
| Problema | estudo para o ENEM sem personalização nem acompanhamento acessível |
| Objetivos da solução | plano e prioridades personalizados, prática com questões, acompanhamento |
| Projeto e desenvolvimento | Nivora 2.0 (este repositório) |
| Demonstração | uso do sistema por estudantes em cenário real ou simulado |
| Avaliação | SUS, pré/pós-teste, métricas de uso (abaixo) |
| Comunicação | texto do TCC |

## 2. Questionário SUS (usabilidade)
Escala de 1 (discordo totalmente) a 5 (concordo totalmente). Use a **versão em português validada** indicada pelo(a) orientador(a) e cite a fonte; abaixo, tradução de trabalho dos 10 itens originais (Brooke, 1996):

1. Acho que gostaria de usar este sistema com frequência.
2. Achei o sistema desnecessariamente complexo.
3. Achei o sistema fácil de usar.
4. Acho que precisaria de ajuda de uma pessoa técnica para usar o sistema.
5. Achei que as funções do sistema estavam bem integradas.
6. Achei que havia muita inconsistência no sistema.
7. Imagino que a maioria das pessoas aprenderia a usar este sistema rapidamente.
8. Achei o sistema muito trabalhoso de usar.
9. Senti-me muito confiante ao usar o sistema.
10. Precisei aprender muitas coisas antes de conseguir usar o sistema.

**Cálculo:** itens ímpares: `resposta − 1`; itens pares: `5 − resposta`; soma × 2,5 (resultado de 0 a 100). Na literatura, média em torno de 68 costuma ser usada como referência. O script `docs/pesquisa/calcular_sus.py` faz o cálculo a partir de um CSV (`codigo,q1,...,q10`).

## 3. Pré-teste e pós-teste (aprendizagem)
- **Instrumento:** mesmo conjunto de questões (ou versões equivalentes) antes e depois do período de uso.
- **Questões:** devem ser **oficiais do ENEM com ano e fonte** (ou autorais revisadas); as de exemplo do sistema **não servem** para a pesquisa.
- **Pareamento:** por **código de participante** (ex.: P01), nunca por nome ou e-mail.
- **Análise sugerida:** diferença de médias pareadas e tamanho de efeito; o teste estatístico deve ser escolhido com o(a) orientador(a) conforme o tamanho da amostra.
- **Aplicação:** formulário externo ou papel, fora do Nivora, para não misturar dados de pesquisa com a conta do estudante.

## 4. Métricas de utilização (instrumentação)
Disponíveis em `GET /api/admin/metricas` (somente ADMIN), **só totais**:
usuários cadastrados, onboarding concluído, estudantes ativos nos últimos 7 dias, questões respondidas, percentual de acerto geral, simulados finalizados, redações salvas.

O sistema não registra cliques, tempo de tela, localização nem identificadores de dispositivo. Se a pesquisa exigir algo além disso, discutir a necessidade e o impacto na LGPD antes de implementar.

## 5. Métricas de avaliação sugeridas
| Dimensão | Métrica | Fonte |
|---|---|---|
| Usabilidade | pontuação SUS | questionário |
| Aprendizagem | diferença pós − pré | testes |
| Engajamento | ativos em 7 dias, questões por estudante | métricas |
| Adoção | % que concluiu o onboarding | métricas |
| Utilidade percebida | perguntas abertas sobre plano e prioridades | questionário |

## 6. LGPD e ética
- **Base e consentimento:** TCLE em linguagem clara; participação voluntária; direito de desistir.
- **Minimização:** o sistema guarda nome, e-mail, perfil de estudo, respostas e redações. Nada além disso.
- **Texto da redação é dado pessoal:** só o dono acessa. Não exportar redações para a pesquisa sem consentimento específico.
- **Pseudonimização:** na análise, trocar e-mail por código de participante.
- **Grupos pequenos:** totais de poucas pessoas podem identificar alguém; avaliar antes de divulgar.
- **Pendências técnicas de LGPD:** exclusão de conta e de redações, política de privacidade, hospedagem local das fontes (hoje vêm do Google Fonts) e prazo de retenção.
- **Menores de 18 anos:** podem ser participantes (público do ENEM); exigir autorização dos responsáveis conforme o comitê de ética.
