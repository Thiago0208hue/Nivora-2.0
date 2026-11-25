#!/bin/sh
# MODELO (não testado). Backup diário do MySQL, guardando os 14 mais recentes.
# Agende com cron:  0 3 * * * /opt/nivora/backup.sh
# Credenciais: crie ~/.my.cnf (chmod 600) com [client] user= e password=, para não deixar senha neste arquivo.
set -eu
DESTINO=/var/backups/nivora
mkdir -p "$DESTINO"
mysqldump --single-transaction nivora | gzip > "$DESTINO/nivora-$(date +%F).sql.gz"
ls -1t "$DESTINO"/nivora-*.sql.gz | tail -n +15 | xargs -r rm --
# Importante: copie também para FORA da máquina (outro computador ou nuvem); backup só no mesmo disco não protege contra perda do disco.
