# Testes e validação

## Como executar

No Windows, com Android SDK e JDK configurados:

- Testes unitários: `gradlew.bat test`
- Testes instrumentados (emulador/dispositivo): `gradlew.bat connectedAndroidTest`
- Compilação debug: `gradlew.bat assembleDebug`

## Cobertura atual

Os testes unitários em `app/src/test` cobrem progresso ponderado e cancelamentos, atraso, meta alcançada, parsing monetário, cotação/conversão BRL↔EUR e simulação financeira com despesas. `app/src/androidTest` verifica persistência Room após fechar e abrir o banco.

## Resultado desta sessão

`test assembleDebug assembleDebugAndroidTest connectedAndroidTest` passou na versão 1.3. No AVD Pixel 10/API 36, 4 testes instrumentados passaram, cobrindo migração 1→2 sem perda, persistência, mesclagem de cenário e contexto do app. O teste Compose de navegação acessível falhou antes da asserção por incompatibilidade do Espresso 3.5.1 com o método removido `InputManager.getInstance`; a inspeção visual/acessibilidade segue pendente. Atualize o stack Espresso antes de reintroduzir esse teste automatizado.
