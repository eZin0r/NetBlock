🛡️ NetBlock

NetBlock é um aplicativo Android de bloqueio seletivo de acesso à internet por aplicativo, baseado em VPN local (VpnService).
Ele permite controlar exatamente quais apps podem ou não acessar a rede, sem necessidade de root.

O foco do projeto é oferecer controle rápido, confiável e transparente, com alternância em tempo real e impacto mínimo no sistema.

🚀 Principais recursos

🔒 Bloqueio de internet por aplicativo

🔁 Ativação e desativação instantânea da VPN

🎯 Modo filtro (bloqueia apenas os apps selecionados)

📡 Implementação baseada em VpnService (sem servidores externos)

🔔 Notificação persistente com ações rápidas:

Ativar / Desativar VPN

Bloquear / Liberar apps

🟢🔴 Indicadores visuais de estado (ativo / inativo)

🪟 Botão flutuante (overlay) para controle rápido

⚡ Reinício automático da VPN ao alterar o filtro

📱 Compatível com Android moderno (API 26+)

🧠 Como funciona

O NetBlock cria uma VPN local que intercepta o tráfego de rede do dispositivo.
Com isso, é possível decidir quais aplicativos:

Entram na VPN → ficam sem acesso à internet

Ficam fora da VPN → continuam com acesso normal

Nenhum dado é enviado para servidores externos.

🔐 Privacidade

❌ Sem coleta de dados

❌ Sem servidores intermediários

✅ Todo o processamento ocorre localmente no dispositivo

🛠️ Tecnologias utilizadas

Kotlin

Android VpnService

Foreground Service

Notification Actions

Overlay (SYSTEM_ALERT_WINDOW)

SharedPreferences

📌 Objetivo do projeto

O NetBlock foi criado com foco em:

Estudo avançado de VpnService

Controle fino de rede por app

Criação de soluções leves, seguras e sem root

Base para automações e controles rápidos de conectividade
