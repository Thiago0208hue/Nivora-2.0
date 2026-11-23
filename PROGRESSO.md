# PROGRESSO — Nivora 2.0

## Etapa atual
Etapa 2 — MySQL e persistência (**escrita, NÃO testada**).

## Concluídas
- Etapa 0 — Auditoria (doc "Nivora 2.0 — Etapa 0: Diagnóstico").
- Etapa 1 — Preparação do backend (validada pelo usuário: `./mvnw test`).

## Etapa 2: alterações (não testadas)
- `pom.xml`: `spring-boot-starter-flyway` e `flyway-mysql`.
- `application.properties` (main e test): `ddl-auto=validate`.
- `db/migration/V1__tabelas.sql`: usuario, curso, instituicao, perfil_estudo, perfil_dificuldade, peso_instituicao, nota_corte.
- `db/migration/V2__dados_iniciais.sql`: 14 cursos (pesos ESTIMADOS) e 10 instituições do protótipo; peso_instituicao e nota_corte vazias.
- Entidades e repositories em `usuario/`, `curso/`, `instituicao/`, `personalizacao/`.
- `PersistenciaTest`: carga inicial, perfil com dificuldades, e-mail duplicado, nota de corte com ano e fonte.

## Etapa 1: alterações (validadas)
- `pom.xml`: removidas tags vazias; adicionado H2 (escopo test).
- `application.properties`: datasource por variáveis `DB_URL`, `DB_USER`, `DB_PASSWORD`; `ddl-auto=none`; `open-in-view=false`.
- `src/test/resources/application.properties`: H2 em memória (modo MySQL) para os testes.
- `.gitignore`: `.env`.

## Pendente / problemas
- O ambiente da IA não acessa o Maven Central (403): build e testes não foram executados.
  Rodar `./mvnw test` localmente; esperado: `contextLoads` passa.
- Se `validate` reclamar de tipo de coluna, ajustar a entidade ou a migração (primeiro ponto a conferir).
- Segurança (Etapa 3) ainda não adicionada.

## Decisões técnicas
- H2 só nos testes; MySQL real em desenvolvimento/produção.
- Hibernate sem `ddl-auto`; esquema por migrações.
- Pesos por curso ficam na tabela `curso` (estimativa); pesos oficiais por instituição, em `peso_instituicao`, sempre com ano e fonte.
- Enums gravados como texto (`@JdbcTypeCode(VARCHAR)`) para evitar tipo ENUM nativo do MySQL.
