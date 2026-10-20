# Minimal Voice

Aplicação macOS local para controlar o Minimal TV por voz. A transcrição usa o framework Speech da Apple com `requiresOnDeviceRecognition = true`; nenhum áudio ou comando é enviado para a nuvem.

## Executar no Mac Mini

```bash
cd macos/MinimalVoice
./scripts/build-and-run.sh
```

Para instalar em um caminho fixo e evitar novos pedidos de permissão a cada build:

```bash
./scripts/install-and-run.sh
```

Esse comando instala em `~/Applications/Minimal Voice.app`. O macOS normalmente mantém as permissões para esse bundle; uma atualização que altere a assinatura ou o bundle pode pedir confirmação novamente.

Na primeira execução, autorize o microfone e o reconhecimento de voz. O Mac precisa estar na mesma rede da TV e o ADB precisa estar autorizado.

O app procura automaticamente a primeira TV ADB autorizada. Isso funciona mesmo quando a TV aparece com um serial de descoberta de rede e o IP muda. Para fixar um serial:

```bash
defaults write com.minimaltv.voice tvSerial 192.168.1.9:36661
```

## Comandos

O botão inicia e para a escuta. Exemplos: `abrir Netflix`, `fechar HBO Max`, `ligar televisão`, `desligar TV`, `aumentar volume`, `abaixar volume em 10`, `volume em 30`, `mutar televisão` e `voltar para o Minimal TV`.

Comandos de conteúdo como `abrir Supernatural na HBO` abrem o aplicativo e informam que a busca deve ser feita manualmente.

## Verificação

Como este ambiente tem apenas o Command Line Tools, o teste do núcleo é um executable spec:

```bash
swift run MinimalVoiceCoreSpec
swift build --product MinimalVoiceApp
```
