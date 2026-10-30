# Arquitetura

## Estado atual

Fases 0 e 1 e o incremento das Fases 2–6 usam um único módulo Android Kotlin/Compose. A apresentação navega por Visão geral, Plano, Finanças, Mais (cadastros/revisões/backup) e Ajustes. ViewModel coordena estados e ações; `domain` mantém regras financeiras/progresso; `data` mantém Room, DAOs, repositório e DataStore.

## Direção para o MVP

Manter separação simples por responsabilidade:

- `presentation`: telas Compose, componentes e ViewModels/estado de tela.
- `domain`: modelos necessários às regras e cálculos puros.
- `data`: entidades Room, DAOs, banco e repositórios que expõem Flow.
- `core`: formatação/validação compartilhada apenas quando houver reutilização real.

Room é a fonte de verdade para tarefas, fases, cenário, reserva, transações, custos, cotações manuais, registros de planejamento e revisões. DataStore fica limitado à preferência de tema. A base foi elevada para v2 por migração explícita. Cálculos de domínio não dependem de Compose nem Android. JSON e CSV são exportados por seletores de documentos do Android; a importação substitui dados somente após validar e confirmar.

## Navegação

Destinos disponíveis: Visão geral, Plano, Finanças, Mais e Ajustes. A tela Mais usa um tipo de registro flexível para as áreas ainda em evolução; os formulários especializados continuam pendentes.

## Dependências

O projeto usa Compose/Material 3, Activity Compose, Navigation Compose, Room (KSP), DataStore, coroutines/Flow e JUnit. Sem Hilt; sem biblioteca de gráficos até existir visualização que justifique a dependência. Flags AGP de compatibilidade com Kotlin/KSP estão temporariamente desativadas em `gradle.properties` e marcadas como deprecated pelo AGP; revisar ao atualizar o plugin.
