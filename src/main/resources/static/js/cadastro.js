// cadastro.js — criação de conta
(async function () {
  if (await NivoraApi.usuarioLogado().catch(() => null)) {
    window.location.href = '/pages/dashboard.html';
    return;
  }
  const form = document.getElementById('formCadastro');
  const erro = document.getElementById('erro');
  const botao = document.getElementById('btnCadastrar');

  form.addEventListener('submit', async evento => {
    evento.preventDefault();
    erro.hidden = true;
    botao.disabled = true;
    const email = form.email.value;
    const senha = form.senha.value;
    try {
      await NivoraApi.post('/api/auth/cadastro', { nome: form.nome.value, email, senha });
      await NivoraApi.post('/api/auth/login', { email, senha });
      NivoraApi.limparDadosLocais();
      window.location.href = '/pages/onboarding.html';
    } catch (e) {
      const detalhes = Object.values(e.campos || {}).join(' ');
      erro.textContent = detalhes || e.message;
      erro.hidden = false;
      botao.disabled = false;
    }
  });
})();
