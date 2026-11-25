CREATE TABLE questao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    area VARCHAR(20) NOT NULL,
    disciplina VARCHAR(40) NOT NULL,
    assunto VARCHAR(80) NOT NULL,
    dificuldade VARCHAR(10) NULL,
    ano INT NULL,
    origem VARCHAR(20) NOT NULL,
    fonte VARCHAR(200) NULL,
    enunciado VARCHAR(6000) NOT NULL,
    resolucao VARCHAR(3000) NULL,
    ativa BOOLEAN NOT NULL
);
CREATE INDEX idx_questao_filtros ON questao (area, disciplina, assunto);

CREATE TABLE alternativa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questao_id BIGINT NOT NULL,
    letra VARCHAR(1) NOT NULL,
    texto VARCHAR(1000) NOT NULL,
    correta BOOLEAN NOT NULL,
    CONSTRAINT uk_alternativa_letra UNIQUE (questao_id, letra),
    CONSTRAINT fk_alternativa_questao FOREIGN KEY (questao_id) REFERENCES questao (id)
);

CREATE TABLE simulado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    area VARCHAR(20) NULL,
    criado_em DATETIME(6) NOT NULL,
    finalizado_em DATETIME(6) NULL,
    CONSTRAINT fk_simulado_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE TABLE simulado_questao (
    simulado_id BIGINT NOT NULL,
    ordem INT NOT NULL,
    questao_id BIGINT NOT NULL,
    PRIMARY KEY (simulado_id, ordem),
    CONSTRAINT fk_simq_simulado FOREIGN KEY (simulado_id) REFERENCES simulado (id),
    CONSTRAINT fk_simq_questao FOREIGN KEY (questao_id) REFERENCES questao (id)
);

CREATE TABLE resposta_questao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    questao_id BIGINT NOT NULL,
    simulado_id BIGINT NULL,
    letra_escolhida VARCHAR(1) NOT NULL,
    correta BOOLEAN NOT NULL,
    respondida_em DATETIME(6) NOT NULL,
    CONSTRAINT fk_resposta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_resposta_questao FOREIGN KEY (questao_id) REFERENCES questao (id),
    CONSTRAINT fk_resposta_simulado FOREIGN KEY (simulado_id) REFERENCES simulado (id)
);
CREATE INDEX idx_resposta_usuario_questao ON resposta_questao (usuario_id, questao_id);
