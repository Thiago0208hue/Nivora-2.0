# Banco de dados

Esquema criado por migrações em `src/main/resources/db/migration` (V1 a V6). Diagrama (Mermaid):

```mermaid
erDiagram
  USUARIO ||--|| PERFIL_ESTUDO : tem
  PERFIL_ESTUDO }o--o| CURSO : escolhe
  PERFIL_ESTUDO }o--o| INSTITUICAO : escolhe
  PERFIL_ESTUDO ||--o{ PERFIL_DIFICULDADE : declara
  CURSO ||--o{ PESO_INSTITUICAO : tem
  INSTITUICAO ||--o{ PESO_INSTITUICAO : define
  CURSO ||--o{ NOTA_CORTE : tem
  INSTITUICAO ||--o{ NOTA_CORTE : define
  USUARIO ||--o{ RESPOSTA_QUESTAO : responde
  QUESTAO ||--o{ ALTERNATIVA : possui
  QUESTAO ||--o{ RESPOSTA_QUESTAO : recebe
  USUARIO ||--o{ SIMULADO : faz
  SIMULADO ||--o{ SIMULADO_QUESTAO : contem
  SIMULADO |o--o{ RESPOSTA_QUESTAO : agrupa
  USUARIO ||--o{ REDACAO : escreve

  USUARIO { bigint id PK
    string nome
    string email UK
    string senha_hash
    string papel
    datetime criado_em }
  PERFIL_ESTUDO { bigint id PK
    bigint usuario_id FK
    bigint curso_id FK
    bigint instituicao_id FK
    string forma_ingresso
    string modalidade_ingresso
    string meta
    decimal nota_pretendida
    string nivel
    int minutos_diarios
    boolean onboarding_concluido }
  CURSO { bigint id PK
    string codigo UK
    string nome
    int pesos_por_area }
  INSTITUICAO { bigint id PK
    string sigla UK
    string nome }
  PESO_INSTITUICAO { bigint id PK
    string area
    decimal peso
    int ano
    string fonte }
  NOTA_CORTE { bigint id PK
    string modalidade
    int ano
    decimal nota
    string fonte }
  QUESTAO { bigint id PK
    string area
    string disciplina
    string assunto
    string dificuldade
    int ano
    string origem
    string fonte
    boolean ativa }
  ALTERNATIVA { bigint id PK
    bigint questao_id FK
    string letra
    boolean correta }
  SIMULADO { bigint id PK
    bigint usuario_id FK
    string tipo
    string area
    datetime criado_em
    datetime finalizado_em }
  SIMULADO_QUESTAO { bigint simulado_id FK
    int ordem
    bigint questao_id }
  RESPOSTA_QUESTAO { bigint id PK
    bigint usuario_id FK
    bigint questao_id FK
    bigint simulado_id FK
    string letra_escolhida
    boolean correta
    datetime respondida_em }
  REDACAO { bigint id PK
    bigint usuario_id FK
    string tema
    string texto
    int c1_a_c5
    datetime criada_em }
  TEMA_REDACAO { bigint id PK
    string titulo
    string origem }
```

Observações:
- `PESO_INSTITUICAO` e `NOTA_CORTE` começam **vazias**: só entram dados com ano e fonte reais.
- `QUESTAO.origem`: `ENEM_OFICIAL` (exige ano e fonte), `EXEMPLO` ou `AUTORAL`.
- `TEMA_REDACAO` não tem ligação com `REDACAO`: a redação guarda o título do tema como texto.
- Dados pessoais: `usuario` (nome, e-mail, senha com hash) e `redacao.texto`. Ver [AVALIACAO.md](AVALIACAO.md).
