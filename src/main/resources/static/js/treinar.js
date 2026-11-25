// treinar.js — Etapa 7: prática livre, simulados e revisão de erros (questões vêm da API)
(function () {
  const quizArea = document.getElementById('quizArea');
  const resultado = document.getElementById('resultado');
  const esc = NivoraApi.escapar;
  let catalogo = [];
  let questoes = [];
  let simuladoId = null;
  let inicio = null;
  const corrigidas = new Set();

  (async function iniciarPagina() {
    await NivoraApi.exigirLogin();
    catalogo = await NivoraApi.get('/api/questoes/catalogo');
    popularMaterias();
    popularAnos();
    ligar();
  })();

  function ligar() {
    document.querySelectorAll('#modos button').forEach(b => b.addEventListener('click', () => trocarModo(b.dataset.modo)));
    document.getElementById('filtroArea').addEventListener('change', popularMaterias);
    document.getElementById('filtroMateria').addEventListener('change', popularAssuntos);
    document.getElementById('btnComecar').addEventListener('click', iniciarPratica);
    document.getElementById('simTipo').addEventListener('change', e => {
      document.getElementById('simArea').style.display = e.target.value === 'AREA' ? '' : 'none';
    });
    document.getElementById('btnSimulado').addEventListener('click', iniciarSimulado);
  }

  function trocarModo(modo) {
    document.querySelectorAll('#modos button').forEach(b => b.classList.toggle('btn-primary', b.dataset.modo === modo));
    document.getElementById('painelPratica').style.display = modo === 'pratica' ? '' : 'none';
    document.getElementById('painelSimulado').style.display = modo === 'simulado' ? '' : 'none';
    quizArea.innerHTML = '';
    resultado.innerHTML = '';
    document.getElementById('avisoOrigem').textContent = '';
    if (modo === 'revisao') carregarRevisao();
  }

  // ---------- filtros ----------
  function popularMaterias() {
    const area = document.getElementById('filtroArea').value;
    const materias = [...new Set(catalogo.filter(c => !area || c.area === area).map(c => c.disciplina))].sort();
    document.getElementById('filtroMateria').innerHTML = '<option value="">Todas as matérias</option>' +
      materias.map(m => `<option value="${esc(m)}">${esc(m)}</option>`).join('');
    popularAssuntos();
  }

  function popularAssuntos() {
    const area = document.getElementById('filtroArea').value;
    const materia = document.getElementById('filtroMateria').value;
    const assuntos = [...new Set(catalogo
      .filter(c => !area || c.area === area).filter(c => !materia || c.disciplina === materia)
      .map(c => c.assunto))].sort();
    document.getElementById('filtroAssunto').innerHTML = '<option value="">Todos os assuntos</option>' +
      assuntos.map(a => `<option value="${esc(a)}">${esc(a)}</option>`).join('');
  }

  function popularAnos() {
    const anos = [...new Set(catalogo.map(c => c.ano).filter(Boolean))].sort();
    if (!anos.length) return;
    const sel = document.getElementById('filtroAno');
    sel.style.display = '';
    anos.forEach(a => sel.insertAdjacentHTML('beforeend', `<option value="${a}">${a}</option>`));
  }

  // ---------- prática livre ----------
  async function iniciarPratica() {
    const v = id => document.getElementById(id).value;
    try {
      questoes = await NivoraApi.post('/api/questoes/praticar', {
        area: v('filtroArea') || null, disciplina: v('filtroMateria') || null,
        assunto: v('filtroAssunto') || null, dificuldade: v('filtroDificuldade') || null,
        ano: v('filtroAno') ? Number(v('filtroAno')) : null, quantidade: Number(v('filtroQuantidade'))
      });
    } catch (e) { quizArea.innerHTML = `<p class="hint">${esc(e.message)}</p>`; return; }
    simuladoId = null;
    corrigidas.clear();
    resultado.innerHTML = '';
    inicio = Date.now();
    renderizar(true);
  }

  // ---------- simulado ----------
  async function iniciarSimulado() {
    try {
      const tipo = document.getElementById('simTipo').value;
      const s = await NivoraApi.post('/api/simulados', {
        tipo, area: tipo === 'AREA' ? document.getElementById('simArea').value : null,
        quantidade: Number(document.getElementById('simQuantidade').value)
      });
      simuladoId = s.id;
      questoes = s.questoes;
    } catch (e) { quizArea.innerHTML = `<p class="hint">${esc(e.message)}</p>`; return; }
    resultado.innerHTML = '';
    inicio = Date.now();
    renderizar(false);
  }

  // ---------- renderização ----------
  function renderizar(corrigirNaHora) {
    if (!questoes.length) {
      quizArea.innerHTML = '<p class="hint">Nenhuma questão encontrada para esse filtro.</p>';
      return;
    }
    const temExemplo = questoes.some(q => !q.origem.startsWith('ENEM'));
    document.getElementById('avisoOrigem').textContent = temExemplo
      ? 'Atenção: questões marcadas como "Exemplo" não são do ENEM oficial.' : '';
    quizArea.innerHTML = questoes.map((q, i) => `
      <div class="questao-card" data-id="${q.id}">
        <p class="questao-meta">${esc(q.disciplina)} • ${esc(q.assunto)} • ${esc(q.origem)}${q.ano ? ' • ' + q.ano : ''}</p>
        <p class="questao-enunciado"><strong>${i + 1}.</strong> ${esc(q.enunciado)}</p>
        <div class="alternativas">
          ${q.alternativas.map(a => `
            <label class="option-card">
              <input type="radio" name="q${q.id}" value="${esc(a.letra)}">
              <span><strong>${esc(a.letra)})</strong> ${esc(a.texto)}</span>
            </label>`).join('')}
        </div>
        <p class="feedback"></p>
      </div>`).join('') +
      (corrigirNaHora
        ? '<button class="btn btn-primary" id="btnCorrigir">Corrigir</button>'
        : '<button class="btn btn-primary" id="btnFinalizar">Finalizar simulado</button>');
    const bc = document.getElementById('btnCorrigir');
    if (bc) bc.addEventListener('click', corrigirPratica);
    const bf = document.getElementById('btnFinalizar');
    if (bf) bf.addEventListener('click', finalizarSimulado);
  }

  function marcada(q) {
    const el = document.querySelector(`input[name="q${q.id}"]:checked`);
    return el ? el.value : null;
  }

  async function corrigirPratica() {
    let acertos = 0, respondidas = 0;
    const erros = {};
    for (const q of questoes) {
      const letra = marcada(q);
      if (!letra || corrigidas.has(q.id)) continue;
      const c = await NivoraApi.post(`/api/questoes/${q.id}/responder`, { letra });
      corrigidas.add(q.id);
      respondidas++;
      if (c.acertou) acertos++; else erros[q.assunto] = (erros[q.assunto] || 0) + 1;
      mostrarCorrecao(q, c.letraEscolhida, c.letraCorreta, c.resolucao);
      compatibilidade(q.assunto, c.acertou);
    }
    if (!respondidas) { resultado.innerHTML = '<p class="hint">Marque ao menos uma alternativa.</p>'; return; }
    NivoraGamification.adicionarXP(acertos * 5);
    const pct = Math.round((acertos / respondidas) * 100);
    resultado.innerHTML = `<div class="resultado-card"><p><strong>Acertos:</strong> ${acertos}/${respondidas} (${pct}%)</p>
      <p><strong>Tempo:</strong> ${Math.round((Date.now() - inicio) / 1000)}s</p>
      ${Object.keys(erros).length ? `<p><strong>Assuntos com erro:</strong> ${Object.keys(erros).map(esc).join(', ')}</p>` : ''}</div>`;
    document.getElementById('btnCorrigir').disabled = true;
  }

  async function finalizarSimulado() {
    for (const q of questoes) {
      const letra = marcada(q);
      if (!letra) continue;
      try { await NivoraApi.post(`/api/simulados/${simuladoId}/questoes/${q.id}/responder`, { letra }); }
      catch (e) { if (e.status !== 409) throw e; }
    }
    const r = await NivoraApi.post(`/api/simulados/${simuladoId}/finalizar`);
    const porId = Object.fromEntries(questoes.map(q => [q.id, q]));
    r.itens.forEach(it => {
      if (it.letraEscolhida) compatibilidade(it.assunto, it.acertou);
      mostrarCorrecao(porId[it.questaoId], it.letraEscolhida, it.letraCorreta, it.resolucao);
    });
    NivoraGamification.adicionarXP(r.acertos * 5);
    resultado.innerHTML = `<div class="resultado-card"><p><strong>Acertos:</strong> ${r.acertos}/${r.total}
      (${Math.round((r.acertos / r.total) * 100)}%)</p><p><strong>Em branco:</strong> ${r.emBranco}</p></div>`;
    document.getElementById('btnFinalizar').disabled = true;
  }

  function mostrarCorrecao(q, escolhida, correta, resolucao) {
    const card = document.querySelector(`.questao-card[data-id="${q.id}"]`);
    if (!card) return;
    card.querySelectorAll('.option-card').forEach(label => {
      const input = label.querySelector('input');
      input.disabled = true;
      if (input.value === correta) label.classList.add('correta');
      else if (input.value === escolhida) label.classList.add('errada');
    });
    card.querySelector('.feedback').innerHTML = (escolhida
      ? (escolhida === correta ? '✅ Acertou.' : `❌ Resposta correta: ${esc(correta)}.`)
      : `Em branco. Resposta correta: ${esc(correta)}.`) +
      (resolucao ? `<br><em>${esc(resolucao)}</em>` : '');
  }

  // ---------- revisão de erros ----------
  async function carregarRevisao() {
    const lista = await NivoraApi.get('/api/questoes/revisao');
    if (!lista.length) { quizArea.innerHTML = '<p class="hint">Nenhum erro para revisar. 🎉</p>'; return; }
    quizArea.innerHTML = lista.map(r => `
      <div class="questao-card">
        <p class="questao-meta">${esc(r.questao.disciplina)} • ${esc(r.questao.assunto)} • ${esc(r.questao.origem)}</p>
        <p class="questao-enunciado">${esc(r.questao.enunciado)}</p>
        <div class="alternativas">${r.questao.alternativas.map(a => `
          <div class="option-card ${a.letra === r.letraCorreta ? 'correta' : a.letra === r.letraEscolhida ? 'errada' : ''}">
            <span><strong>${esc(a.letra)})</strong> ${esc(a.texto)}</span></div>`).join('')}</div>
        <p class="feedback">Você marcou ${esc(r.letraEscolhida)}; correta: ${esc(r.letraCorreta)}.
          ${r.resolucao ? '<br><em>' + esc(r.resolucao) + '</em>' : ''}</p>
      </div>`).join('');
  }

  // Cópia temporária no localStorage para as páginas antigas (dashboard/desempenho) até a Etapa 8.
  function compatibilidade(assunto, acertou) {
    const d = NivoraStorage.get(NivoraStorage.KEYS.DESEMPENHO, {});
    const x = d[assunto] || { respondidas: 0, acertos: 0 };
    x.respondidas++;
    if (acertou) x.acertos++;
    d[assunto] = x;
    NivoraStorage.set(NivoraStorage.KEYS.DESEMPENHO, d);
  }
})();
