-- Dados iniciais vindos do protótipo original (data/cursos.json e data/universidades.json).
-- Os pesos dos cursos são ESTIMATIVAS do Nivora, não pesos oficiais de edital.
-- peso_instituicao e nota_corte ficam vazias até haver dado com fonte e ano.

INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('cc', 'Ciência da Computação', FALSE, TRUE, 5, 4, 4, 3, 2);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('si', 'Sistemas de Informação', FALSE, TRUE, 5, 3, 3, 3, 2);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('es', 'Engenharia de Software', FALSE, TRUE, 5, 4, 3, 3, 2);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('ec', 'Engenharia da Computação', FALSE, TRUE, 5, 5, 3, 2, 2);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('ads', 'Análise e Desenvolvimento de Sistemas', FALSE, TRUE, 4, 3, 3, 3, 2);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('medicina', 'Medicina', FALSE, TRUE, 5, 5, 4, 3, 2);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('direito', 'Direito', FALSE, TRUE, 2, 2, 5, 5, 5);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('enfermagem', 'Enfermagem', FALSE, TRUE, 2, 5, 4, 3, 3);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('psicologia', 'Psicologia', FALSE, TRUE, 2, 3, 4, 4, 5);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('administracao', 'Administração', FALSE, TRUE, 4, 2, 4, 4, 3);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('arquitetura', 'Arquitetura', FALSE, TRUE, 4, 3, 4, 3, 3);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('civil', 'Engenharia Civil', FALSE, TRUE, 5, 5, 3, 2, 2);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('licenciaturas', 'Licenciaturas', FALSE, TRUE, 3, 3, 4, 4, 4);
INSERT INTO curso (codigo, nome, generico, pesos_estimados, peso_matematica, peso_natureza, peso_redacao, peso_linguagens, peso_humanas) VALUES ('outros', 'Outros', TRUE, TRUE, 3, 3, 3, 3, 3);

INSERT INTO instituicao (sigla, nome) VALUES ('ufrn', 'UFRN');
INSERT INTO instituicao (sigla, nome) VALUES ('ifrn', 'IFRN');
INSERT INTO instituicao (sigla, nome) VALUES ('ufpe', 'UFPE');
INSERT INTO instituicao (sigla, nome) VALUES ('ufpb', 'UFPB');
INSERT INTO instituicao (sigla, nome) VALUES ('ufc', 'UFC');
INSERT INTO instituicao (sigla, nome) VALUES ('ufba', 'UFBA');
INSERT INTO instituicao (sigla, nome) VALUES ('usp', 'USP');
INSERT INTO instituicao (sigla, nome) VALUES ('unicamp', 'UNICAMP');
INSERT INTO instituicao (sigla, nome) VALUES ('ufrj', 'UFRJ');
INSERT INTO instituicao (sigla, nome) VALUES ('outras', 'Outras');
