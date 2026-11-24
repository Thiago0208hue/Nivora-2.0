// perfil.js — perfil do estudante: conversa entre o formato do frontend e o da API
const NivoraPerfil = {
  NIVEIS: { INICIANTE: 'Iniciante', BASICO: 'Básico', INTERMEDIARIO: 'Intermediário', AVANCADO: 'Avançado' },
  TEMPOS: { 45: 'Menos de 1 hora', 60: '1 hora', 120: '2 horas', 180: '3 horas', 240: '4 horas', 300: '5 horas ou mais' },

  nivelParaApi(rotulo) {
    return Object.keys(this.NIVEIS).find(chave => this.NIVEIS[chave] === rotulo) || null;
  },

  // O frontend usa "id" para identificar curso e faculdade; na API são codigo e sigla
  async listarCursos() {
    const lista = await NivoraApi.get('/api/cursos');
    return lista.map(c => ({ id: c.codigo, nome: c.nome, pesos: c.pesos, pesosEstimados: c.pesosEstimados }));
  },

  async listarFaculdades() {
    const lista = await NivoraApi.get('/api/instituicoes');
    return lista.map(i => ({ id: i.sigla, nome: i.nome }));
  },

  // Perfil no formato que as telas usam, ou null se o onboarding ainda não foi concluído
  async carregar() {
    const [p, cursos] = await Promise.all([NivoraApi.get('/api/perfil'), this.listarCursos()]);
    if (!p.onboardingConcluido || !p.cursoCodigo) return null;
    const perfil = {
      curso: cursos.find(c => c.id === p.cursoCodigo),
      faculdade: p.instituicaoSigla ? { id: p.instituicaoSigla, nome: p.instituicaoNome } : null,
      ingresso: p.formaIngresso,
      modalidade: p.modalidadeIngresso,
      tempoMin: p.minutosDiarios,
      tempoEstudo: this.TEMPOS[p.minutosDiarios] || `${p.minutosDiarios} min`,
      nivel: this.NIVEIS[p.nivel],
      dificuldades: p.dificuldades || [],
      meta: p.meta,
      notaDesejada: p.notaPretendida
    };
    this.guardarCopia(perfil);
    return perfil;
  },

  async salvar(estado) {
    await NivoraApi.put('/api/perfil', {
      cursoCodigo: estado.curso.id,
      instituicaoSigla: estado.faculdade.id,
      formaIngresso: estado.ingresso,
      modalidadeIngresso: estado.modalidade,
      meta: estado.meta,
      notaPretendida: estado.notaDesejada,
      nivel: this.nivelParaApi(estado.nivel),
      minutosDiarios: estado.tempoMin,
      dificuldades: estado.dificuldades,
      onboardingConcluido: true
    });
    return this.carregar();
  },

  // Temporário: as telas Plano, Assuntos, Treinar etc. ainda leem o perfil do localStorage.
  // Esta cópia some quando elas forem ligadas à API (etapas 5 a 9).
  guardarCopia(perfil) {
    try { NivoraStorage.set(NivoraStorage.KEYS.PERFIL, perfil); } catch { /* sem localStorage */ }
  }
};
