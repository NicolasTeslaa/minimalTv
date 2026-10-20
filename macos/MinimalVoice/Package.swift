// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "MinimalVoice",
    platforms: [.macOS(.v13)],
    products: [
        .library(name: "MinimalVoiceCore", targets: ["MinimalVoiceCore"]),
        .executable(name: "MinimalVoiceCoreSpec", targets: ["MinimalVoiceCoreSpec"])
    ],
    targets: [
        .target(name: "MinimalVoiceCore"),
        .executableTarget(name: "MinimalVoiceCoreSpec", dependencies: ["MinimalVoiceCore"])
    ]
)
