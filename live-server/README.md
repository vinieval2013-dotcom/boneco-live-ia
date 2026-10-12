# Servidor de eventos TikTok LIVE — Lula x Bolsonaro

Este serviço opcional recebe eventos de uma LIVE pública usando `tiktok-live-connector` e os retransmite por Socket.IO para o APK.

## Implantar gratuitamente pelo celular

1. Abra o repositório no GitHub e confirme que os últimos commits foram enviados.
2. Entre em https://render.com e crie uma conta ou entre na sua conta.
3. Escolha **New → Blueprint** e conecte este repositório.
4. O Render deve detectar o arquivo `render.yaml` na raiz. Confirme a criação do serviço `lula-bolsonaro-live`.
5. Aguarde o deploy e copie o endereço HTTPS fornecido, por exemplo `https://lula-bolsonaro-live.onrender.com`.
6. No APK, abra **Conectar a TikTok LIVE**, informe o nome de usuário que está ao vivo (sem @) e cole o endereço HTTPS.
7. Toque em **Conectar à LIVE**.

O endpoint `/api/health` mostra se o servidor está em execução.

## Eventos

O servidor envia `tiktok_gift` com `giftName`, `giftValue`, `repeatCount`, `giftId` e `user.nickname`. Também retransmite chat, curtidas e estado da conexão.

## Importante

- O plano gratuito de hospedagem pode dormir quando não é usado; nesse caso, espere o serviço acordar antes de conectar.
- A biblioteca usada é comunitária/não oficial e pode parar de funcionar quando o TikTok mudar seus serviços.
- A conta precisa estar ao vivo e acessível publicamente.
- Não coloque senhas, cookies ou tokens pessoais no APK ou neste repositório.
- O APK mantém um modo de teste que não envia presentes reais nem compra moedas.
- A conexão com eventos do TikTok é separada do APK: a interface é empacotada no app, mas este servidor precisa estar implantado na internet.

A arquitetura de corrida foi inspirada em [TikTok Live Games](https://github.com/vamnguyen/tiktok-live-games), cujo README e `package.json` declaram licença MIT. Este servidor é uma implementação própria e compacta para este aplicativo.
