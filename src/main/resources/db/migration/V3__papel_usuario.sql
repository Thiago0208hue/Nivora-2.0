-- Papel do usuário: ESTUDANTE (padrão) ou ADMIN. A promoção a ADMIN é feita direto no banco:
-- UPDATE usuario SET papel = 'ADMIN' WHERE email = 'seu@email.com';
ALTER TABLE usuario ADD COLUMN papel VARCHAR(20) NOT NULL DEFAULT 'ESTUDANTE';
