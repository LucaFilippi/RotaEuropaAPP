# Instruções para agentes

Antes de alterar o projeto:

1. Leia este arquivo, `README.md`, `docs/ROADMAP.md` e `docs/SESSION_LOG.md`.
2. Confira `git status` e leia as decisões em `docs/DECISIONS.md`.
3. Preserve implementações funcionais e mantenha as mudanças focadas.
4. Não introduza dependências sem verificar compatibilidade e necessidade.
5. Não adicione rede, login, backend, telemetria ou sincronização em nuvem sem solicitação explícita.
6. Mantenha dados pessoais no dispositivo e todas as datas de mudança configuráveis.
7. Atualize a documentação e o log da sessão ao concluir uma entrega.
8. Execute apenas validações pertinentes; nunca declare build ou testes aprovados sem executá-los.
9. Registre limitações e próximas ações. Não apresente dados de exemplo como dados reais.
10. Não altere arquivos fora do escopo sem justificativa.

## Direção arquitetural

- Aplicativo nativo em Kotlin, Jetpack Compose e Material 3.
- Evoluir para MVVM com separação entre apresentação, domínio e dados sem criar abstrações prematuras.
- Room como fonte de verdade para dados estruturados offline; DataStore para preferências simples.
- Coroutines e Flow para operações assíncronas.
- Version Catalog (`gradle/libs.versions.toml`) para versões centralizadas.
- Sem Hilt inicialmente; avaliar quando a composição manual deixar de ser simples.

## Fluxo de trabalho

Manter entregas pequenas e compiláveis. Antes de finalizar, revisar o diff, atualizar os documentos afetados e anotar resultados reais no `docs/SESSION_LOG.md`.
