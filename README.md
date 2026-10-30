# ROTA EUROPA

Aplicativo Android pessoal para planejar uma mudança internacional com cenários configuráveis, acompanhamento de tarefas, finanças, carreira, estudos e documentos. O objetivo é apoiar decisões sustentáveis com dados locais e sem depender de serviços online.

## Estado atual

Fases 0 e 1 concluídas. A branch de desenvolvimento amplia a base com finanças, cadastros de carreira/estudo/destinos/documentos, revisões trimestrais e backup. As Fases 2–6 ainda estão em andamento; consulte `docs/ROADMAP.md` para os limites exatos desta entrega.

## Tecnologias e ambiente

- Kotlin e Jetpack Compose / Material 3
- Android Gradle Plugin e Gradle Wrapper definidos pelo template do Android Studio
- SDK mínimo API 24; compilação/target configurados pelo template para API 36
- O caminho local do Android SDK configurado pelo Android Studio existe neste ambiente (`C:\Users\user\AppData\Local\Android\Sdk`)
- Java/Kotlin bytecode target 11 para compatibilidade do app; usar JDK 21 do Android Studio para executar Gradle
- Version Catalog em `gradle/libs.versions.toml`

Não altere versões geradas pelo ambiente sem verificar compatibilidade e disponibilidade.

## Como abrir e compilar

Abra esta pasta no Android Studio e aguarde a sincronização do Gradle. Para uma compilação local, use `gradlew.bat assembleDebug` no Windows. Para executar testes unitários, use `gradlew.bat test`. É necessário ter o Android SDK instalado e o JDK exigido pelo Android Gradle Plugin configurado.

Os comandos acima são instruções de uso. Use o cache Gradle local com `gradlew.bat -g .gradle-user-home test assembleDebug --console=plain --no-daemon` quando o diretório Gradle global estiver indisponível. Configure `JAVA_HOME` para o JDK 21 do Android Studio. A assinatura debug usa o `debug.keystore` padrão do usuário, preservando compatibilidade com o APK debug anterior neste computador; mantenha essa chave para atualizações.

## Estrutura atual e planejada

- `app/src/main`: aplicação Android, telas Compose, ViewModel, domínio, Room e DataStore.
- `docs/`: roadmap, arquitetura, dados, decisões, testes, registro de sessões e instruções de retomada.
- Camadas: apresentação (Compose/ViewModels), domínio (regras puras) e dados (Room/DataStore).

## Princípios

- Offline por padrão, sem conta, backend, telemetria ou chamadas de API necessárias.
- Dados estruturados armazenados localmente.
- Valores monetários representados sem ponto flutuante e moedas convertidas apenas com cotação manual explícita.
- Cenários e datas de mudança configuráveis; nenhuma data fixa no código.
- Progresso operacional separado de prontidão efetiva para mudança.

## Limitações conhecidas

- O app é nativo Android e deve ser testado no emulador/dispositivo; Chrome não executa o APK.
- Registros de carreira, idiomas, estudos, universidades e documentos usam um formato local flexível; ainda não têm todos os campos especializados do prompt mestre.
- O comparador financeiro ainda não salva cenários independentes; os três valores em euros apresentados são referências rápidas. Gráficos, notificações, indicadores de prontidão e cenários múltiplos continuam pendentes.
- A importação JSON substitui dados após confirmação e validação; mesclagem ainda não está implementada. CSV cobre tarefas, movimentações e orçamento.
- O projeto usa flags temporárias de compatibilidade AGP/KSP documentadas em `gradle.properties`; essas flags são depreciadas e precisam ser revistas ao atualizar o Android Gradle Plugin.
- O teste unitário e o build debug foram executados. O emulador não iniciou nesta sessão por bloqueio de escrita na pasta `.android`; o teste de migração Room para v2 e a inspeção manual das novas telas precisam ser feitos ao retomar.

Consulte `docs/ROADMAP.md` e `docs/SESSION_LOG.md` para o estado de implementação.
