// configuracoes.js — Configurações: tema, conta e dados
(async function () {
  document.getElementById('btnTemaConfig').addEventListener('click', () => {
    document.getElementById('themeToggle').click();
  });

  const esc = NivoraApi.escapar;
  const usuario = await NivoraApi.exigirLogin();
  const perfil = await NivoraPerfil.carregar();

  document.getElementById('perfilResumo').innerHTML = `
    <p>👤 <strong>Conta:</strong> ${esc(usuario.nome)} (${esc(usuario.email)})</p>
    ${perfil ? `
      <p>🎓 <strong>Curso:</strong> ${esc(perfil.curso.nome)}</p>
      <p>🏫 <strong>Faculdade:</strong> ${esc(perfil.faculdade?.nome)}</p>
      <p>🎯 <strong>Meta:</strong> ${esc(perfil.meta)}</p>
    ` : '<p class="hint">Você ainda não concluiu o onboarding.</p>'}
  `;

  document.getElementById('btnResetar').addEventListener('click', () => {
    if (!confirm('Isso vai apagar o progresso salvo neste navegador (desempenho, redações, metas e XP). Seu perfil e sua conta continuam. Deseja continuar?')) return;
    NivoraApi.limparDadosLocais();
    window.location.href = '/pages/dashboard.html';
  });
})();
