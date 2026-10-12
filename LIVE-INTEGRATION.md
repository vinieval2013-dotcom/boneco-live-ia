# Créditos e integração da corrida LIVE

A tela de corrida deste aplicativo foi adaptada para Android a partir da ideia e das mecânicas documentadas no projeto **TikTok Live Games**, de vamnguyen:
https://github.com/vamnguyen/tiktok-live-games

O projeto de referência declara licença MIT no seu `package.json`. Esta implementação Android é uma adaptação própria da mecânica descrita no README e no motor de corrida, e não uma cópia integral do servidor original.

## Mecânicas adaptadas

- Duas pistas: Lula (verde) e Jair Bolsonaro (azul).
- Presentes com valores de teste de 1, 5, 10 e 99 moedas.
- Avanço proporcional ao valor do presente, inspirado na fórmula `5 + 3 × √(valor)`.
- Estados de espera, contagem regressiva, corrida, chegada e reinício.
- Linha de chegada, porcentagem de progresso e feed das últimas jogadas.
- Fotos de candidatos carregadas de URLs da Wikimedia Commons.

## Limitação importante

O APK atual contém a tela de corrida e um modo de teste manual. **Ele ainda não recebe presentes reais do TikTok LIVE.** O projeto de referência usa um servidor Node.js, Express, Socket.IO e `tiktok-live-connector`; esses serviços não rodam automaticamente dentro do APK Android.

Para ativar presentes reais será necessário implantar e testar um servidor acessível pela internet e ligar a tela do jogo aos eventos desse servidor. A biblioteca de conexão com o TikTok é não oficial e pode deixar de funcionar se o TikTok alterar seu serviço. Não inserir senhas, cookies ou tokens pessoais no código do aplicativo.

## Como testar

1. Instale o APK de teste quando a compilação estiver concluída.
2. Escolha Lula ou Bolsonaro.
3. Toque em um dos presentes de teste para movimentar o corredor.
4. Toque em **Nova rodada** para reiniciar.

Os presentes da tela são simulações e não enviam presentes nem cobram moedas do TikTok.
