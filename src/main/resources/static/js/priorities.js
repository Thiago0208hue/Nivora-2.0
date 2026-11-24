// priorities.js — apoio às telas: agrega o desempenho por área.
// O cálculo das prioridades agora é feito no servidor (PersonalizacaoService, POST /api/personalizacao/prioridades).
const NivoraPriorities = {
  // Agrega o desempenho (por assunto) em desempenho por área, usando o mapeamento de assuntos.json
  agregarDesempenhoPorArea(desempenho = {}, assuntosData = []) {
    const porArea = {};
    assuntosData.forEach(grupo => {
      grupo.assuntos.forEach(nome => {
        const d = desempenho[nome];
        if (!d || !d.respondidas) return;
        const a = porArea[grupo.area] || { respondidas: 0, acertos: 0 };
        a.respondidas += d.respondidas;
        a.acertos += d.acertos;
        porArea[grupo.area] = a;
      });
    });
    return porArea;
  }
};
