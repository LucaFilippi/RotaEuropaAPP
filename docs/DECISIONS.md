# Decisões arquiteturais

## D-001 — Android nativo com Kotlin e Compose

- Estado: Aceita.
- Contexto: aplicativo pessoal Android, offline e com interface moderna.
- Decisão: Kotlin, Jetpack Compose, Material 3 e Gradle Kotlin DSL, conforme o template do Android Studio.
- Consequência: UI declarativa e stack nativa; versões iniciais dependem do ambiente instalado e serão validadas antes de evoluir.

## D-002 — Armazenamento local como padrão

- Estado: Aceita.
- Decisão: Room para dados relacionais e DataStore somente para preferências; nenhuma conta, backend, telemetria ou sincronização na primeira versão.
- Consequência: dados ficam no dispositivo; backup/exportação explícita será planejada em fase posterior.

## D-003 — Começar com um módulo e sem Hilt

- Estado: Aceita.
- Decisão: manter o módulo `app`, separar responsabilidades por pacotes e compor dependências manualmente no início.
- Justificativa: MVP individual não precisa de módulos ou DI avançada antes de a complexidade demonstrar essa necessidade.

## D-004 — Dinheiro e cenários configuráveis

- Estado: Aceita.
- Decisão: datas-alvo e metas não ficam fixas no código. Valores serão inteiros em unidade menor e cada valor carrega moeda. Conversão só usa taxa manual identificada por data/origem.
- Consequência: evita misturar BRL/EUR e evita falsa precisão de ponto flutuante.

## D-005 — Preparação permanece na Fase 0

- Estado: Aceita.
- Contexto: o prompt inicial define explicitamente a primeira tarefa como inspeção/planejamento e limita a execução a preparações seguras.
- Decisão: criar o esqueleto/documentação e validar o ambiente antes de implementar funcionalidades do MVP.

## D-006 — Expansão em incrementos usando os dados locais existentes

- Estado: Aceita.
- Contexto: iniciar Fases 2–6 sem reescrever a Fase 1 nem descartar os dados já salvos no aparelho.
- Decisão: adicionar tabelas Room na migração 1→2; usar registros flexíveis provisórios para áreas que ainda não têm formulário especializado; exportar JSON/CSV no dispositivo.
- Consequência: a base existente permanece; cenários independentes, campos detalhados, notificações e relatórios completos continuam explicitamente pendentes.

## D-007 — Fluxo Git para desenvolvimento

- Estado: Aceita.
- Contexto: o usuário solicitou continuidade versionada por branches no repositório `LucaFilippi/RotaEuropaAPP`.
- Decisão: usar `develop` como integração, `feature/*` para incrementos e reservar `main` para a versão de desenvolvimento final/release. Publicar o trabalho autorizado em branches remotas.
- Consequência: continuar com commits pequenos e atualizar o arquivo de retomada; não tratar funcionalidades parciais como finalizadas.
