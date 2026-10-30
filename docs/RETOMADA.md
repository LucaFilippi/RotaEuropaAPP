# Retomada — ROTA EUROPA

Data de registro: 30/10/2026

## Onde está o projeto

- Pasta: `C:\Users\user\Documents\Codex\2026-10-29\c\rota-europa`
- Projeto Android: Kotlin, Jetpack Compose, Room/DataStore, pacote `com.lucafilippi.rotaeuropa`.
- Repositório solicitado: `https://github.com/LucaFilippi/RotaEuropaAPP`.
- Build: JDK 21 do Android Studio e cache Gradle local `-g .gradle-user-home`.

## Estado salvo

- Fases 0–5 implementadas; a Fase 6 aguarda inspeção visual/acessibilidade manual.
- Logo do usuário em `app/src/main/res/drawable-nodpi/rota_europa_logo.png`.
- Room v2 com migração explícita 1→2.
- `versionCode=4`, `versionName=1.3`, `applicationId` preservado.
- O certificado SHA-256 do APK anterior e do novo build foi comparado e é o mesmo: `7f67ca43f96e3f2c52142269864856d9b60afb4240de3331cb7f4d5d39ef4543`.
- Branch de integração: `develop`; branch de trabalho: `feature/fases-3-6`.
- Entregas profissionais: `380eb9b` (carreira/estudos/prontidão) e `43363fd` (mesclagem de backups/migração).
- Release atual: `releases/ROTA-EUROPA-debug.apk`, APK 1.3; substitui o arquivo 1.2 na pasta pública do repositório.
- SHA-256 do APK: `A75F59AB0BAB05AE6657F06F4D5BCF5BD8A0ADEAF646D46604D29FB82FE0EFD1`.
- `apksigner verify` passou. Certificado SHA-256 `7f67ca43f96e3f2c52142269864856d9b60afb4240de3331cb7f4d5d39ef4543`, igual às versões anteriores.
- `test assembleDebug assembleDebugAndroidTest` passou. `connectedAndroidTest` passou com 3 testes no Pixel 10: migração Room 1→2, persistência e contexto.
- O teste Compose de acessibilidade foi removido após falhar em Espresso 3.5.1/API 36 antes da asserção (`InputManager.getInstance` ausente). Inspeção visual/acessibilidade manual continua pendente.
- Fases 3–5 implementam formulários especializados, Kanban, checklist documental, relatórios, prontidão e lembretes/notificações locais. Fase 6 inclui mesclagem JSON.

## Próximas ações prioritárias

1. Instalar o APK 1.3 e conferir a interface/fluxos no emulador ou celular.
2. Atualizar Espresso para uma versão compatível com API 36 e reintroduzir o teste automatizado de acessibilidade, ou validar manualmente em aparelho.
3. Abrir no Android Studio a pasta: `C:\Users\user\Documents\Codex\2026-10-29\c\rota-europa`.

## Limitação conhecida

O emulador não iniciou nesta sessão: a sandbox bloqueou escrita em `%USERPROFILE%\.android\emu-last-feature-flags.protobuf.lock`. `adb devices` não mostrou dispositivos conectados. A abertura do projeto na janela do Android Studio também não foi confirmada. Não apagar `.android`, o AVD, o banco do app ou a chave de assinatura. A próxima execução pode solicitar acesso de escrita pontual à pasta do AVD/estado do emulador.
