# Servidor de eventos TikTok LIVE — Lula x Bolsonaro

Este serviço recebe eventos de uma LIVE pública usando `tiktok-live-connector` e os retransmite por Socket.IO para o APK.

## Implantar pelo celular

1. Abra https://render.com e entre/crie uma conta.
2. Escolha **New → Web Service** e conecte o repositório `vinieval2013-dotcom/boneco-live-ia`.
3. Nas configurações, defina **Root Directory** como `live-server`.
4. Defina **Build Command** como `npm install` e **Start Command** como `npm start`.
5. Crie o serviço e aguarde o deploy. Copie o endereço HTTPS fornecido.
6. No APK, abra **Conectar a TikTok LIVE**, informe o usuário que está AO VIVO (sem @), cole o endereço HTTPS e toque em **Conectar à LIVE**.

O endpoint `/api/health` mostra se o servidor está funcionando.

## Eventos retransmitidos

- `tiktok_gift`: nome do presente, valor, quantidade, ID e apelido do espectador.
- `tiktok_chat`: comentário e apelido.
- `tiktok_like`: curtidas.
- `tiktok_connected`, `tiktok_disconnected` e `tiktok_error`: estado da conexão.

## Importante

- O plano gratuito pode suspender serviços sem uso; aguarde o serviço iniciar novamente antes de conectar.
- A biblioteca de conexão é comunitária/não oficial e pode parar de funcionar se o TikTok mudar os serviços.
- A conta precisa estar AO VIVO e acessível publicamente.
- Não coloque senhas, cookies ou tokens pessoais no APK ou no repositório.
- Os presentes do modo de teste não enviam presentes reais nem cobram moedas.

A mecânica de corrida foi inspirada em [TikTok Live Games](https://github.com/vamnguyen/tiktok-live-games), cujo README e `package.json` declaram licença MIT. O servidor desta pasta é uma implementação compacta própria para este aplicativo.
