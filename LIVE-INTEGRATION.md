# Corrida LIVE — mecânicas e integração

A tela Android foi adaptada a partir das mecânicas descritas pelo projeto **TikTok Live Games**:
https://github.com/vamnguyen/tiktok-live-games

O projeto de referência declara licença MIT no README e no `package.json`. Esta versão utiliza uma implementação compacta própria do servidor e uma tela Android própria, com atribuição à referência.

## O que está no aplicativo

- Duas pistas: Lula (verde) e Jair Bolsonaro (azul).
- Corrida com linha de chegada e progresso percentual.
- Presentes de teste de 1, 5, 10 e 99 moedas.
- Avanço proporcional ao valor do presente, inspirado na fórmula `5 + 3 × √(valor)`.
- Contagem regressiva, corrida, chegada, feed de eventos e reinício.
- Conexão opcional por Socket.IO para receber eventos reais, depois de implantar o servidor.
- Fotos carregadas de URLs da Wikimedia Commons.

## Como ativar presentes reais

O APK não hospeda um servidor Node.js dentro de si. O código do servidor está na pasta `live-server/` e precisa ficar acessível pela internet:

1. Crie um serviço Web Node no Render (https://render.com) ligado a este repositório.
2. Configure **Root Directory** como `live-server`, **Build Command** como `npm install` e **Start Command** como `npm start`.
3. Depois que o deploy terminar, copie a URL HTTPS do serviço.
4. Abra o APK, preencha o nome de usuário que está AO VIVO (sem @) e a URL HTTPS do servidor, e toque em **Conectar à LIVE**.

O servidor disponibiliza `/api/health` para verificar se está ativo. O serviço de hospedagem gratuito pode dormir quando não está em uso.

## Mapeamento atual de presentes

O jogo mapeia alguns nomes de presentes conhecidos para as pistas Lula e Bolsonaro. Se um presente real não estiver no mapeamento, ele aparece no feed como não configurado; o nome pode ser acrescentado no arquivo `app/src/main/assets/race/index.html`.

## Limitações e segurança

- A conexão de eventos usa uma biblioteca comunitária/não oficial; mudanças no TikTok podem interrompê-la.
- A conta do TikTok precisa estar transmitindo e acessível publicamente.
- Não inserir senhas, cookies ou tokens pessoais no aplicativo ou no repositório.
- Os botões de presentes de teste não enviam presentes nem cobram moedas.
- O APK precisa ser recompilado para incluir mudanças futuras no código.
