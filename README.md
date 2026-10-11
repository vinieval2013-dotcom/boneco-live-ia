# Boneco Live IA — protótipo Android

Este projeto é um protótipo inicial para testar a interface e a leitura de respostas em voz alta no Android.

## O que esta versão faz
- Abre uma tela chamada Boneco Live IA.
- Permite digitar um comentário de teste.
- Gera uma resposta demonstrativa baseada em palavras simples.
- Lê a resposta em português usando a síntese de voz do Android (TTS).

## O que ainda não faz
- Não lê automaticamente comentários de uma LIVE do TikTok.
- Não usa uma IA online para responder.
- Não se conecta à conta do TikTok nem envia mensagens/presentes.
- Ainda precisa ser compilado e instalado/testado num dispositivo Android real.

Não há como garantir “zero erros” sem compilar e testar no dispositivo. Este projeto é o ponto inicial funcional para testar a tela e a voz.

## Compilar APK pelo GitHub Actions
1. Envie todos os arquivos deste projeto para a raiz do seu repositório GitHub.
2. Abra a aba **Actions**.
3. Se o GitHub pedir, permita a execução de workflows.
4. Execute o workflow **Compilar APK Android** (ou faça um commit na branch `main`).
5. Abra a execução concluída e, na área **Artifacts**, baixe `boneco-live-ia-debug-apk`.
6. O arquivo baixado é um APK de depuração. No Android, abra-o e permita a instalação se o sistema solicitar.

## Segurança
Nunca coloque senhas, cookies, tokens ou chaves privadas no código ou no repositório.
