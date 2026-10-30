# Modelo de dados do MVP e evolução

O banco Room v2 mantém as entidades do MVP e adiciona transações, custos, cotações manuais, registros flexíveis de planejamento e revisões. A migração 1→2 cria apenas tabelas novas e preserva cenário, fases, tarefas e reserva. A Fase 2 usa a tabela `migration_scenarios` existente para criar cenários independentes sem nova migração.

## Entidades iniciais

- **MigrationScenario**: configuração de cada cenário, ativo selecionado, nome/destino e data-alvo opcional. Tarefas, fases, reserva, movimentações, orçamento e cotação são associados ao seu ID.
- **Phase**: `id`, cenário, nome e ordem; quatro fases iniciais editáveis são apenas um ponto de partida.
- **Task**: `id`, fase, título, descrição, categoria, prioridade, status, prazo opcional, datas de criação/conclusão, peso estratégico (1–5) e sinalizador obrigatório.
- **TaskDependency** (futuro próximo): pares tarefa pré-requisito/tarefa dependente; pendências sinalizadas sem bloquear ações paralelas.
- **Reserve**: reserva do cenário, saldo atual, meta, aporte mensal e moeda; saldo diretamente editável até a fase de transações.
- **FinancialTransaction**: `id`, cenário, valor inteiro em unidade menor, moeda original, tipo (aporte/retirada), data e observação. Não somar moedas diferentes sem taxa explícita.
- **ExchangeRate** (fase 2): cotação manual BRL/EUR, data, origem e observação.
- **BudgetItem**: nome, categoria, valor em unidade mínima, moeda e recorrência.
- **JourneyRecord**: tipo, título, campos específicos serializados como linhas rotuladas em `details`, status, prazo e atualização; formulários distintos cobrem carreira, desenvolvimento, projetos, estudo, universidades, candidaturas, documentos, destinos e lembretes.
- **QuarterlyReview**: data, respostas, risco e prioridades futuras.
- **Backup JSON v1**: envelope com versão/formato e arrays de entidades; importação valida relações e valores antes de substituir ou mesclar. Na mesclagem, cenários recebem IDs novos e os dados existentes são preservados. CSV exporta tarefas, transações e custos.

## Integridade e tipos

Usar IDs estáveis, chaves estrangeiras e índices em relações/filtros relevantes. Valores financeiros nunca usam `Float`/`Double`: armazenar unidades menores em `Long`, com moeda explícita. Datas de calendário como `LocalDate` convertidas por TypeConverter ou epoch day; instantes como `Instant`/epoch millis. Exibição formatada apenas na camada de apresentação.

Progresso operacional será ponderado por importância configurável. Prontidão para mudança será um indicador separado, exigindo condições obrigatórias verificadas; progresso alto não implica prontidão.

## Limites do modelo atual

Os cenários podem ser criados e alternados; o cenário padrão ID 1 preserva os dados das versões anteriores. Relações/tipos específicos para carreiras, universidades e documentos, requisitos de prontidão, lembretes e anexos ainda não estão implementados. Alterações de esquema exigem migrações explícitas e testes instrumentados.
