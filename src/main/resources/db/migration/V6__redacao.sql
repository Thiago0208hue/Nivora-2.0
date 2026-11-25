-- Etapa 9: redação. Temas de treino e redações escritas pelos estudantes.
CREATE TABLE tema_redacao (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo  VARCHAR(300) NOT NULL,
    origem  VARCHAR(20)  NOT NULL,
    ano     INT NULL,
    fonte   VARCHAR(200) NULL,
    ativo   BOOLEAN NOT NULL DEFAULT TRUE
);

-- Temas de treino do protótipo. Marcados como EXEMPLO: não afirmamos que sejam temas oficiais do ENEM.
INSERT INTO tema_redacao (titulo, origem) VALUES
 ('Desafios para a valorização de comunidades e povos tradicionais no Brasil', 'EXEMPLO'),
 ('O estigma associado às doenças mentais na sociedade brasileira', 'EXEMPLO'),
 ('Desafios para a formação educacional de surdos no Brasil', 'EXEMPLO'),
 ('Caminhos para combater a desinformação no ambiente digital', 'EXEMPLO'),
 ('O desafio de reduzir as desigualdades regionais no Brasil', 'EXEMPLO');

CREATE TABLE redacao (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT NOT NULL,
    tema        VARCHAR(300) NOT NULL,
    texto       VARCHAR(8000) NOT NULL,
    palavras    INT NOT NULL,
    paragrafos  INT NOT NULL,
    c1          INT NULL,
    c2          INT NULL,
    c3          INT NULL,
    c4          INT NULL,
    c5          INT NULL,
    criada_em   TIMESTAMP NOT NULL,
    CONSTRAINT fk_redacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
CREATE INDEX idx_redacao_usuario ON redacao (usuario_id, id);
