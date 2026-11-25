// cursos.js — lista pública de cursos (antes era um script dentro da página; movido por causa da CSP)
  (async function () {
    await NivoraData.carregar();
    const grid = document.getElementById('grid');
    function render(itens) {
      grid.innerHTML = itens.map(c => {
        const areas = Object.entries(c.pesos).sort((a, b) => b[1] - a[1]).slice(0, 2)
          .map(([k]) => ({matematica:'Matemática',natureza:'Natureza',redacao:'Redação',linguagens:'Linguagens',humanas:'Humanas'}[k]));
        return `<div class="curso-card">
          <h3>${NivoraApi.escapar(c.nome)}</h3>
          <p class="hint">Prioridades: ${areas.join(' e ')}</p>
          <a href="/pages/onboarding.html" class="btn btn-outline btn-sm">Montar plano para este curso</a>
        </div>`;
      }).join('');
    }
    render(NivoraData.cursos);
    document.getElementById('buscaCurso').addEventListener('input', e => {
      const termo = e.target.value.toLowerCase();
      render(NivoraData.cursos.filter(c => c.nome.toLowerCase().includes(termo)));
    });
  })();
