import AVFoundation
import Speech
import SwiftUI
import MinimalVoiceCore

@MainActor
final class VoiceController: NSObject, ObservableObject, SFSpeechRecognizerDelegate {
    @Published private(set) var state: VoiceSessionState = .idle
    @Published private(set) var transcript = ""
    @Published private(set) var feedback = "Pronto para ouvir"
    @Published private(set) var errorMessage: String?

    private let recognizer = SFSpeechRecognizer(locale: Locale(identifier: "pt-BR"))
    private let audioEngine = AVAudioEngine()
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let parser = CommandParser()
    private let executor: ProcessAdbExecutor

    init(serial: String = UserDefaults.standard.string(forKey: "tvSerial") ?? TvConfiguration.defaultSerial) {
        executor = ProcessAdbExecutor(builder: AdbCommandBuilder(serial: serial))
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
        transcript = ""
        feedback = "Ouvindo…"
        state = .listening

        SFSpeechRecognizer.requestAuthorization { [weak self] authorization in
            Task { @MainActor in
                guard let self else { return }
                guard authorization == .authorized else {
                    self.fail("Permissão de reconhecimento de voz não concedida")
                    return
                }
                do {
                    try self.beginRecognition()
                } catch {
                    self.fail("Não foi possível acessar o microfone: \(error.localizedDescription)")
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
                    if result.isFinal { self.executeTranscript() }
                }
                if let error, self.state == .listening {
                    self.fail(error.localizedDescription)
                }
            }
        }

        audioEngine.prepare()
        try audioEngine.start()
    }

    private func stopListening() {
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)
        recognitionRequest?.endAudio()
        state = .transcribing
        feedback = "Processando…"
    }

    private func executeTranscript() {
        guard state == .listening || state == .transcribing else { return }
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)
        recognitionRequest = nil
        recognitionTask = nil

        guard let intent = parser.parse(transcript) else {
            fail("Não entendi esse comando")
            return
        }
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

    private func fail(_ message: String) {
        audioEngine.stop()
        if audioEngine.inputNode.numberOfInputs > 0 {
            audioEngine.inputNode.removeTap(onBus: 0)
        }
        recognitionRequest = nil
        recognitionTask?.cancel()
        recognitionTask = nil
        state = .failure
        errorMessage = message
        feedback = "Tente novamente"
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

            Button(controller.state == .listening ? "Parar de escutar" : "Começar a escutar") {
                controller.toggleListening()
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .keyboardShortcut(.space, modifiers: [])

            if let errorMessage = controller.errorMessage {
                Text(errorMessage)
                    .font(.callout)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }

            Text("Comandos: Netflix, HBO Max, Prime Video, volume, mudo e Minimal TV")
                .font(.caption)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
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
