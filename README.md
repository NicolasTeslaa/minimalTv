# Minimal TV

Launcher Android TV em Kotlin, Android Views e RecyclerView. A Home mostra aplicativos instalados, favoritos primeiro, sem anúncios, feeds ou rede própria.

## Requisitos de desenvolvimento

- macOS Apple Silicon ou outro ambiente com JDK 17 e Android SDK.
- Android SDK Platform 35, Build Tools 35.0.0 e Platform Tools.
- TV ou emulador Android TV; Android 14 é o alvo testado.

Defina `JAVA_HOME` para um JDK 17 e `ANDROID_HOME` para a raiz do SDK. No ambiente usado neste projeto:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
```

## Build e testes

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

O APK debug fica em `app/build/outputs/apk/debug/app-debug.apk`. Use `adb devices -l` para descobrir o serial atual; a porta de ADB sem fio é dinâmica.

## Instalação e abertura como app comum

Substitua `<serial>` pelo dispositivo listado em `adb devices -l`:

```bash
adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <serial> shell am start -n com.minimaltv.launcher/.MainActivity
```

Esses comandos não definem o launcher padrão. A instalação permite testar a Home e abrir aplicativos sem afetar a tela inicial original.

## Diagnóstico e desempenho

```bash
adb -s <serial> shell dumpsys meminfo -s com.google.android.tvlauncher
adb -s <serial> shell dumpsys meminfo -s com.minimaltv.launcher
adb -s <serial> shell top -b -n 1 -m 15
adb -s <serial> shell cat /proc/meminfo
adb -s <serial> logcat -d -t 500 | grep -E 'com.minimaltv.launcher|FATAL EXCEPTION|ANR'
```

Compare medições após o mesmo tempo desde a inicialização, com os mesmos aplicativos abertos. PSS e RSS representam medidas diferentes; RSS de processos não deve ser somado como memória exclusiva.

## Recuperação e remoção segura

O Minimal TV não desativa nem remove `com.google.android.tvlauncher`. Para retornar ao launcher original após uma eventual seleção manual de Home, abra **Configurações do Android TV → Apps → Apps padrão → App de início/Home** e selecione o launcher de fábrica. A nomenclatura pode variar no firmware. Teste esse caminho no aparelho antes de qualquer troca manual.

Se a configuração Home não estiver disponível, use ADB para abrir as configurações do sistema, sem desativar pacotes:

```bash
adb -s <serial> shell am start -a android.settings.SETTINGS
```

Para remover apenas o Minimal TV:

```bash
adb -s <serial> uninstall com.minimaltv.launcher
```

Não use `pm disable`, root, alterações de partição ou comandos de firmware para forçar a substituição da Home.

Veja [SPEC.md](SPEC.md), [TEST_PLAN.md](TEST_PLAN.md) e [PERFORMANCE.md](PERFORMANCE.md).
