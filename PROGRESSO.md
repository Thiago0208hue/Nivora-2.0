# PROGRESSO — Nivora 2.0

## Etapa atual
Etapa 1 — Preparação do backend (**preparada, NÃO testada**).

## Concluídas
- Etapa 0 — Auditoria (doc "Nivora 2.0 — Etapa 0: Diagnóstico").

## Etapa 1: alterações (não testadas)
- `pom.xml`: removidas tags vazias; adicionado H2 (escopo test).
- `application.properties`: datasource por variáveis `DB_URL`, `DB_USER`, `DB_PASSWORD`; `ddl-auto=none`; `open-in-view=false`.
- `src/test/resources/application.properties`: H2 em memória (modo MySQL) para os testes.
- `.gitignore`: `.env`.

## Pendente / problemas
- O ambiente da IA não acessa o Maven Central (403): build e testes não foram executados.
  Rodar `./mvnw test` localmente; esperado: `contextLoads` passa.
- Segurança (Etapa 3) e Flyway (Etapa 2) ainda não adicionados.

## Decisões técnicas
- H2 só nos testes; MySQL real em desenvolvimento/produção.
- Hibernate sem `ddl-auto`; esquema por migrações.
