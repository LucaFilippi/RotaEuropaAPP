# Registro de sessões

## 2026-10-30 — Fases 3–6 e atualização da release

- **Objetivo:** avançar carreira/estudos, mobilidade, revisões/notificações e acabamento; substituir o APK publicado pelo mais recente.
- **Implementado:** formulários específicos por cadastro; experiência em desenvolvimento; Kanban de portfólio; plano de estudos; dados oficiais para universidades/candidaturas/documentos/destinos; checklist documental; indicador orientativo de prontidão do cenário ativo; relatório trimestral; lembretes locais com notificações Android; mesclagem de backups JSON; testes instrumentados da migração Room 1→2, persistência e mesclagem.
- **Commits:** `380eb9b feat(planning): add career, study and readiness workflows`; `43363fd feat(backup): merge imports and test database migration`; `456b6d6 test(backup): verify scenario merge on device`.
- **Validação:** `test assembleDebug assembleDebugAndroidTest connectedAndroidTest` passou com 4 testes no Pixel 10 (API 36). O teste Compose de acessibilidade falhou antes da asserção por incompatibilidade do Espresso 3.5.1 com `InputManager.getInstance` removido; teste removido e verificação visual pendente.
- **APK:** versão 1.3, `versionCode 4`; `apksigner verify` passou, certificado SHA-256 permanece `7f67ca43f96e3f2c52142269864856d9b60afb4240de3331cb7f4d5d39ef4543`. APK SHA-256 `A75F59AB0BAB05AE6657F06F4D5BCF5BD8A0ADEAF646D46604D29FB82FE0EFD1`.
- **Publicação:** o arquivo de release anterior 1.2 será removido da árvore atual; o caminho estável `releases/ROTA-EUROPA-debug.apk` recebe a versão atual.

## 2026-10-30 — Fase 2: cenários financeiros e gráfico

- **Objetivo:** concluir exclusivamente o planejamento financeiro (Fase 2), sem começar a Fase 3.
- **Implementado:** criação/seleção de cenários financeiros persistidos com reserva, fases, tarefas, histórico, orçamento e cotação isolados por `scenarioId`; gráfico de projeção da reserva por seis meses; importação JSON com validação e restauração de múltiplos cenários; versão 1.2 / `versionCode 3`.
- **Validação:** `test assembleDebug` passou (43 tarefas Gradle, incluindo testes unitários). `apksigner verify` passou e o certificado do APK 1.2 corresponde ao APK 1.1 (`7f67ca43f96e3f2c52142269864856d9b60afb4240de3331cb7f4d5d39ef4543`). O APK foi gerado como atualização compatível.
- **Limitação:** teste manual de troca de cenários/backup e migração no emulador ou aparelho segue pendente porque não havia dispositivo ADB conectado e o emulador foi bloqueado pela sandbox.
- **Escopo:** Fase 3 não foi iniciada; aguardar autorização do usuário.

## 2026-10-30 — Continuação das Fases 2–6, logo e conexão Git

- **Objetivo:** continuar o app existente, manter possibilidade de atualização por cima do APK anterior, incorporar logo e salvar o progresso em branch remota.
- **Implementado:** Room v2 com migração 1→2; histórico financeiro, orçamento em duas moedas, cotação manual e conversão explícita, projeção até a data-alvo; cadastros flexíveis de carreira/estudos/idiomas/universidades/documentos/destinos; revisão trimestral; backup JSON validado com confirmação; CSV de tarefas/finanças; logo no dashboard e ícone; versão 1.1 / `versionCode 2`.
- **Validação registrada:** `test assembleDebug` passou antes do último pequeno ajuste de apresentação de conversão. `apksigner` confirmou que o APK anterior, o novo build e `C:\Users\user\.android\debug.keystore` têm o mesmo SHA-256 (`7f67ca43f96e3f2c52142269864856d9b60afb4240de3331cb7f4d5d39ef4543`). Ainda executar o build final após o último ajuste.
- **Limitação desta sessão:** não foi possível iniciar o AVD Pixel 10 porque o sandbox negou escrita em `%USERPROFILE%\.android\emu-last-feature-flags.protobuf.lock`; teste da migração Room v1→v2 e inspeção manual ainda pendentes. O terminal usa o cache Gradle local `-g .gradle-user-home`.
- **GitHub:** URL fornecida pelo usuário foi consultada com `git ls-remote`; o repositório estava sem refs. O root commit `a53d973` está em `develop`; a branch ativa `feature/fases-2-6` recebeu o commit de retomada. `origin` foi configurado e ambas as branches foram publicadas com tracking remoto.
- **Build final:** `test assembleDebug` passou nesta sessão após o último ajuste; o APK 1.1 foi copiado para `C:\Users\user\Documents\Codex\2026-10-29\c\outputs\ROTA-EUROPA-1.1-debug.apk` e o alias `ROTA-EUROPA-debug.apk` também foi atualizado.
- **Próximas ações:** configurar `origin` e push das duas branches; manter o Android Studio apontado para a pasta correta quando a janela estiver disponível; retomar pela `docs/RETOMADA.md`.

## 2026-10-29 — Fase 1: MVP funcional

- **Objetivo:** entregar a base funcional da Fase 1 e preparar o primeiro teste manual no emulador Pixel 10.
- **Implementado:** navegação entre Visão geral, Plano, Finanças e Ajustes; dashboard com progresso, atrasos e próximos passos; tarefas com fase, título/descrição, categoria, prioridade, status, prazo, importância, obrigatoriedade, edição, conclusão e exclusão; meta/destino/data configuráveis; reserva, moeda, saldo, aporte mensal, progresso e estimativa; tema claro/escuro; Room e DataStore locais.
- **Validação:** `gradlew.bat test assembleDebug --console=plain --no-daemon` passou (6 testes unitários). `gradlew.bat connectedAndroidTest --console=plain --no-daemon` passou (2 testes instrumentados, incluindo persistência após reabrir o banco). APK instalado e `MainActivity` observada em primeiro plano no Pixel 10.
- **Compatibilidade do ambiente:** build utiliza JDK 21 do Android Studio e bytecode target 11. AGP 9.1.1 exige flags temporárias `android.builtInKotlin=false` e `android.newDsl=false` para o fluxo atual KSP/Kotlin; o Gradle avisa que ambas são depreciadas. Rever ao migrar AGP/KSP.
- **Ajuste de teste:** corrigida a expectativa de projeção para 4 meses (901/300 precisa arredondar para cima) e o método instrumentado foi ajustado para retornar `Unit`, exigência do JUnit4.
- **Emulador e primeiro teste manual:** a aceleração gráfica automática falhou neste computador; o AVD foi configurado para renderização por software. Após um aviso temporário de lentidão do Android na primeira inicialização, o dashboard foi exibido corretamente. `MainActivity` está aberta em primeiro plano no Android Studio para o primeiro teste manual.
- **Próxima fase:** histórico de transações, orçamento e simulação de BRL/EUR; manter exportação/backup para a Fase 6.

## 2026-10-29 — Fase 0: criação e planejamento inicial

- **Objetivo:** criar o projeto Android ROTA EUROPA e preparar base de continuidade conforme o prompt mestre.
- **Alterações:** projeto Android Studio Empty Activity em Kotlin/Compose/Material 3, nome `ROTA EUROPA`, pacote `com.lucafilippi.rotaeuropa`, SDK mínimo API 24; documentação inicial e instruções de agentes.
- **Arquivos alterados:** `AGENTS.md`, `README.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`, `docs/DATA_MODEL.md`, `docs/DECISIONS.md`, `docs/TESTING.md`, `docs/SESSION_LOG.md`.
- **Validação:** projeto aberto no Android Studio; caminho local do SDK existe; arquivos de configuração inspecionados; Git inicializado. O Android Studio informou que a sincronização está baixando dependências. Uma tentativa de `assembleDebug` não retornou resultado no terminal e foi interrompida; build não confirmado. Testes não executados.
- **Problemas conhecidos:** o app ainda exibe a tela de saudação do template; ainda não há persistência, navegação ou funcionalidades do MVP. Sincronização/build precisam ser concluídos e confirmados.
- **Decisões pendentes:** confirmar versões estáveis/compatíveis conforme ambiente ao iniciar cada dependência; confirmar a estratégia de meta inicial somente como dado configurável, sem hardcode.
- **Próximas ações:** aguardar/confirmar sincronização Gradle e build debug; revisar e registrar a base inicial no Git; então começar incrementos pequenos da Fase 1 após a Fase 0.
