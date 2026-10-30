# Testes e validação

## Como executar

No Windows, com Android SDK e JDK configurados:

- Testes unitários: `gradlew.bat test`
- Testes instrumentados (emulador/dispositivo): `gradlew.bat connectedAndroidTest`
- Compilação debug: `gradlew.bat assembleDebug`

## Cobertura atual

Os testes unitários em `app/src/test` cobrem progresso ponderado e cancelamentos, atraso, meta alcançada, parsing monetário, cotação/conversão BRL↔EUR e simulação financeira com despesas. `app/src/androidTest` verifica persistência Room após fechar e abrir o banco.

## Resultado desta sessão

`test assembleDebug assembleDebugAndroidTest connectedAndroidTest` passou na versão 1.3. No AVD Pixel 10/API 36, 5 testes instrumentados passaram, cobrindo migração 1→2 sem perda, persistência, mesclagem de cenário, contexto do app e navegação com labels legíveis. O Espresso foi atualizado para 3.7.0, resolvendo a incompatibilidade do teste Compose no Android 16. A inspeção visual/acessibilidade manual segue pendente.
