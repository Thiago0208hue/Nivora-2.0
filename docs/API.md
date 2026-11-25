# API

Base: mesma origem do site. Formato JSON. Erros: `{"erro":"mensagem"}` (validação: `{"erro":"Dados inválidos.","campos":{...}}`).

**Autenticação:** sessão por cookie. Antes de qualquer POST/PUT/DELETE, chame `GET /api/auth/csrf` e envie o token no cabeçalho `X-XSRF-TOKEN`. Sem login, rotas protegidas devolvem `401`; sem permissão, `403`.

| Método e rota | Acesso | Descrição |
|---|---|---|
| `GET /api/auth/csrf` | público | token CSRF |
| `POST /api/auth/cadastro` | público | cria conta (`nome`, `email`, `senha` 8–72) |
| `POST /api/auth/login` | público | entra (limite: 5 falhas/15 min → `429`) |
| `POST /api/auth/logout` | logado | encerra a sessão (`204`) |
| `GET /api/auth/me` | logado | dados de quem está logado |
| `GET/PUT /api/perfil` | logado | perfil de estudo |
| `GET /api/cursos`, `GET /api/instituicoes` | logado | catálogos |
| `POST /api/personalizacao/prioridades` | logado | prioridades por área, motivos e avisos |
| `POST /api/plano/semanal` | logado | cronograma semanal |
| `GET /api/questoes/catalogo` | logado | combinações de área/disciplina/assunto/ano com quantidade |
| `POST /api/questoes/praticar` | logado | sorteia questões (sem gabarito); filtros opcionais, `quantidade` 1–50 |
| `POST /api/questoes/{id}/responder` | logado | corrige (`letra` A–E) e grava |
| `GET /api/questoes/revisao` | logado | questões cuja última resposta foi errada |
| `POST /api/simulados` | logado | cria simulado (`tipo` COMPLETO ou AREA, `area`, `quantidade` 1–90) |
| `GET /api/simulados`, `GET /api/simulados/{id}` | logado | histórico e detalhe (sem gabarito) |
| `POST /api/simulados/{id}/questoes/{questaoId}/responder` | logado | grava resposta (`204`, sem revelar) |
| `POST /api/simulados/{id}/finalizar` | logado | resultado com gabarito |
| `GET /api/desempenho` | logado | totais, por área/assunto, dias, sequência, XP, conquistas |
| `GET /api/redacao/temas` | logado | temas de treino |
| `POST /api/redacao` | logado | salva redação (`tema`, `texto` ≤ 8000, `competencias` opcional com 5 notas 0/40/80/120/160/200) |
| `GET /api/redacao`, `GET /api/redacao/{id}` | logado | histórico e detalhe (só do dono) |
| `POST /api/admin/questoes` | ADMIN | cadastra questão (oficial exige ano e fonte; uma única correta) |
| `DELETE /api/admin/questoes/{id}` | ADMIN | desativa questão (`204`) |
| `GET /api/admin/metricas` | ADMIN | totais agregados de uso (sem dados pessoais) |

Códigos usados: `200`, `201`, `204`, `400`, `401`, `403`, `404`, `405`, `409`, `415`, `429`, `500`.
