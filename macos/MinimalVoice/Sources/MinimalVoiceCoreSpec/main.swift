import Foundation
import MinimalVoiceCore

func expect(_ condition: @autoclosure () -> Bool, _ message: String) {
    guard condition() else {
        FileHandle.standardError.write(Data("FAIL: \(message)\n".utf8))
        exit(1)
    }
}

let parser = CommandParser()
expect(parser.parse("abrir Netflix") == .launchApp(package: "com.netflix.ninja"), "abrir Netflix")
expect(parser.parse("ligar televisão") == .powerOn, "ligar televisão")
expect(parser.parse("desligar a TV") == .powerOff, "desligar TV")
expect(parser.parse("fechar HBO Max") == .closeApp(package: "com.hbo.hbonow"), "fechar HBO Max")
expect(parser.parse("aumentar volume") == .volumeUp, "aumentar volume")
expect(parser.parse("abaixar volume em 10") == .volumeAdjust(-10), "abaixar volume em 10")
expect(parser.parse("mutar televisão") == .mute, "mutar televisão")
expect(
    parser.parse("abrir Supernatural na HBO") ==
        .manualSearch(appPackage: "com.hbo.hbonow", title: "Supernatural"),
    "busca manual na HBO"
)
expect(parser.parse("fazer alguma coisa que não existe") == nil, "comando desconhecido")

let builder = AdbCommandBuilder(serial: "192.168.1.9:5555")
expect(
    builder.commands(for: .launchApp(package: "com.netflix.ninja")) == [
        AdbCommand(arguments: ["-s", "192.168.1.9:5555", "shell", "monkey", "-p", "com.netflix.ninja", "1"])
    ],
    "lançar Netflix via ADB"
)
expect(
    builder.commands(for: .closeApp(package: "com.netflix.ninja")) == [
        AdbCommand(arguments: ["-s", "192.168.1.9:5555", "shell", "am", "force-stop", "com.netflix.ninja"]),
        AdbCommand(arguments: ["-s", "192.168.1.9:5555", "shell", "monkey", "-p", "com.minimaltv.launcher", "1"])
    ],
    "fechar Netflix e voltar ao Minimal TV"
)
expect(
    builder.commands(for: .volumeUp) == [
        AdbCommand(arguments: ["-s", "192.168.1.9:5555", "shell", "input", "keyevent", "24"])
    ],
    "aumentar volume via ADB"
)

final class RecordingAdbRunner: AdbCommandRunning {
    var executed: [[String]] = []

    func run(_ command: AdbCommand) throws {
        executed.append(command.arguments)
    }
}

let runner = RecordingAdbRunner()
let executor = ProcessAdbExecutor(builder: builder, runner: runner)
try! executor.execute(.launchApp(package: "com.netflix.ninja"))
expect(runner.executed.count == 1, "executor executa o comando ADB")
expect(SpeechConfiguration.requiresOnDeviceRecognition, "reconhecimento somente no dispositivo")
expect(SpeechSetupGuide.steps.contains { $0.localizedCaseInsensitiveContains("Siri") }, "orientação para ativar Siri")
expect(SpeechSetupGuide.steps.contains { $0.localizedCaseInsensitiveContains("Ditado") }, "orientação para ativar Ditado")
expect(
    AdbDeviceList.parse("List of devices attached\nadb-tv._adb-tls-connect._tcp device product:TV model:SMART_TV\n").first == "adb-tv._adb-tls-connect._tcp",
    "descoberta de TV ADB autorizada"
)
expect(VoiceSessionState.idle.canStartListening, "estado inativo permite iniciar")
expect(!VoiceSessionState.transcribing.canExecuteCommand, "transcrição bloqueia execução")
print("PASS: MinimalVoiceCoreSpec")
