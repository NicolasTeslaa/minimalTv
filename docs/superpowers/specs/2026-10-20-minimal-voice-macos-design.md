# Minimal Voice para macOS — Especificação

## Objetivo

Criar uma aplicação macOS residente no Mac Mini que escuta uma frase iniciada pelo usuário, transcreve o áudio localmente em português, interpreta um conjunto seguro de comandos e controla a Android TV por ADB direto na rede local.

## Regras aprovadas

- O áudio e a transcrição permanecem no Mac; não há serviço de voz em nuvem.
- A escuta começa e termina por um botão explícito; não haverá microfone sempre aberto nem wake word na primeira versão.
- Comandos não reconhecidos não executam ações.
- Fechar um aplicativo significa encerrá-lo com ADB e retornar ao Minimal TV.
- Comandos de conteúdo específico abrem o aplicativo correspondente e informam que a busca deve ser feita manualmente.
- A aplicação não depende de Alexa, Home Assistant ou assinatura.

## Arquitetura

O app será um alvo macOS em SwiftUI, com o núcleo de domínio separado da interface:

1. `SpeechCapture`: captura PCM pelo `AVAudioEngine` e solicita permissão de microfone.
2. `LocalTranscriber`: adapta o Whisper local e retorna texto em português.
3. `CommandParser`: normaliza acentos/caixa e converte texto em uma enumeração fechada de intenções.
4. `TvCommandExecutor`: traduz intenções para comandos ADB, com reconexão e timeout.
5. `MinimalVoiceApp`: apresenta estado de escuta, transcrição, resultado e erros.

O primeiro protótipo poderá usar um adaptador de processo para o executável `adb`; a interface do domínio não dependerá do processo para permitir testes com um executor falso. O modelo Whisper e a estratégia final de empacotamento serão escolhidos após a prova técnica, mantendo o áudio local.

## Comandos

- Energia: ligar e desligar a televisão.
- Aplicativos: abrir/fechar Netflix, HBO Max, Prime Video e YouTube; retornar ao Minimal TV.
- Volume: aumentar, diminuir, aumentar/diminuir por valor, definir valor quando suportado, mutar e desmutar.
- Conteúdo: abrir o aplicativo mencionado e produzir uma mensagem de busca manual.

Os identificadores de pacote, serial ADB e comportamento de volume serão configuráveis, não hard-coded no parser. A configuração inicial usará a TV já autorizada do usuário.

## Falhas e segurança

- TV indisponível: mostrar erro acionável e não repetir comandos destrutivos automaticamente.
- ADB não autorizado: mostrar instrução para autorizar a chave do Mac.
- Microfone negado: mostrar instrução de Privacidade e Segurança.
- Transcrição vazia ou ambígua: não executar nada.
- Aplicativo desconhecido: informar que o app ainda não está configurado.
- Uma execução por vez; o botão fica bloqueado durante transcrição/execução.

## Critérios de aceite

- Uma frase curta em português é transcrita localmente e exibida.
- `abrir Netflix` gera exatamente o comando de lançamento configurado.
- `fechar Netflix` gera encerramento e retorno ao Minimal TV.
- Comandos de volume geram os keyevents/ações esperados.
- Frases não reconhecidas não invocam ADB.
- O app compila como alvo macOS quando o Xcode estiver disponível.

## Fora do primeiro ciclo

- Wake word contínua.
- Busca automática dentro de Netflix/HBO.
- Alexa, Home Assistant, cloud speech ou backend remoto.
