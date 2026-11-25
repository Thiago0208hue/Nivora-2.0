// desempenho.js — Etapa 8: Meu Desempenho (dados vêm de GET /api/desempenho)
(async function () {
  await NivoraApi.exigirLogin();
  const app = document.getElementById('desempenhoApp');
  const esc = NivoraApi.escapar;
  const d = await NivoraApi.get('/api/desempenho');

  if (!d.respondidas) {
    app.innerHTML = `
      <h1 class="section-title">Meu Desempenho</h1>
      <p class="hint">Você ainda não respondeu questões. Responda algumas em "Treinar" para ver seu desempenho aqui.</p>
      <a href="/pages/treinar.html" class="btn btn-primary">Ir para Treinar</a>`;
    return;
  }

  app.innerHTML = `
    <h1 class="section-title">Meu Desempenho</h1>
    <div class="dash-grid">
      <div class="dash-card"><span class="dash-label">Questões respondidas</span><strong>${d.respondidas}</strong></div>
      <div class="dash-card"><span class="dash-label">Acertos</span><strong>${d.acertos} (${d.percentual}%)</strong></div>
      <div class="dash-card"><span class="dash-label">Sequência de estudos</span><strong>${d.sequenciaDias} dias</strong></div>
      <div class="dash-card"><span class="dash-label">Nível</span><strong>${d.gamificacao.nivel} — ${d.gamificacao.xp} XP</strong></div>
    </div>

    <h2 class="section-title" style="margin-top:40px;">Evolução dos acertos por dia</h2>
    ${grafico(d.ultimosDias)}

    <h2 class="section-title" style="margin-top:40px;">Desempenho por área</h2>
    <div class="assunto-lista">
      ${d.porArea.map(a => barra(a.rotulo, a.percentual, `${a.acertos}/${a.respondidas}`)).join('')}
    </div>

    <h2 class="section-title" style="margin-top:40px;">Desempenho por assunto</h2>
    <div class="assunto-lista">
      ${d.porAssunto.map(a => barra(a.assunto, a.percentual, `${a.acertos}/${a.respondidas}`)).join('')}
    </div>

    <h2 class="section-title" style="margin-top:40px;">O que estudar agora?</h2>
    ${recomendacao(d.assuntoMaisFraco)}`;

  function barra(titulo, perc, detalhe) {
    return `
      <div class="assunto-item">
        <div class="assunto-item-topo"><span>${esc(titulo)}</span><span class="hint">${perc}% (${esc(detalhe)})</span></div>
        <div class="mini-bar"><div class="mini-bar-fill" style="width:${perc}%"></div></div>
      </div>`;
  }

  function grafico(dias) {
    if (dias.length < 2) return '<p class="hint">Responda questões em mais de um dia para ver o gráfico de evolução.</p>';
    const w = 600, h = 180, pad = 24;
    const pts = dias.map((x, i) => ({
      x: pad + (i * (w - pad * 2)) / (dias.length - 1),
      y: pad + (h - pad * 2) * (1 - x.percentual / 100), d: x
    }));
    return `
      <svg viewBox="0 0 ${w} ${h}" class="grafico-evolucao" style="width:100%;height:auto;">
        <line x1="${pad}" y1="${h - pad}" x2="${w - pad}" y2="${h - pad}" stroke="var(--border)" />
        <polyline points="${pts.map(p => p.x + ',' + p.y).join(' ')}" fill="none" stroke="var(--accent)" stroke-width="2" />
        ${pts.map(p => `<circle cx="${p.x}" cy="${p.y}" r="3" fill="var(--accent)"><title>${esc(p.d.data)} — ${p.d.percentual}% (${p.d.acertos}/${p.d.respondidas})</title></circle>`).join('')}
      </svg>
      <p class="hint">Últimos dias com atividade (${dias[0].percentual}% → ${dias[dias.length - 1].percentual}%)</p>`;
  }

  function recomendacao(f) {
    if (!f) return '<p class="hint">Nenhum assunto com taxa de acerto baixa (considerando ao menos 3 respostas). Continue treinando.</p>';
    return `
      <div class="resultado-card">
        <p>Seu assunto com mais erros é <strong>${esc(f.assunto)}</strong> (${f.percentual}% de acerto em ${f.respondidas} respostas).</p>
        <ol class="hint" style="padding-left:18px;">
          <li>Revisar a teoria do assunto.</li>
          <li>Resolver questões básicas dele.</li>
          <li>Refazer as que você errou em <a href="/pages/treinar.html">Treinar → Revisar erros</a>.</li>
        </ol>
        <p class="hint">Regra do Nivora (não oficial): assunto com menos de 70% de acerto e ao menos 3 respostas.</p>
      </div>`;
  }
})();
