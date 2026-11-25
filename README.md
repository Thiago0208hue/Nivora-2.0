# Nivora 2.0

Plataforma web de preparação para o ENEM, com plano de estudos personalizado, banco de questões, simulados, acompanhamento de desempenho e prática de redação. Projeto de TCC.

> Estado: desenvolvimento. Nenhum dado oficial do ENEM/INEP é inventado: pesos e notas de corte só existem com ano e fonte; as questões incluídas por padrão são **exemplos não oficiais**.

## Tecnologias
Java 21 · Spring Boot 4.1.1 · Maven · Spring Data JPA · Spring Security (sessão, BCrypt, CSRF) · MySQL (H2 só nos testes) · Flyway · HTML/CSS/JavaScript puro (Fetch API).

## Como executar

**Pré-requisitos:** JDK 21 (não basta o JRE; `javac -version` deve funcionar), MySQL 8 rodando.

1. Configure o banco por variáveis de ambiente (valores padrão entre parênteses):
   - `DB_URL` (`jdbc:mysql://localhost:3306/nivora?createDatabaseIfNotExist=true`)
   - `DB_USER` (`root`)
   - `DB_PASSWORD` (vazio)
2. Rode os testes (usam H2 em memória, não precisam do MySQL):
   ```
   ./mvnw test        # Windows: .\mvnw.cmd test
   ```
3. Suba a aplicação (o Flyway cria as tabelas sozinho):
   ```
   ./mvnw spring-boot:run
   ```
4. Abra http://localhost:8080

**Virar administrador** (cadastro de questões e métricas): crie a conta pelo site e rode no MySQL:
```sql
UPDATE usuario SET papel = 'ADMIN' WHERE email = 'seu@email.com';
```

**Produção:** `SPRING_PROFILES_ACTIVE=prod` (exige HTTPS; ativa cookie `secure`).

## Documentação
- [Arquitetura](docs/ARQUITETURA.md)
- [API](docs/API.md)
- [Banco de dados (diagrama)](docs/BANCO.md)
- [Avaliação e pesquisa (DSR, SUS, pré/pós-teste, LGPD)](docs/AVALIACAO.md)
- [Progresso e pendências](PROGRESSO.md)
