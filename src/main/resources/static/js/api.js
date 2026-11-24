// api.js — acesso à API REST do Nivora (mesma origem: cookie de sessão + token CSRF)
const NivoraApi = {
  async csrf() {
    const cookie = document.cookie.split('; ').find(c => c.startsWith('XSRF-TOKEN='));
    if (cookie) return decodeURIComponent(cookie.split('=')[1]);
    const resposta = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
    return (await resposta.json()).token;
  },

  async requisicao(metodo, url, corpo) {
    const opcoes = { method: metodo, headers: {}, credentials: 'same-origin' };
    if (metodo !== 'GET') opcoes.headers['X-XSRF-TOKEN'] = await this.csrf();
    if (corpo !== undefined) {
      opcoes.headers['Content-Type'] = 'application/json';
      opcoes.body = JSON.stringify(corpo);
    }
    const resposta = await fetch(url, opcoes);
    if (resposta.status === 204) return null;
    const dados = await resposta.json().catch(() => null);
    if (!resposta.ok) {
      const erro = new Error(dados?.erro || 'Erro inesperado. Tente novamente.');
      erro.status = resposta.status;
      erro.campos = dados?.campos || {};
      throw erro;
    }
    return dados;
  },

  get(url) { return this.requisicao('GET', url); },
  post(url, corpo) { return this.requisicao('POST', url, corpo); },
  put(url, corpo) { return this.requisicao('PUT', url, corpo); },

  // Devolve o usuário logado ou null (401 = sem sessão)
  async usuarioLogado() {
    try {
      return await this.get('/api/auth/me');
    } catch (erro) {
      if (erro.status === 401) return null;
      throw erro;
    }
  },

  // Para páginas internas: sem login, vai para a tela de entrada e interrompe o script da página
  async exigirLogin() {
    const usuario = await this.usuarioLogado();
    if (!usuario) {
      window.location.href = '/pages/login.html';
      return new Promise(() => {});
    }
    return usuario;
  },

  // Apaga o que as páginas ainda guardam no navegador (menos o tema)
  limparDadosLocais() {
    try {
      Object.values(NivoraStorage.KEYS).forEach(chave => {
        if (chave !== NivoraStorage.KEYS.THEME) localStorage.removeItem(chave);
      });
    } catch { /* localStorage indisponível */ }
  },

  async sair() {
    await this.post('/api/auth/logout');
    this.limparDadosLocais();
    window.location.href = '/';
  },

  // Evita que texto digitado pelo usuário vire HTML ao ser colocado em innerHTML
  escapar(texto) {
    return String(texto ?? '').replace(/[&<>"']/g, c => (
      { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]
    ));
  }
};

// Botões "Sair" (qualquer elemento com data-sair)
document.querySelectorAll('[data-sair]').forEach(botao => {
  botao.addEventListener('click', () => NivoraApi.sair());
});
