// dashboard.js — Etapa 3: dashboard personalizado
(async function () {
  const app = document.getElementById('dashboardApp');
  const usuario = await NivoraApi.exigirLogin();
  const perfil = await NivoraPerfil.carregar();
  const esc = NivoraApi.escapar;

  if (!perfil) {
    app.innerHTML = `
      <h1 class="section-title">Você ainda não montou seu plano</h1>
      <p class="hint">Complete o onboarding para ver seu dashboard personalizado.</p>
      <a href="/pages/onboarding.html" class="btn btn-primary">Montar meu plano</a>
    `;
    return;
  }

  const [dados, recomendacao] = await Promise.all([
    NivoraApi.get('/api/desempenho'),
    NivoraPerfil.prioridades()
  ]);
  const gam = dados.gamificacao;
  const progressoGeral = Math.round((
    dados.percentual +
    Math.min(100, dados.sequenciaDias * 10) +
    Math.min(100, dados.respondidas)
  ) / 3);
  const prioridadesAtuais = recomendacao.prioridades;
  const ordem = Object.values(prioridadesAtuais).sort((a, b) => b.score - a.score);
  const nomesConquistas = { xp100: 'Primeiros 100 XP', sequencia7: '7 dias estudando', questoes100: '100 questões resolvidas', primeiraRedacao: 'Primeira redação' };

  app.innerHTML = `
    <h1 class="section-title">Olá, ${esc(usuario.nome.split(' ')[0])}! Este é o seu caminho até a faculdade.</h1>

    <div class="dash-grid">
      <div class="dash-card"><span class="dash-label">Curso</span><strong>${esc(perfil.curso.nome)}</strong></div>
      <div class="dash-card"><span class="dash-label">Faculdade</span><strong>${esc(perfil.faculdade?.nome)}</strong></div>
      <div class="dash-card"><span class="dash-label">Forma de ingresso</span><strong>${esc(perfil.ingresso)}</strong></div>
      <div class="dash-card"><span class="dash-label"> </span><a href="/pages/objetivo.html" class="btn btn-outline btn-sm">Conheça seu objetivo</a></div>
      <div class="dash-card"><span class="dash-label">Meta</span><strong>${esc(perfil.meta)}</strong></div>
      <div class="dash-card dash-card-wide">
        <span class="dash-label">Progresso geral</span>
        <div class="mini-bar" style="margin-top:6px;"><div class="mini-bar-fill" style="width:${progressoGeral}%"></div></div>
        <strong style="font-size:.9rem;">${progressoGeral}%</strong>
      </div>
      <div class="dash-card"><span class="dash-label">Questões respondidas</span><strong>${dados.respondidas}</strong></div>
      <div class="dash-card"><span class="dash-label">Acertos</span><strong>${dados.percentual}%</strong></div>
      <div class="dash-card"><span class="dash-label">Sequência de estudos</span><strong>${dados.sequenciaDias} dias</strong></div>
      <div class="dash-card"><span class="dash-label">Nível</span><strong>Nível ${gam.nivel} — ${gam.xpNoNivel}/100 XP</strong></div>
    </div>

    <h2 class="section-title" style="margin-top:40px;">Sua estratégia</h2>
    <div class="resultado-prioridades">
      ${ordem.map(p => `
        <div class="prioridade-item">
          <span>${p.cor} ${p.label} — ${p.nivel}</span>
          ${p.motivos.map(m => `<p class="hint">${esc(m)}</p>`).join('')}
        </div>
      `).join('')}
    </div>
    ${NivoraPerfil.htmlAvisos(recomendacao.avisos)}

    <h2 class="section-title" style="margin-top:40px;">Conquistas</h2>
    <div class="metas-lista">
      ${gam.conquistas.length
        ? gam.conquistas.map(id => `<div class="assunto-item">🏆 ${nomesConquistas[id] || id}</div>`).join('')
        : `<p class="hint">Nenhuma conquista ainda. Continue estudando!</p>`}
    </div>
  `;
})();
