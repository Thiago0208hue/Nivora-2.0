# PROGRESSO — Nivora 2.0

## Etapa atual
Etapa 4 — Frontend e onboarding (**escrita, NÃO testada**).

## Concluídas
- Etapa 0 — Auditoria (doc "Nivora 2.0 — Etapa 0: Diagnóstico").
- Etapa 1 — Preparação do backend (validada pelo usuário: `./mvnw test`).

## Etapa 4: alterações (não testadas)
- Frontend original copiado para `src/main/resources/static/` (`index.html`, `pages/`, `css/`, `js/`, `data/`, `assets/`) com caminhos absolutos (`/css/...`, `/pages/...`).
- Novas telas: `pages/login.html` e `pages/cadastro.html` (+ `login.js`, `cadastro.js`).
- `js/api.js`: fetch com cookie de sessão e token CSRF, `exigirLogin`, `sair`, `escapar` (anti-XSS).
- `js/perfil.js`: perfil via API (`GET/PUT /api/perfil`) e catálogo (`/api/cursos`, `/api/instituicoes`).
- Ligados à API: onboarding (grava no MySQL), dashboard, configurações, página inicial (botões mudam se logado), logout.
- Backend: `V2__perfil_ingresso_meta.sql` (colunas `forma_ingresso` e `meta`), `CursoController/Service/Response`, `InstituicaoController/Service/Response`, campos novos em `PerfilEstudo/Request/Response`.
- Testes: `FrontendTest` (arquivos estáticos), mais um caso em `AutenticacaoTest` (catálogo).

## Etapa 3: alterações (não testadas)
- `pom.xml`: `spring-boot-starter-security` e `-security-test`.
- `application.properties`: sessão de 2h, cookie HttpOnly e SameSite=Lax.
- `security/SecurityConfig`: BCrypt, sessão, CSRF por cookie (`XSRF-TOKEN` → cabeçalho `X-XSRF-TOKEN`), `/api/**` exige login (401 sem sessão), logout em `POST /api/auth/logout`.
- `security/UsuarioDetailsService`: busca o usuário pelo e-mail.
- `config/TratadorDeErros` e `NegocioException`: erros em JSON (400, 401, 404, 409).
- `usuario/`: `AuthController` (`/api/auth/csrf`, `cadastro`, `login`, `me`), `PerfilController` (`GET/PUT /api/perfil`), `UsuarioService`, `PerfilService` e DTOs.
- Testes: `seguranca/AutenticacaoTest` (MockMvc) e `seguranca/UsuarioServiceTest` (Mockito).

## Etapa 2: alterações (validadas se `./mvnw test` passou)
- `pom.xml`: `spring-boot-starter-flyway` e `flyway-mysql`.
- `application.properties` (main e test): `ddl-auto=validate`.
- `db/migration/V1__banco.sql`: cria usuario, curso, instituicao, perfil_estudo, perfil_dificuldade, peso_instituicao, nota_corte.; carga inicial: 14 cursos (pesos ESTIMADOS) e 10 instituições do protótipo; peso_instituicao e nota_corte vazias.
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
- Se `springSecurity()`/MockMvc ou nomes de starters do Boot 4 falharem na compilação, ajustar import/dependência (pontos de maior risco da Etapa 3).
- Painel admin (papel ADMIN) ainda não existe; só o papel ESTUDANTE.
- Temporário: `priorities.js` (cálculo de prioridades) ainda roda no navegador; vai para o backend na Etapa 5.
- Temporário: Plano, Assuntos, Treinar, Desempenho, Redação, Metas e Objetivo ainda usam localStorage; `perfil.js` guarda uma cópia do perfil lá para elas funcionarem. Progresso local é apagado ao sair.
- Páginas internas são servidas sem login (o JS redireciona); os dados estão protegidos na API.
- Fontes do Google Fonts ainda carregadas de terceiros (LGPD): resolver nas etapas 10/11 (hospedar localmente).
- Onboarding refeito não pré-preenche as respostas anteriores.
- Falta exclusão de conta (LGPD).

## Decisões técnicas
- H2 só nos testes; MySQL real em desenvolvimento/produção.
- Hibernate sem `ddl-auto`; esquema por migrações.
- Pesos por curso ficam na tabela `curso` (estimativa); pesos oficiais por instituição, em `peso_instituicao`, sempre com ano e fonte.
- Enums gravados como texto (`@JdbcTypeCode(VARCHAR)`) para evitar tipo ENUM nativo do MySQL.
- Autenticação por sessão (JSESSIONID) com CSRF por cookie; login manual no AuthController para devolver JSON em vez de redirecionar.
- Senha mínima de 8 e máxima de 72 caracteres (limite do BCrypt); e-mail normalizado em minúsculas.
- Frontend e API na mesma origem: sem CORS; páginas em `/pages/`, caminhos absolutos.
- Perfil guarda `formaIngresso` (SISU/ProUni...) e `modalidadeIngresso` (ampla concorrência/escola pública...) em campos separados, como no onboarding original.
