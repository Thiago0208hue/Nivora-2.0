// inicio.js — página inicial: se já houver sessão, os botões levam ao painel
(async function () {
  let usuario = null;
  try { usuario = await NivoraApi.usuarioLogado(); } catch { return; }
  if (!usuario) return;
  const entrar = document.getElementById('linkEntrar');
  const cta = document.getElementById('linkCta');
  const heroCta = document.getElementById('heroCta');
  if (entrar) { entrar.textContent = 'Meu painel'; entrar.href = '/pages/dashboard.html'; }
  if (cta) { cta.textContent = 'Ver meu plano'; cta.href = '/pages/dashboard.html'; }
  if (heroCta) { heroCta.textContent = 'Ver meu plano'; heroCta.href = '/pages/dashboard.html'; }
})();
