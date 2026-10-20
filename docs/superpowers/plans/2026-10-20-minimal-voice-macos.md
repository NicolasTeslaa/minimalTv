# Minimal Voice macOS Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir um app macOS residente que transcreve voz localmente e controla a TV Android via ADB.

**Architecture:** Um núcleo Swift Package testável conterá parser, intenções e executor ADB. Um alvo SwiftUI macOS conectará captura de áudio, transcrição local e estado visual ao núcleo. O primeiro ciclo usará um adaptador ADB por processo e deixará o motor Whisper atrás de um protocolo substituível.

**Tech Stack:** Swift 6, Swift Package Manager, SwiftUI, AVFoundation, XCTest, processo `adb`, Whisper local em adaptador isolado.

**Spec:** `docs/superpowers/specs/2026-10-20-minimal-voice-macos-design.md`

## Global Constraints

- Áudio e transcrição devem permanecer no Mac.
- A escuta só ocorre enquanto o usuário mantiver o estado iniciado pelo botão.
- Comandos não reconhecidos nunca executam ADB.
- Fechar app encerra o pacote e retorna ao `com.minimaltv.launcher`.
- Conteúdo específico apenas abre o app e informa busca manual.
- O núcleo deve ser testável sem TV física.
- Não alterar o launcher Android existente neste primeiro ciclo.

---

### Task 1: Criar o pacote macOS do núcleo

**Files:**
- Create: `macos/MinimalVoice/Package.swift`
- Create: `macos/MinimalVoice/Sources/MinimalVoiceCore/MinimalVoiceCore.swift`
- Create: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/MinimalVoiceCoreTests.swift`

**Interfaces:**
- `enum TvIntent: Equatable` com casos de energia, apps, volume e busca manual.
- `struct TvAppDefinition` com `name`, `packageName` e `aliases`.
- `protocol TvCommandExecuting` com `func execute(_ intent: TvIntent) async throws`.

- [ ] **Step 1: Escrever o teste RED** para `CommandParser.parse("abrir Netflix")` retornar `.launchApp(package: "com.netflix.ninja")` e para texto desconhecido retornar `nil`.
- [ ] **Step 2: Executar `swift test` e confirmar falha** por tipos/parser inexistentes.
- [ ] **Step 3: Implementar o mínimo**: tipos, catálogo inicial e parser de abrir app.
- [ ] **Step 4: Executar `swift test` e confirmar PASS**.
- [ ] **Step 5: Commitar** `git add macos/MinimalVoice && git commit -m "feat: add minimal voice core package"`.

### Task 2: Completar parser seguro de comandos

**Files:**
- Modify: `macos/MinimalVoice/Sources/MinimalVoiceCore/MinimalVoiceCore.swift`
- Test: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/MinimalVoiceCoreTests.swift`

**Interfaces:**
- `CommandParser.parse(_:) -> TvIntent?` normaliza caixa, acentos, pontuação e espaços.
- Intenções incluem `.powerOn`, `.powerOff`, `.launchApp`, `.closeApp`, `.returnToMinimalTv`, `.volumeUp`, `.volumeDown`, `.volumeSet(Int)`, `.volumeAdjust(Int)`, `.mute`, `.unmute`, `.manualSearch(appPackage:title:)`.

- [ ] **Step 1: Escrever testes RED** para energia, fechar app, aliases HBO/HBO Max, volume, mute e conteúdo específico.
- [ ] **Step 2: Executar `swift test` e confirmar as falhas esperadas**.
- [ ] **Step 3: Implementar apenas as regras aprovadas**, rejeitando frases ambíguas.
- [ ] **Step 4: Executar todos os testes do pacote e confirmar PASS**.
- [ ] **Step 5: Commitar** `git add macos/MinimalVoice && git commit -m "feat: parse safe Portuguese TV commands"`.

### Task 3: Traduzir intenções em ADB

**Files:**
- Modify: `macos/MinimalVoice/Sources/MinimalVoiceCore/MinimalVoiceCore.swift`
- Test: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/MinimalVoiceCoreTests.swift`

**Interfaces:**
- `AdbCommandBuilder.commands(for:) -> [AdbCommand]`.
- `AdbCommand` contém executável, argumentos e timeout.
- `ProcessAdbExecutor` implementa `TvCommandExecuting` sem expor saída sensível na UI.

- [ ] **Step 1: Escrever testes RED** verificando `monkey -p`, `am force-stop`, retorno ao launcher, volume keyevents e mute.
- [ ] **Step 2: Executar `swift test` e confirmar falha**.
- [ ] **Step 3: Implementar o builder e executor mínimo** com serial configurável e reconexão única.
- [ ] **Step 4: Executar testes unitários e confirmar PASS**.
- [ ] **Step 5: Testar manualmente no dispositivo** com `adb shell dumpsys activity` e comandos não destrutivos de volume.
- [ ] **Step 6: Commitar** `git add macos/MinimalVoice && git commit -m "feat: execute TV intents through adb"`.

### Task 4: Provar transcrição local

**Files:**
- Create: `macos/MinimalVoice/Sources/MinimalVoiceCore/LocalTranscriber.swift`
- Create: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/TranscriberTests.swift`
- Modify: `macos/MinimalVoice/Package.swift`

**Interfaces:**
- `protocol LocalTranscribing { func transcribe(audioURL: URL) async throws -> String }`.
- Implementação inicial atrás de adaptador Whisper local, sem rede.

- [ ] **Step 1: Escrever teste RED** usando uma gravação fixture e uma implementação de teste do protocolo.
- [ ] **Step 2: Executar `swift test` e confirmar falha**.
- [ ] **Step 3: Integrar o backend local escolhido após verificar compatibilidade com o SDK disponível**.
- [ ] **Step 4: Executar testes com fixture e confirmar PASS**.
- [ ] **Step 5: Medir latência e memória no Mac Mini** antes de escolher modelo `tiny` ou `base`.
- [ ] **Step 6: Commitar** `git add macos/MinimalVoice && git commit -m "feat: add local speech transcription adapter"`.

### Task 5: Criar interface SwiftUI com botão de escuta

**Files:**
- Create: `macos/MinimalVoice/Sources/MinimalVoiceApp/MinimalVoiceApp.swift`
- Create: `macos/MinimalVoice/Sources/MinimalVoiceApp/ContentView.swift`
- Create: `macos/MinimalVoice/Sources/MinimalVoiceApp/VoiceSessionViewModel.swift`
- Test: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/VoiceSessionViewModelTests.swift`

**Interfaces:**
- Estados: `.idle`, `.listening`, `.transcribing`, `.executing`, `.success`, `.failure`.
- `VoiceSessionViewModel.toggleListening()` controla a sessão e nunca executa enquanto não houver transcrição reconhecida.

- [ ] **Step 1: Escrever testes RED** para transição do botão, comando vazio e sucesso/erro do executor.
- [ ] **Step 2: Executar `swift test` e confirmar falha**.
- [ ] **Step 3: Implementar ViewModel e uma tela mínima** com estado, transcrição e botão.
- [ ] **Step 4: Executar testes e confirmar PASS**.
- [ ] **Step 5: Validar manualmente permissões de microfone e feedback visual**.
- [ ] **Step 6: Commitar** `git add macos/MinimalVoice && git commit -m "feat: add minimal voice control screen"`.

### Task 6: Inicialização automática e documentação

**Files:**
- Modify: `macos/MinimalVoice/Sources/MinimalVoiceApp/MinimalVoiceApp.swift`
- Create: `macos/MinimalVoice/README.md`
- Modify: `README.md`

- [ ] **Step 1: Escrever teste RED** para configuração sem serial ADB não executar comandos.
- [ ] **Step 2: Implementar configuração do serial, catálogo de apps e opção de iniciar no login**.
- [ ] **Step 3: Executar testes completos**: `swift test` e `./gradlew test`.
- [ ] **Step 4: Validar build macOS com `xcodebuild` quando o Xcode estiver instalado; registrar a limitação atual se continuar apenas com Command Line Tools**.
- [ ] **Step 5: Documentar instalação, permissão de microfone, autorização ADB e comandos suportados**.
- [ ] **Step 6: Commitar** `git add macos/MinimalVoice README.md && git commit -m "docs: document minimal voice macos app"`.
