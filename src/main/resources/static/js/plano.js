// plano.js — Meu Plano: cronograma semanal gerado pelo servidor (GeradorPlanoSemanal)
(async function () {
  await NivoraApi.exigirLogin();
  const app = document.getElementById('planoApp');
  const esc = NivoraApi.escapar;
  const perfil = await NivoraPerfil.carregar();

  if (!perfil) {
    app.innerHTML = `
      <h1 class="section-title">Você ainda não montou seu plano</h1>
      <p class="hint">Complete o onboarding para gerar seu cronograma.</p>
      <a href="/pages/onboarding.html" class="btn btn-primary">Montar meu plano</a>
    `;
    return;
  }

  let plano;
  try {
    plano = await NivoraApi.post('/api/plano/semanal');
  } catch (erro) {
    app.innerHTML = `
      <h1 class="section-title">Meu Plano</h1>
      <p class="hint">Não foi possível gerar o plano agora: ${esc(erro.message)}</p>
    `;
    return;
  }

  function formatMin(min) {
    if (min >= 60) {
      const h = Math.floor(min / 60), m = min % 60;
      return m ? `${h}h${m}min` : `${h}h`;
    }
    return `${min}min`;
  }

  const cores = { ALTA: '🔴', MEDIA: '🟡', BAIXA: '🟢' };
  const rotulos = {};
  plano.dias.forEach(d => d.blocos.forEach(b => { if (b.area) rotulos[b.area] = b.rotulo; }));
  const totalEstudo = Object.values(plano.minutosPorArea).reduce((soma, m) => soma + m, 0);
  const distribuicao = Object.entries(plano.minutosPorArea).sort((a, b) => b[1] - a[1]);

  function htmlBloco(b) {
    if (b.tipo === 'REVISAO') {
      return `<p><strong>${formatMin(b.minutos)}</strong> 🔁 Revisão</p>`;
    }
    const foco = b.foco && b.foco.length
      ? `<br><span class="hint">Foco: ${esc(b.foco.join(', '))}</span>` : '';
    return `<p><strong>${formatMin(b.minutos)}</strong> ${cores[b.prioridade] || ''} ${esc(b.rotulo)}${foco}</p>`;
  }

  app.innerHTML = `
    <h1 class="section-title">Meu Plano</h1>
    <p class="hint">Cronograma de ${formatMin(plano.minutosPorDia)} por dia, gerado a partir das suas prioridades. Ele se adapta conforme seu desempenho evolui.</p>
    <div class="plano-grid">
      ${plano.dias.map(d => `
        <div class="plano-card">
          <h3>${esc(d.dia.toUpperCase())}</h3>
          ${d.blocos.map(htmlBloco).join('')}
        </div>
      `).join('')}
    </div>

    <h2 class="section-title" style="margin-top:40px;">Distribuição da semana</h2>
    <div class="metas-lista">
      ${distribuicao.map(([area, min]) => `
        <div class="assunto-item">
          <div class="assunto-item-topo"><span>${esc(rotulos[area] || area)}</span><span>${formatMin(min)}</span></div>
          <div class="mini-bar"><div class="mini-bar-fill" style="width:${totalEstudo ? Math.round(min / totalEstudo * 100) : 0}%"></div></div>
        </div>
      `).join('')}
    </div>
    ${NivoraPerfil.htmlAvisos(plano.avisos)}
  `;
})();
