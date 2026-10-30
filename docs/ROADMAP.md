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

## Fase 3 — Carreira e estudos (Em andamento)

- [x] Cadastros locais iniciais para experiências, projetos, formação, idiomas, certificações e marcos.
- [ ] Formulários especializados, distinção de experiência em desenvolvimento, Kanban e plano de estudo.

## Fase 4 — Universidades e documentos (Em andamento)

- [x] Cadastros locais iniciais de universidades, candidaturas, documentos e destinos com detalhes e prazo.
- [ ] Campos próprios, fontes oficiais, aproveitamento de créditos e checklist documental completo.

## Fase 5 — Revisões e notificações (Em andamento)

- [x] Revisões trimestrais com respostas, risco e prioridades persistidas.
- [ ] Comparação de indicadores, relatórios, lembretes locais e prontidão por cenário.

## Fase 6 — Backup e acabamento (Em andamento)

- [x] Exportação JSON completa pelo seletor de documentos.
- [x] Importação JSON validada e com confirmação antes de substituir os dados.
- [x] Exportação CSV de tarefas, movimentações e custos.
- [x] Migração Room 1→2 criada sem apagar dados existentes.
- [ ] Teste instrumentado da migração, mesclagem, acessibilidade e validação final em emulador.

## Próximo incremento

Fase 2 concluída em 30/10/2026 após validar `test assembleDebug`. A Fase 3 não foi iniciada nesta entrega e depende de autorização explícita do usuário.
