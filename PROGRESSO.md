# PROGRESSO — Nivora 2.0

## Etapa atual
Etapa 6 — Gerador de plano semanal (**escrita, NÃO testada**).

## Concluídas
- Etapa 0 — Auditoria (doc "Nivora 2.0 — Etapa 0: Diagnóstico").
- Etapa 1 — Preparação do backend (validada pelo usuário: `./mvnw test`).

## Etapa 6: alterações (não testadas)
- `plano/GeradorPlanoSemanal`: gerador puro. 6 dias (segunda a sábado), 2 blocos de estudo por dia de áreas diferentes + revisão quando há 60 min ou mais; soma do dia = tempo disponível exato; blocos < 60 min: 60%/40%, sem revisão; >= 60 min: 50% principal, 20% revisão (mínimo 10), resto no secundário.
- Distribuição: cada área ganha ao menos 1 bloco na semana (prioridade alta, 2); os blocos restantes vão por D'Hondt (score/(blocos+1)); máximo 1 bloco por área por dia. Escolha diária: as duas áreas com mais blocos restantes (garante encaixe sem repetir), preferindo as não estudadas no dia anterior.
- `plano/PlanoService` (usa `PersonalizacaoService`, então considera dificuldades e desempenho; dificuldades viram "foco" nos blocos) e `POST /api/plano/semanal` (`PlanoController`).
- Frontend: `plano.js` reescrito para usar a API (mostra blocos com prioridade e foco, distribuição da semana e avisos).
- Testes: `GeradorPlanoSemanalTest` (11, perfis Direito, Medicina e Computação, vários tempos e níveis), `PlanoServiceTest` (4) e um caso em `AutenticacaoTest`.

## Etapa 5: alterações (não testadas)
- `personalizacao/CalculadoraPrioridades`: cálculo puro das prioridades (sem banco), com motivos explicados por área.
- `personalizacao/PersonalizacaoService`: reúne perfil, pesos da instituição (ano mais recente), nota de corte da modalidade e dificuldades; gera avisos.
- `POST /api/personalizacao/prioridades` (`PersonalizacaoController`): aceita o desempenho por área (opcional, vem do navegador até a Etapa 8).
- Regras: peso da instituição (oficial ou estimado) substitui o do curso quando existir; sem dado, usa a estimativa do curso e avisa. Nota de corte só da modalidade escolhida, com ano e fonte. Nota-alvo >= 700 reforça áreas de peso >= 4. Dificuldade +2; desempenho (>= 3 questões) +2/+1/-1; nível soma o mesmo valor a todas as áreas (muda a classificação, não a ordem); tempo < 60 min sobe os limiares em 1 e >= 240 min desce em 1. Limiares: Alta >= 5, Média >= 3,5.
- Frontend: dashboard, resultado do onboarding, plano e assuntos usam as prioridades do servidor (`NivoraPerfil.prioridades`); `priorities.js` ficou só com a agregação do desempenho. Avisos aparecem no dashboard e no onboarding.
- `AreaEnem.daDisciplina`: mapeia disciplina para área.
- Testes: `CalculadoraPrioridadesTest` (11) e `PersonalizacaoServiceTest` (10).

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
- `peso_instituicao` e `nota_corte` estão vazias: a instituição só muda a recomendação quando houver dado cadastrado com ano e fonte (não inventar). Falta um jeito de cadastrar (painel admin ou migração com dados reais de edital/SISU).
- Limiares e bônus da calculadora são heurísticas do Nivora (constantes em `CalculadoraPrioridades`), a validar na pesquisa.
- Plano gerado na hora a cada acesso (não é gravado no banco); histórico de planos, se necessário, fica para depois.
- Domingo fica sem estudo (descanso), como no protótipo.
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
- Cálculo no servidor, sem IA generativa; a calculadora é uma classe pura (testável sem banco).
- O desempenho vem do navegador (POST) só até a Etapa 8, quando passa a ser lido do banco.
- Plano 100% calculado no servidor, sem IA; o gerador é uma classe pura testada para vários perfis e tempos.
