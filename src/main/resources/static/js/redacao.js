// redacao.js — Etapa 9: redações salvas no servidor, com autoavaliação opcional por competência
(async function () {
  await NivoraApi.exigirLogin();
  const esc = NivoraApi.escapar;
  const temaAtual = document.getElementById('temaAtual');
  const editor = document.getElementById('editorRedacao');
  const contador = document.getElementById('contadorPalavras');
  const resultado = document.getElementById('avaliacaoResultado');
  let temas = [];

  try {
    temas = await NivoraApi.get('/api/redacao/temas');
  } catch (e) { temaAtual.textContent = 'Não foi possível carregar os temas.'; }

  function sortearTema() {
    if (!temas.length) return;
    const t = temas[Math.floor(Math.random() * temas.length)];
    temaAtual.textContent = t.titulo;
    temaAtual.dataset.origem = t.origem;
    document.getElementById('origemTema').textContent =
      `Tema de treino do Nivora (${t.origem}). Não afirmamos que seja um tema oficial do ENEM.`;
  }
  sortearTema();
  document.getElementById('btnNovoTema').addEventListener('click', sortearTema);

  editor.addEventListener('input', () => {
    const palavras = editor.value.trim().split(/\s+/).filter(Boolean).length;
    contador.textContent = `${palavras} palavras`;
  });

  document.getElementById('btnSalvar').addEventListener('click', async () => {
    const selecionadas = [...document.querySelectorAll('select.comp')].map(s => s.value);
    const preenchidas = selecionadas.filter(v => v !== '').length;
    if (preenchidas !== 0 && preenchidas !== 5) {
      resultado.innerHTML = '<p class="hint">Preencha as cinco competências ou deixe todas em branco.</p>';
      return;
    }
    try {
      const r = await NivoraApi.post('/api/redacao', {
        tema: temaAtual.textContent,
        texto: editor.value,
        competencias: preenchidas ? selecionadas.map(Number) : null
      });
      resultado.innerHTML = `
        <div class="resultado-card" style="margin-top:16px;">
          <p><strong>Redação salva.</strong> ${r.palavras} palavras, ${r.paragrafos} parágrafo(s).</p>
          ${r.total !== null ? `<p><strong>Sua autoavaliação:</strong> ${r.total}/1000</p>` : ''}
          ${r.observacoes.map(o => `<p class="hint">${esc(o)}</p>`).join('')}
          <p class="hint" style="margin-top:8px;">O Nivora só conta palavras e parágrafos; a nota é a que você mesmo atribuiu. Não é correção oficial do ENEM.</p>
        </div>`;
      editor.value = '';
      contador.textContent = '0 palavras';
      document.querySelectorAll('select.comp').forEach(s => { s.value = ''; });
      carregarHistorico();
    } catch (e) {
      resultado.innerHTML = `<p class="hint">${esc(e.message)}</p>`;
    }
  });

  async function carregarHistorico() {
    const lista = await NivoraApi.get('/api/redacao');
    document.getElementById('historicoRedacoes').innerHTML = lista.length
      ? lista.map(r => `
          <div class="assunto-item">
            <div class="assunto-item-topo">
              <span>${esc(r.tema)}</span>
              <span class="hint">${new Date(r.criadaEm).toLocaleDateString('pt-BR')} • ${r.palavras} palavras${r.total !== null ? ` • ${r.total}/1000` : ''}</span>
            </div>
          </div>`).join('')
      : '<p class="hint">Você ainda não salvou nenhuma redação.</p>';
  }
  carregarHistorico();
})();
