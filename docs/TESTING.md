# Testes e validação

## Como executar

No Windows, com Android SDK e JDK configurados:

- Testes unitários: `gradlew.bat test`
- Testes instrumentados (emulador/dispositivo): `gradlew.bat connectedAndroidTest`
- Compilação debug: `gradlew.bat assembleDebug`

## Cobertura atual

Os testes unitários em `app/src/test` cobrem progresso ponderado e cancelamentos, atraso, meta alcançada, parsing monetário, cotação/conversão BRL↔EUR e simulação financeira com despesas. `app/src/androidTest` verifica persistência Room após fechar e abrir o banco.

## Resultado desta sessão

Na versão 1.1, `test assembleDebug` passou antes do último ajuste visual de conversão por cotação manual. Reexecutar depois de qualquer retomada. O AVD Pixel 10 está configurado com renderização Software, mas nesta sessão o emulador não iniciou: o sandbox negou a escrita do arquivo de bloqueio em `%USERPROFILE%\.android\emu-last-feature-flags.protobuf.lock`. Portanto, o teste instrumentado da migração 1→2 e a verificação manual das novas telas não foram executados nesta sessão.
