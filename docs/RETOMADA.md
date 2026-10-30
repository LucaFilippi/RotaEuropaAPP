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
- Continue novas entregas em branches `feature/*`; integre em `develop` e reserve `main` para a versão final.

## Próximas ações prioritárias

1. Executar `gradlew.bat -g .gradle-user-home test assembleDebug --console=plain --no-daemon` após os últimos ajustes.
2. Copiar `app/build/outputs/apk/debug/app-debug.apk` para `outputs/ROTA-EUROPA-1.1-debug.apk`.
3. Criar branch/commit e publicar a branch no remoto conforme pedido.
4. Testar migração Room 1→2 e instalação de atualização no Pixel 10 ou celular conectado.
5. Abrir a pasta específica do projeto no Android Studio se ainda estiver na tela errada.
6. Continuar os itens pendentes da Fase 2 (cenários independentes/gráficos), Fases 3–4 (campos especializados), Fase 5 (notificações/prontidão) e Fase 6 (mesclagem/testes/acessibilidade).

## Limitação conhecida

O emulador não iniciou nesta sessão: a sandbox bloqueou escrita em `%USERPROFILE%\.android\emu-last-feature-flags.protobuf.lock`. Não apagar `.android`, o AVD, o banco do app ou a chave de assinatura. A próxima execução pode solicitar acesso de escrita pontual à pasta do AVD/estado do emulador.
