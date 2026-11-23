CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

CREATE TABLE curso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    generico BOOLEAN NOT NULL,
    pesos_estimados BOOLEAN NOT NULL,
    peso_matematica INT NOT NULL,
    peso_natureza INT NOT NULL,
    peso_redacao INT NOT NULL,
    peso_linguagens INT NOT NULL,
    peso_humanas INT NOT NULL,
    CONSTRAINT uk_curso_codigo UNIQUE (codigo)
);

CREATE TABLE instituicao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sigla VARCHAR(30) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    CONSTRAINT uk_instituicao_sigla UNIQUE (sigla)
);

CREATE TABLE perfil_estudo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    curso_id BIGINT NULL,
    instituicao_id BIGINT NULL,
    modalidade_ingresso VARCHAR(80) NULL,
    nota_pretendida DECIMAL(6,2) NULL,
    nivel VARCHAR(20) NULL,
    minutos_diarios INT NULL,
    onboarding_concluido BOOLEAN NOT NULL,
    CONSTRAINT uk_perfil_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_perfil_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_perfil_curso FOREIGN KEY (curso_id) REFERENCES curso (id),
    CONSTRAINT fk_perfil_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
);

CREATE TABLE perfil_dificuldade (
    perfil_id BIGINT NOT NULL,
    dificuldade VARCHAR(40) NOT NULL,
    PRIMARY KEY (perfil_id, dificuldade),
    CONSTRAINT fk_dificuldade_perfil FOREIGN KEY (perfil_id) REFERENCES perfil_estudo (id)
);

CREATE TABLE peso_instituicao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curso_id BIGINT NOT NULL,
    instituicao_id BIGINT NOT NULL,
    area VARCHAR(20) NOT NULL,
    peso DECIMAL(4,2) NOT NULL,
    ano INT NOT NULL,
    fonte VARCHAR(200) NOT NULL,
    link VARCHAR(300) NULL,
    estimado BOOLEAN NOT NULL,
    CONSTRAINT uk_peso UNIQUE (curso_id, instituicao_id, area, ano),
    CONSTRAINT fk_peso_curso FOREIGN KEY (curso_id) REFERENCES curso (id),
    CONSTRAINT fk_peso_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
);

CREATE TABLE nota_corte (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curso_id BIGINT NOT NULL,
    instituicao_id BIGINT NOT NULL,
    modalidade VARCHAR(80) NOT NULL,
    ano INT NOT NULL,
    nota DECIMAL(6,2) NOT NULL,
    fonte VARCHAR(200) NOT NULL,
    link VARCHAR(300) NULL,
    estimado BOOLEAN NOT NULL,
    CONSTRAINT uk_nota_corte UNIQUE (curso_id, instituicao_id, modalidade, ano),
    CONSTRAINT fk_nota_curso FOREIGN KEY (curso_id) REFERENCES curso (id),
    CONSTRAINT fk_nota_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
);
