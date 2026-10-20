import Foundation

public enum TvIntent: Equatable, Sendable {
    case powerOn
    case powerOff
    case launchApp(package: String)
    case closeApp(package: String)
    case volumeUp
    case volumeDown
    case volumeAdjust(Int)
    case volumeSet(Int)
    case mute
    case unmute
    case returnToMinimalTv
    case manualSearch(appPackage: String, title: String)
}

public struct CommandParser: Sendable {
    public init() {}

    public func parse(_ phrase: String) -> TvIntent? {
        let normalized = Self.normalize(phrase)

        if normalized == "ligar televisao" || normalized == "ligar a televisao" || normalized == "ligar tv" {
            return .powerOn
        }
        if normalized == "desligar televisao" || normalized == "desligar a televisao" || normalized == "desligar tv" || normalized == "desligar a tv" {
            return .powerOff
        }
        if normalized == "voltar para o minimal tv" || normalized == "abrir minimal tv" {
            return .returnToMinimalTv
        }
        if normalized == "aumentar volume" || normalized == "volume mais alto" {
            return .volumeUp
        }
        if normalized == "abaixar volume" || normalized == "diminuir volume" || normalized == "volume mais baixo" {
            return .volumeDown
        }
        if normalized == "mutar televisao" || normalized == "mutar tv" || normalized == "silenciar televisao" {
            return .mute
        }
        if normalized == "desmutar televisao" || normalized == "desmutar tv" || normalized == "tirar do mudo" {
            return .unmute
        }

        if let amount = Self.extractAmount(from: normalized, prefix: "aumentar volume em ") {
            return .volumeAdjust(amount)
        }
        if let amount = Self.extractAmount(from: normalized, prefix: "abaixar volume em ") {
            return .volumeAdjust(-amount)
        }
        if let amount = Self.extractAmount(from: normalized, prefix: "diminuir volume em ") {
            return .volumeAdjust(-amount)
        }
        if let amount = Self.extractAmount(from: normalized, prefix: "volume em ") {
            return .volumeSet(amount)
        }

        if normalized.hasPrefix("abrir "), let app = Self.app(for: normalized) {
            return .launchApp(package: app.package)
        }
        if normalized.hasPrefix("fechar "), let app = Self.app(for: normalized) {
            return .closeApp(package: app.package)
        }

        for app in Self.apps where normalized.hasSuffix(" na \(app.alias)") {
            let prefix = "abrir "
            guard normalized.hasPrefix(prefix) else { continue }
            let titleEnd = normalized.count - (" na \(app.alias)".count)
            let titleStart = normalized.index(normalized.startIndex, offsetBy: prefix.count)
            let titleEndIndex = normalized.index(normalized.startIndex, offsetBy: titleEnd)
            let title = String(normalized[titleStart..<titleEndIndex]).capitalized
            guard !title.isEmpty else { continue }
            return .manualSearch(appPackage: app.package, title: title)
        }

        return nil
    }

    private static func normalize(_ phrase: String) -> String {
        phrase
            .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: Locale(identifier: "pt_BR"))
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "\\s+", with: " ", options: .regularExpression)
    }

    private static func extractAmount(from text: String, prefix: String) -> Int? {
        guard text.hasPrefix(prefix), let value = Int(text.dropFirst(prefix.count)) else { return nil }
        return max(0, min(value, 100))
    }

    private static func app(for text: String) -> (alias: String, package: String)? {
        apps.first { text == "abrir \($0.alias)" || text == "fechar \($0.alias)" }
    }

    private static let apps = [
        (alias: "netflix", package: "com.netflix.ninja"),
        (alias: "hbo", package: "com.hbo.hbonow"),
        (alias: "hbo max", package: "com.hbo.hbonow"),
        (alias: "prime", package: "com.amazon.amazonvideo.livingroom"),
        (alias: "prime video", package: "com.amazon.amazonvideo.livingroom"),
        (alias: "youtube", package: "com.google.android.youtube.tv")
    ]
}

public struct AdbCommand: Equatable, Sendable {
    public let arguments: [String]

    public init(arguments: [String]) {
        self.arguments = arguments
    }
}

public struct AdbCommandBuilder: Sendable {
    public let serial: String

    public init(serial: String) {
        self.serial = serial
    }

    public func commands(for intent: TvIntent) -> [AdbCommand] {
        switch intent {
        case .powerOn:
            return [command("shell", "input", "keyevent", "224")]
        case .powerOff:
            return [command("shell", "input", "keyevent", "26")]
        case .launchApp(let package):
            return [launch(package)]
        case .closeApp(let package):
            return [command("shell", "am", "force-stop", package), launch("com.minimaltv.launcher")]
        case .returnToMinimalTv:
            return [launch("com.minimaltv.launcher")]
        case .volumeUp:
            return [keyevent(24)]
        case .volumeDown:
            return [keyevent(25)]
        case .volumeAdjust(let amount):
            let key = amount >= 0 ? 24 : 25
            return Array(repeating: keyevent(key), count: max(1, abs(amount) / 5))
        case .volumeSet(let amount):
            return [command("shell", "cmd", "media_session", "volume", "--stream", "3", "--set", String(max(0, min(amount, 100))))]
        case .mute, .unmute:
            return [keyevent(164)]
        case .manualSearch:
            return []
        }
    }

    private func launch(_ package: String) -> AdbCommand {
        command("shell", "monkey", "-p", package, "1")
    }

    private func keyevent(_ key: Int) -> AdbCommand {
        command("shell", "input", "keyevent", String(key))
    }

    private func command(_ arguments: String...) -> AdbCommand {
        AdbCommand(arguments: ["-s", serial] + arguments)
    }
}
