// login.js — tela de entrada
(async function () {
  if (await NivoraApi.usuarioLogado().catch(() => null)) {
    window.location.href = '/pages/dashboard.html';
    return;
  }
  const form = document.getElementById('formLogin');
  const erro = document.getElementById('erro');
  const botao = document.getElementById('btnEntrar');

  form.addEventListener('submit', async evento => {
    evento.preventDefault();
    erro.hidden = true;
    botao.disabled = true;
    try {
      await NivoraApi.post('/api/auth/login', {
        email: form.email.value,
        senha: form.senha.value
      });
      NivoraApi.limparDadosLocais();
      const perfil = await NivoraApi.get('/api/perfil');
      window.location.href = perfil.onboardingConcluido ? '/pages/dashboard.html' : '/pages/onboarding.html';
    } catch (e) {
      erro.textContent = e.message;
      erro.hidden = false;
      botao.disabled = false;
    }
  });
})();
