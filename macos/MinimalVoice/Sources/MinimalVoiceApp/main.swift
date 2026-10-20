import AVFoundation
import AppKit
import Speech
import SwiftUI
import MinimalVoiceCore

@MainActor
final class VoiceController: NSObject, ObservableObject, SFSpeechRecognizerDelegate {
    @Published private(set) var state: VoiceSessionState = .idle
    @Published private(set) var transcript = ""
    @Published private(set) var feedback = "Pronto para ouvir"
    @Published private(set) var errorMessage: String?
    @Published private(set) var needsSpeechSetup = false
    let tvSerial: String

    private let recognizer = SFSpeechRecognizer(locale: Locale(identifier: "pt-BR"))
    private let audioEngine = AVAudioEngine()
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private var automaticExecutionTask: Task<Void, Never>?
    private let parser = CommandParser()
    private let executor: ProcessAdbExecutor
    private let commandLog = VoiceCommandLogger()

    init(serial: String = UserDefaults.standard.string(forKey: "tvSerial") ?? TvConfiguration.defaultSerial) {
        let resolvedSerial = UserDefaults.standard.string(forKey: "tvSerial") ?? ProcessAdbDeviceDiscovery().firstAvailableSerial() ?? serial
        tvSerial = resolvedSerial
        executor = ProcessAdbExecutor(builder: AdbCommandBuilder(serial: resolvedSerial))
        super.init()
        recognizer?.delegate = self
    }

    func toggleListening() {
        if state == .listening {
            stopListening()
        } else if state.canStartListening {
            startListening()
        }
    }

    private func startListening() {
        errorMessage = nil
        needsSpeechSetup = false
        transcript = ""
        feedback = "Ouvindo…"
        state = .listening

        SFSpeechRecognizer.requestAuthorization { [weak self] authorization in
            Task { @MainActor in
                guard let self else { return }
                guard authorization == .authorized else {
                    self.fail("O reconhecimento de voz está desativado", speechSetup: true)
                    return
                }
                do {
                    try self.beginRecognition()
                } catch {
                    self.fail("Não foi possível iniciar a escuta: \(error.localizedDescription)", speechSetup: true)
                }
            }
        }
    }

    private func beginRecognition() throws {
        recognitionTask?.cancel()
        recognitionTask = nil
        let request = SFSpeechAudioBufferRecognitionRequest()
        request.requiresOnDeviceRecognition = SpeechConfiguration.requiresOnDeviceRecognition
        request.shouldReportPartialResults = true
        recognitionRequest = request

        let inputNode = audioEngine.inputNode
        let format = inputNode.outputFormat(forBus: 0)
        inputNode.removeTap(onBus: 0)
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: format) { [weak self] buffer, _ in
            self?.recognitionRequest?.append(buffer)
        }

        recognitionTask = recognizer?.recognitionTask(with: request) { [weak self] result, error in
            Task { @MainActor in
                guard let self else { return }
                if let result {
                    self.transcript = result.bestTranscription.formattedString
                    self.scheduleAutomaticExecution()
                    if result.isFinal { self.executeTranscript() }
                }
                if let error, self.state == .listening {
                    self.fail(error.localizedDescription, speechSetup: true)
                }
            }
        }

        audioEngine.prepare()
        try audioEngine.start()
    }

    private func stopListening() {
        automaticExecutionTask?.cancel()
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)
        recognitionRequest?.endAudio()
        state = .transcribing
        feedback = "Processando…"
        automaticExecutionTask = Task { [weak self] in
            do {
                try await Task.sleep(nanoseconds: 900_000_000)
            } catch {
                return
            }
            await MainActor.run {
                guard let self, self.state == .transcribing else { return }
                self.executeTranscript()
            }
        }
    }

    private func executeTranscript() {
        guard state == .listening || state == .transcribing else { return }
        automaticExecutionTask?.cancel()
        automaticExecutionTask = nil
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)
        recognitionRequest = nil
        recognitionTask = nil

        guard let intent = parser.parse(transcript) else {
            commandLog.record(status: "unrecognized", phrase: transcript)
            fail("Não entendi esse comando")
            return
        }
        commandLog.record(status: "recognized", phrase: transcript)
        state = .executing

        if case let .manualSearch(_, title) = intent {
            state = .success
            feedback = "Abra o app e pesquise \"\(title)\" manualmente"
            return
        }

        do {
            try executor.execute(intent)
            state = .success
            feedback = "Comando executado"
        } catch {
            fail("Falha ao falar com a TV: \(error.localizedDescription)")
        }
    }

    private func fail(_ message: String, speechSetup: Bool = false) {
        automaticExecutionTask?.cancel()
        automaticExecutionTask = nil
        audioEngine.stop()
        if audioEngine.inputNode.numberOfInputs > 0 {
            audioEngine.inputNode.removeTap(onBus: 0)
        }
        recognitionRequest = nil
        recognitionTask?.cancel()
        recognitionTask = nil
        state = .failure
        errorMessage = message
        needsSpeechSetup = speechSetup
        feedback = "Tente novamente"
    }

    private func scheduleAutomaticExecution() {
        automaticExecutionTask?.cancel()
        automaticExecutionTask = Task { [weak self] in
            do {
                try await Task.sleep(nanoseconds: SpeechConfiguration.automaticExecutionPauseNanoseconds)
            } catch {
                return
            }
            guard !Task.isCancelled else { return }
            await MainActor.run {
                guard let self, self.state == .listening, !self.transcript.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
                self.executeTranscript()
            }
        }
    }

    func openSpeechSettings() {
        guard let url = URL(string: "x-apple.systempreferences:com.apple.Siri-Settings") else { return }
        NSWorkspace.shared.open(url)
    }
}

final class VoiceCommandLogger {
    private let fileURL: URL

    init() {
        let library = FileManager.default.urls(for: .libraryDirectory, in: .userDomainMask).first!
        fileURL = library.appendingPathComponent("Logs/MinimalVoice/commands.log")
    }

    func record(status: String, phrase: String) {
        do {
            try FileManager.default.createDirectory(at: fileURL.deletingLastPathComponent(), withIntermediateDirectories: true)
            let timestamp = ISO8601DateFormatter().string(from: Date())
            let line = "\(timestamp)\t" + VoiceCommandLog.format(status: status, phrase: phrase)
            if FileManager.default.fileExists(atPath: fileURL.path) {
                let handle = try FileHandle(forWritingTo: fileURL)
                try handle.seekToEnd()
                try handle.write(contentsOf: Data(line.utf8))
                try handle.close()
            } else {
                try Data(line.utf8).write(to: fileURL, options: .atomic)
            }
        } catch {
            // Logging must never interrupt voice control.
        }
    }
}

struct MinimalVoiceView: View {
    @StateObject private var controller = VoiceController()

    var body: some View {
        VStack(spacing: 24) {
            VStack(spacing: 8) {
                Text("Minimal Voice")
                    .font(.system(size: 34, weight: .semibold, design: .rounded))
                Text("Controle sua TV falando naturalmente")
                    .foregroundStyle(.secondary)
            }

            VStack(spacing: 10) {
                Text(controller.feedback)
                    .font(.headline)
                Text(controller.transcript.isEmpty ? "Sua transcrição aparecerá aqui" : controller.transcript)
                    .font(.title3)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(controller.transcript.isEmpty ? .secondary : .primary)
                    .frame(minHeight: 50)
            }
            .frame(maxWidth: .infinity)
            .padding(24)
            .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 18))

            Button(controller.state == .listening ? "Parar e executar" : "Começar a escutar") {
                controller.toggleListening()
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .keyboardShortcut(.space, modifiers: [])

            if controller.state == .listening {
                Text("Fale um comando; ele será executado após uma breve pausa.")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            if let errorMessage = controller.errorMessage {
                Text(errorMessage)
                    .font(.callout)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }

            if controller.needsSpeechSetup {
                VStack(alignment: .leading, spacing: 8) {
                    Text(SpeechSetupGuide.title)
                        .font(.headline)
                    ForEach(Array(SpeechSetupGuide.steps.enumerated()), id: \.offset) { index, step in
                        Text("\(index + 1). \(step)")
                            .font(.callout)
                    }
                    Button("Abrir Ajustes do Sistema") {
                        controller.openSpeechSettings()
                    }
                    .buttonStyle(.bordered)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .background(Color.orange.opacity(0.14), in: RoundedRectangle(cornerRadius: 14))
            }

            Text("Comandos: Netflix, HBO Max, Prime Video, volume, mudo e Minimal TV")
                .font(.caption)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            Text("TV ADB: \(controller.tvSerial)")
                .font(.caption2)
                .foregroundStyle(.tertiary)
        }
        .padding(36)
        .frame(width: 520, height: 390)
    }
}

@main
struct MinimalVoiceApp: App {
    var body: some Scene {
        WindowGroup("Minimal Voice") {
            MinimalVoiceView()
        }
        .windowResizability(.contentSize)
    }
}
