# Roadmap

Estados: **Preparação**, **Pendente**, **Em andamento**, **Concluído**. Uma fase só muda para concluída após validação registrada.

## Fase 0 — Inspeção e planejamento (Concluído)

- [x] Criar projeto Kotlin/Compose no Android Studio.
- [x] Registrar arquitetura inicial, modelo mínimo, decisões e plano.
- [x] Confirmar sincronização Gradle e build básico.
- [x] Inicializar Git; alterações iniciais permanecem sem commit para revisão.

## Fase 1 — MVP funcional (Concluído)

- [x] Navegação principal e dashboard inicial.
- [x] Tarefas com fase, prioridade, status, prazo e edição/conclusão/exclusão.
- [x] Persistência Room para tarefas, fases, cenário ativo e reserva.
- [x] Reserva financeira, meta configurável, aporte mensal e cálculo de falta/projeção.
- [x] Tema claro/escuro e estados vazios.
- [x] Testes das regras de progresso e finanças (6 testes unitários).
- Critério central: dados cadastrados permanecem após fechar e abrir o app.

## Fase 2 — Planejamento financeiro (Concluído)

- [x] Histórico de aportes e retiradas; operações atualizam a reserva na moeda configurada.
- [x] Orçamento com custos únicos/recorrentes e categorias iniciais; valores separados por moeda.
- [x] Cotação manual BRL/EUR com origem/data e conversão estimada explícita.
- [x] Projeção até a data-alvo, incluindo custos únicos na moeda da reserva.
- [x] Comparação rápida de metas de € 8 mil, € 10 mil e € 12 mil.
- [x] Cenários financeiros independentes editáveis e persistidos, com troca do cenário ativo e dados isolados por cenário.
- [x] Gráfico da evolução projetada da reserva nos próximos seis meses.
- [x] Testes unitários de cotação, conversão e projeção adicionados.

## Fase 3 — Carreira e estudos (Concluído)

- [x] Cadastros locais iniciais para experiências, projetos, formação, idiomas, certificações e marcos.
- [x] Formulários especializados para experiências, projetos, formação, idiomas, certificações e plano de estudo.
- [x] Experiência em desenvolvimento separada, com stack e impacto técnico.
- [x] Kanban para projetos de portfólio com movimentação entre colunas.

## Fase 4 — Universidades e documentos (Concluído)

- [x] Cadastros locais iniciais de universidades, candidaturas, documentos e destinos com detalhes e prazo.
- [x] Campos próprios para universidades, candidaturas, comparação de destino e documentos.
- [x] Fontes oficiais, taxa de candidatura e aproveitamento de créditos registrados no cadastro.
- [x] Checklist documental com alternância entre pendente e concluído.

## Fase 5 — Revisões e notificações (Concluído)

- [x] Revisões trimestrais com respostas, risco e prioridades persistidas.
- [x] Relatório local com histórico de revisões trimestrais, riscos e prioridades.
- [x] Lembretes locais com notificação no Android e prazo salvo no dispositivo.
- [x] Indicador de prontidão calculado para o cenário ativo com pré-requisitos e bloqueios explicados.

## Fase 6 — Backup e acabamento (Concluído)

- [x] Exportação JSON completa pelo seletor de documentos.
- [x] Importação JSON validada e com confirmação antes de substituir os dados.
- [x] Exportação CSV de tarefas, movimentações e custos.
- [x] Migração Room 1→2 criada sem apagar dados existentes.
- [x] Mesclagem de backup JSON validada antes de adicionar cenários e registros sem remover os dados locais.
- [x] Teste instrumentado da migração 1→2 adicionado e compilado.
- [x] `connectedAndroidTest` passou com 5 testes instrumentados no Pixel 10/API 36 (migração, persistência, mesclagem, contexto e navegação/labels legíveis).
- [x] Validação visual manual no AVD Pixel 10: Início, Plano, Finanças, Mais, Ajustes, Backup e seletor de arquivos.
- [x] Navegação e abertura do seletor concluídas sem falha; base de dados vazia no AVD limpo é esperada.

## Próximo incremento

Fases 0–6 foram concluídas em 30/10/2026. Compilação, testes unitários, 5 testes instrumentados e inspeção visual das telas principais passaram. O prompt mestre não define uma Fase 7; o próximo incremento precisa ser priorizado a partir das funcionalidades previstas ainda não cobertas.
