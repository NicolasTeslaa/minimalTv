# Minimal Voice macOS Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir um app macOS residente que transcreve voz localmente e controla a TV Android via ADB.

**Architecture:** Um núcleo Swift Package testável conterá parser, intenções e executor ADB. Um alvo SwiftUI macOS conectará captura de áudio, transcrição local e estado visual ao núcleo. O primeiro ciclo usa `Speech` da Apple com reconhecimento no dispositivo e um adaptador ADB por processo.

**Tech Stack:** Swift 6, Swift Package Manager, SwiftUI, AVFoundation, Speech, executable spec, processo `adb`.

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

- [x] **Step 1–5: Implementado e validado** com `MinimalVoiceCoreSpec`; XCTest não está disponível no Command Line Tools instalado.

### Task 2: Completar parser seguro de comandos

**Files:**
- Modify: `macos/MinimalVoice/Sources/MinimalVoiceCore/MinimalVoiceCore.swift`
- Test: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/MinimalVoiceCoreTests.swift`

**Interfaces:**
- `CommandParser.parse(_:) -> TvIntent?` normaliza caixa, acentos, pontuação e espaços.
- Intenções incluem `.powerOn`, `.powerOff`, `.launchApp`, `.closeApp`, `.returnToMinimalTv`, `.volumeUp`, `.volumeDown`, `.volumeSet(Int)`, `.volumeAdjust(Int)`, `.mute`, `.unmute`, `.manualSearch(appPackage:title:)`.

- [x] **Step 1–5: Implementado e validado** com comandos aprovados e rejeição de texto desconhecido.

### Task 3: Traduzir intenções em ADB

**Files:**
- Modify: `macos/MinimalVoice/Sources/MinimalVoiceCore/MinimalVoiceCore.swift`
- Test: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/MinimalVoiceCoreTests.swift`

**Interfaces:**
- `AdbCommandBuilder.commands(for:) -> [AdbCommand]`.
- `AdbCommand` contém executável, argumentos e timeout.
- `ProcessAdbExecutor` implementa `TvCommandExecuting` sem expor saída sensível na UI.

- [x] **Step 1–4: Implementado e validado** com runner substituto; o serial padrão é `192.168.1.9:36661`.
- [ ] **Step 5:** Testar manualmente no dispositivo com `adb shell dumpsys activity` e comandos não destrutivos de volume.

### Task 4: Provar transcrição local

**Files:**
- Create: `macos/MinimalVoice/Sources/MinimalVoiceCore/LocalTranscriber.swift`
- Create: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/TranscriberTests.swift`
- Modify: `macos/MinimalVoice/Package.swift`

**Interfaces:**
- `protocol LocalTranscribing { func transcribe(audioURL: URL) async throws -> String }`.
- Implementação inicial atrás de adaptador Whisper local, sem rede.

- [x] **Step 1–4:** Implementado no alvo macOS com `SFSpeechAudioBufferRecognitionRequest` e `requiresOnDeviceRecognition = true`; o núcleo verifica essa política.
- [ ] **Step 5:** Medir latência e memória no Mac Mini em uso contínuo.

### Task 5: Criar interface SwiftUI com botão de escuta

**Files:**
- Create: `macos/MinimalVoice/Sources/MinimalVoiceApp/MinimalVoiceApp.swift`
- Create: `macos/MinimalVoice/Sources/MinimalVoiceApp/ContentView.swift`
- Create: `macos/MinimalVoice/Sources/MinimalVoiceApp/VoiceSessionViewModel.swift`
- Test: `macos/MinimalVoice/Tests/MinimalVoiceCoreTests/VoiceSessionViewModelTests.swift`

**Interfaces:**
- Estados: `.idle`, `.listening`, `.transcribing`, `.executing`, `.success`, `.failure`.
- `VoiceSessionViewModel.toggleListening()` controla a sessão e nunca executa enquanto não houver transcrição reconhecida.

- [x] **Step 1–4:** Implementado com estados do núcleo e tela SwiftUI de botão único, transcrição, feedback e erros.
- [ ] **Step 5:** Validar manualmente permissões de microfone e feedback visual no Mac Mini.

### Task 6: Inicialização automática e documentação

**Files:**
- Modify: `macos/MinimalVoice/Sources/MinimalVoiceApp/MinimalVoiceApp.swift`
- Create: `macos/MinimalVoice/README.md`
- Modify: `README.md`

- [x] **Step 1–2:** Serial configurável por `defaults` e script de build/execução adicionados.
- [ ] **Step 3:** Executar testes Android quando o JDK estiver disponível.
- [x] **Step 4–5:** Build Swift validado; a limitação do Command Line Tools e a instalação estão documentadas.
