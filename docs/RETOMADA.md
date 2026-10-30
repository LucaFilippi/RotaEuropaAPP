# Retomada — ROTA EUROPA

Data de registro: 30/10/2026

## Onde está o projeto

- Pasta: `C:\Users\user\Documents\Codex\2026-10-29\c\rota-europa`
- Projeto Android: Kotlin, Jetpack Compose, Room/DataStore, pacote `com.lucafilippi.rotaeuropa`.
- Repositório solicitado: `https://github.com/LucaFilippi/RotaEuropaAPP`.
- Build: JDK 21 do Android Studio e cache Gradle local `-g .gradle-user-home`.

## Estado salvo

- Fases 0–1 funcionais.
- Fases 2–6 têm incrementos locais descritos em `ROADMAP.md`; não estão todas concluídas.
- Logo do usuário em `app/src/main/res/drawable-nodpi/rota_europa_logo.png`.
- Room v2 com migração explícita 1→2.
- `versionCode=2`, `versionName=1.1`, `applicationId` preservado.
- O certificado SHA-256 do APK anterior e do novo build foi comparado e é o mesmo: `7f67ca43f96e3f2c52142269864856d9b60afb4240de3331cb7f4d5d39ef4543`.
- Branch de integração: `develop` (`a53d973`, baseline salvo).
- Branch ativa: `feature/fases-2-6` (continuação desta entrega).
- Remoto `origin` configurado; `develop` e `feature/fases-2-6` publicados e rastreando o GitHub.
- Commit mais recente no início desta retomada: `b756c73` (`docs: confirm GitHub branches and handoff`).
- Build e testes unitários executados com sucesso após os últimos ajustes (`test assembleDebug`).
- APK atualizado salvo em `outputs/ROTA-EUROPA-1.1-debug.apk` e no alias `outputs/ROTA-EUROPA-debug.apk`.
- SHA-256 do APK: `26683542A46155892512D16D13F042D08AAB6F63CBAEF2BBDF982CAD7F054A7C`.
- `apksigner verify` passou. A assinatura foi comparada com a versão anterior e com a chave debug local, preservando a possibilidade de atualização no mesmo aparelho.
- Continue novas entregas em branches `feature/*`; integre em `develop` e reserve `main` para a versão final.

## Próximas ações prioritárias

1. Instalar o APK acima no Pixel 10 ou celular conectado e validar atualização sobre a versão existente.
2. Testar migração Room 1→2 com dados existentes no dispositivo/emulador.
3. Abrir no Android Studio a pasta específica do projeto caso ainda não esteja aberta: `C:\Users\user\Documents\Codex\2026-10-29\c\rota-europa`.
4. Continuar os itens pendentes da Fase 2 (cenários independentes/gráficos), Fases 3–4 (campos especializados), Fase 5 (notificações/prontidão) e Fase 6 (mesclagem/testes/acessibilidade).

## Limitação conhecida

O emulador não iniciou nesta sessão: a sandbox bloqueou escrita em `%USERPROFILE%\.android\emu-last-feature-flags.protobuf.lock`. `adb devices` não mostrou dispositivos conectados. A abertura do projeto na janela do Android Studio também não foi confirmada. Não apagar `.android`, o AVD, o banco do app ou a chave de assinatura. A próxima execução pode solicitar acesso de escrita pontual à pasta do AVD/estado do emulador.
