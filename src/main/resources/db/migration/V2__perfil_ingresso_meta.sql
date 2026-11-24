-- O onboarding coleta a forma de ingresso (SISU, ProUni...) e a meta do estudante.
ALTER TABLE perfil_estudo ADD COLUMN forma_ingresso VARCHAR(40) NULL;
ALTER TABLE perfil_estudo ADD COLUMN meta VARCHAR(60) NULL;
