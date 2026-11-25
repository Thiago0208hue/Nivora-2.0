# Publicação (Etapa 12)

> **Nada aqui foi contratado nem executado.** Os arquivos em `deploy/` são **modelos não testados**. Preços e limites foram consultados em 25/11/2026 e mudam: confirme nas páginas oficiais antes de decidir.

## 1. Onde hospedar (decisão sua)
O Nivora precisa de uma máquina ligada o tempo todo com Java 21 e MySQL 8. O DuckDNS **não hospeda nada**: só aponta um nome (`algo.duckdns.org`) para o IP dessa máquina; o serviço é gratuito, e a página dele não informa limites de uso.

| Opção | Custo | Prós | Contras |
|---|---|---|---|
| **A. Computador em casa** (PC ou mini-PC) | R$ 0 de hospedagem + energia | sem mensalidade; bom para a banca do TCC | cai se a internet ou a luz cair; é preciso abrir as portas 80 e 443 no roteador; alguns provedores bloqueiam essas portas ou usam IP compartilhado (CGNAT), o que impede o acesso de fora |
| **B. Oracle Cloud "Always Free"** | grátis dentro dos limites | máquina na nuvem com IP público; MySQL gerenciado gratuito também existe | exige cadastro com cartão; a máquina Arm gratuita tem **2 OCPUs e 12 GB** (a Oracle reduziu esse limite); pode faltar capacidade para criar; instâncias com uso muito baixo podem ser recuperadas pela Oracle; recursos ficam presos à "home region" |
| **C. VPS pago (ex.: Hetzner)** | planos de 2 vCPU/4 GB a partir de **€ 5,99/mês sem impostos** (CX23); Arm (CAX11) € 6,49 | estável, simples, IP fixo | pagamento em euro; no momento da consulta os planos baratos apareciam **indisponíveis**; o IVA depende do país |

**Recomendação para o TCC:** começar pela **A** ou pela **B** (custo zero) para o período de avaliação; migrar para **C** só se precisar de estabilidade. Servidores de 1 GB de memória são apertados para Java + MySQL: use pelo menos 2 GB.

Fontes: [Oracle: recursos Always Free](https://docs.oracle.com/iaas/Content/FreeTier/resourceref.htm) · [Hetzner: planos Cost-Optimized](https://www.hetzner.com/cloud/cost-optimized) · [DuckDNS: especificação](https://www.duckdns.org/spec.jsp)

## 2. Passo a passo (qualquer opção, Linux)
1. **Nome:** crie o subdomínio no DuckDNS e aponte para o IP público da máquina. Para IP que muda (casa), atualize periodicamente com `https://www.duckdns.org/update?domains=SEU&token=SEU_TOKEN`.
2. **Instalar:** Java 21 (JDK ou JRE), MySQL 8 e Caddy.
3. **Banco:** criar o banco e um usuário **só com acesso a ele** (não usar `root`):
   ```sql
   CREATE DATABASE nivora CHARACTER SET utf8mb4;
   CREATE USER 'nivora'@'localhost' IDENTIFIED BY 'senha-forte';
   GRANT ALL ON nivora.* TO 'nivora'@'localhost';
   ```
4. **Gerar o pacote** (no seu computador): `./mvnw package` → `target/nivora-0.0.1-SNAPSHOT.jar`; copiar para `/opt/nivora/nivora.jar`.
5. **Segredos:** copiar `deploy/nivora.env.example` para `/etc/nivora/nivora.env`, preencher e `chmod 600`.
6. **Serviço:** copiar `deploy/nivora.service` e `systemctl enable --now nivora`. O perfil `prod` liga o cookie seguro, escuta só em `127.0.0.1` e confia no cabeçalho do proxy.
7. **HTTPS:** copiar `deploy/Caddyfile` (com seu subdomínio) para `/etc/caddy/Caddyfile` e `systemctl reload caddy`. O Caddy emite o certificado sozinho.
8. **Firewall:** liberar só 80, 443 e SSH. **Nunca** abrir 8080 nem 3306.
9. **Primeiro acesso:** criar sua conta pelo site e virar admin por SQL (ver README).

## 3. Variáveis de ambiente
`SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USER`, `DB_PASSWORD`. Nenhuma senha vai para o GitHub (`.env*` está no `.gitignore`).

## 4. Backup
- `deploy/backup.sh` faz `mysqldump` diário e guarda 14 cópias.
- **Copie para fora da máquina** (outro computador ou nuvem). Backup no mesmo disco não protege contra perda do disco.
- **Teste a restauração** pelo menos uma vez, em outra máquina. Backup nunca restaurado não é backup confiável.
- Os dados incluem textos de redação (dado pessoal): proteja os arquivos de backup (`chmod 600`, disco/nuvem com acesso restrito).

## 5. Monitoramento (simples e gratuito)
- Logs: `journalctl -u nivora -f`. Erros inesperados são registrados pelo `TratadorDeErros`.
- Verificação de disponibilidade: um serviço externo de "ping" gratuito apontando para `https://SEU.duckdns.org/` e avisando por e-mail.
- Espaço em disco e memória: conferir manualmente com `df -h` e `free -h` no período de avaliação.
- Não há Actuator nem métricas técnicas no projeto ainda; avaliar só se houver necessidade.

## 6. Antes de abrir para participantes
- [ ] `./mvnw test` passando com JDK 21 (hoje **nunca executado** com sucesso).
- [ ] Exclusão de conta/redações e política de privacidade (LGPD).
- [ ] Fontes hospedadas localmente (hoje vêm do Google Fonts).
- [ ] Questões oficiais com ano e fonte cadastradas; pesos e notas de corte reais (ou aviso claro de estimativa).
- [ ] Aprovação do comitê de ética e TCLE, se houver coleta com participantes.
- [ ] Backup testado e HTTPS funcionando.

## 7. O que você precisa decidir
1. Opção de hospedagem (A, B ou C)? Para C, aceita pagar em euro?
2. Quanto tempo o sistema ficará no ar (só a avaliação ou depois)?
3. Há restrição de local para armazenar dados de participantes (por exemplo, exigência do comitê de ética sobre servidores no exterior)? Isso pode descartar as opções B e C.
