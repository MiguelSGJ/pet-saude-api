# Enriquecimento meteorológico das notificações — Especificação

**Criada em:** 20/10/2026  
**Pontuação de ambiguidade:** 0,19 (limite: ≤ 0,20)  
**Atualizada em:** 21/10/2026
**Requisitos:** 14 bloqueados

## Objetivo

Associar automaticamente todas as notificações existentes e futuras a dados diários de precipitação, umidade relativa e vento do seu bairro, alcançando cobertura combinada superior a 90% e concordância operacional de pelo menos 90% entre Open-Meteo e NASA POWER.

## Estado atual

- `Notification` possui `dataPrimeiroSintoma`, `dataNotification`, `idBairro` e `nomeBairro`, mas nenhum dado meteorológico.
- `Bairro` possui nome e cidade, mas não coordenadas.
- O backend Spring Boot já habilita `@Async` para importações, mas não habilita tarefas agendadas.
- PostgreSQL é usado com JPA e Flyway.
- Não existe cliente meteorológico, tabela de dados diários, vínculo meteorológico na notificação, backfill ou rotina diária.

## Decisões bloqueadas

- Data meteorológica: `dataPrimeiroSintoma`; usar `dataNotification` quando a primeira estiver nula.
- Localização: um ponto configurável por bairro.
- Escopo histórico: desde a menor data de referência encontrada nas notificações existentes.
- Provedores: Open-Meteo primário e NASA POWER secundário.
- Consolidação: usar valor válido da fonte primária e recorrer à secundária apenas quando necessário; não calcular média entre modelos.
- Concordância: comparar valores quando ambos existirem, armazenar diferenças e sinalizar limites excedidos sem trocar silenciosamente a fonte final.
- Periodicidade: processar diariamente o último dia completo e revisar uma janela recente para absorver o atraso da NASA.

## Arquitetura

```text
Open-Meteo archive ─┐
                    ├─> adaptadores normalizados ─> weather_daily ─> notifications.weather_data_id
NASA POWER (3 calls)┘              │                       │
                                   ├─ validação física      ├─ valores finais
                                   ├─ retry/backoff         └─ valores brutos e proveniência
                                   └─ métricas de cobertura

ApplicationReadyEvent ─> backfill assíncrono desde a data mínima
@Scheduled diário ─────> dia anterior + janela de reconciliação
Endpoint administrativo ─> disparo/reprocessamento e consulta de status
```

### Persistência

`bairros` receberá `latitude` e `longitude` editáveis.

`weather_daily` manterá uma linha única por `(bairro_id, weather_date)` contendo:

- coordenadas utilizadas;
- valores finais de precipitação, umidade e vento;
- valores normalizados retornados por Open-Meteo e NASA POWER;
- fonte escolhida por variável;
- horários da última coleta de cada provedor;
- estado da consolidação e mensagem de erro, quando houver.
- diferenças absolutas, flags por variável e estado de concordância entre os provedores.

`notifications.weather_data_id` referenciará `weather_daily`. Várias notificações do mesmo bairro e dia compartilharão a mesma linha meteorológica.

### Fluxo de backfill

1. Calcular a menor data entre `dataPrimeiroSintoma` e o fallback `dataNotification`.
2. Carregar bairros com coordenadas.
3. Dividir o intervalo até ontem em blocos configuráveis.
4. Buscar os dois provedores fora da transação de banco.
5. Validar, normalizar unidades e consolidar cada dia.
6. Fazer `upsert` idempotente em `weather_daily`.
7. Vincular em lote as notificações que tenham o mesmo bairro e data de referência.
8. Registrar cobertura, falhas e duração.

### Fluxo diário

- Executar em `America/Fortaleza`, em horário configurável.
- Processar ontem e reprocessar os sete dias anteriores.
- A janela móvel permite que a NASA complete registros inicialmente preenchidos apenas pela Open-Meteo.
- Uma trava no banco impedirá duas instâncias de executar o mesmo trabalho simultaneamente.

## Requisitos

1. **Duas fontes independentes**: integrar Open-Meteo e NASA POWER para todas as três variáveis.
   - Atual: nenhuma integração existe.
   - Alvo: cada linha diária registra resposta normalizada de ambos os provedores quando disponível.
   - Aceite: teste de integração comprova persistência separada das duas fontes.

2. **Cobertura mínima**: a união dos provedores deve preencher mais de 90% dos valores esperados por variável.
   - Atual: cobertura meteorológica é 0%.
   - Alvo: cobertura calculada por período, bairro e variável.
   - Aceite: verificador falha quando qualquer variável fica em ≤90% e passa para o conjunto piloto medido em 100%.

3. **Data epidemiológica correta**: usar `dataPrimeiroSintoma`, com fallback para `dataNotification`.
   - Atual: nenhum vínculo temporal meteorológico existe.
   - Alvo: toda associação usa essa regra única.
   - Aceite: testes cobrem as duas datas presentes, somente notificação presente e ambas nulas.

4. **Ponto por bairro**: consultar usando coordenadas persistidas do bairro canônico.
   - Atual: bairros não têm coordenadas.
   - Alvo: cada bairro possui latitude/longitude editáveis e rastreáveis.
   - Aceite: dois bairros com coordenadas diferentes geram chaves diárias distintas.

5. **Backfill integral**: processar desde a notificação mais antiga até o último dia completo.
   - Atual: não há backfill.
   - Alvo: o início é descoberto por consulta ao banco, sem ano fixo no código.
   - Aceite: banco de teste com datas conhecidas gera exatamente o intervalo esperado.

6. **Execução assíncrona**: backfill e reprocessamento não devem bloquear requisições HTTP nem a inicialização da aplicação.
   - Atual: somente importações usam `@Async`.
   - Alvo: executor dedicado e limitado executa enriquecimento meteorológico.
   - Aceite: endpoint de disparo responde `202` antes do término do job.

7. **Atualização diária**: novos dias devem ser preenchidos automaticamente.
   - Atual: scheduling não está habilitado.
   - Alvo: rotina configurável processa o dia anterior e reconcilia sete dias.
   - Aceite: teste do serviço agenda o intervalo correto e a segunda execução não duplica linhas.

8. **Idempotência**: reprocessamentos devem atualizar a linha existente.
   - Atual: não há estrutura meteorológica.
   - Alvo: restrição única `(bairro_id, weather_date)` e upsert transacional.
   - Aceite: duas execuções mantêm a mesma contagem e atualizam timestamps/proveniência.

9. **Rastreabilidade**: preservar valores dos dois provedores e a fonte final de cada variável.
   - Atual: impossível auditar origem.
   - Alvo: dados brutos normalizados, valor final, fonte e horário ficam persistidos.
   - Aceite: consulta de uma linha identifica como cada valor final foi escolhido.

10. **Validação**: rejeitar nulos, `-999`, `NaN`, infinito e valores fora dos limites físicos configurados.
    - Atual: nenhuma validação meteorológica existe.
    - Alvo: somente valores válidos participam da consolidação.
    - Aceite: testes unitários cobrem sentinelas e limites das três variáveis.

11. **Resiliência**: falha de um provedor não deve apagar dado válido do outro.
    - Atual: não aplicável.
    - Alvo: timeout, retry com backoff e isolamento por provedor/bairro/bloco.
    - Aceite: simulação de indisponibilidade da NASA ainda persiste Open-Meteo e marca estado parcial.

12. **Observabilidade**: expor status do job e registrar cobertura, duração, erros e contagem de vínculos.
    - Atual: processamento assíncrono imprime exceções diretamente.
    - Alvo: logs estruturados e consulta administrativa de status.
    - Aceite: uma execução concluída informa período, linhas processadas, cobertura e falhas.

13. **Configuração segura**: URLs, timeouts, agenda, fuso, limites e habilitação devem vir de propriedades/ambiente.
    - Atual: não há configuração meteorológica.
    - Alvo: nenhum segredo é necessário para os provedores selecionados; troca de endpoint não exige recompilação.
    - Aceite: testes carregam endpoints mockados por propriedades.

14. **Concordância entre provedores**: medir divergência além da completude.
    - Atual: os valores das duas fontes são armazenados, mas não existe classificação operacional da diferença.
    - Alvo: calcular diferença absoluta por variável, sinalizar precipitação acima de 10 mm, umidade acima de 10 p.p. e vento acima de 15 km/h, com limites configuráveis.
    - Aceite: o endpoint de cobertura informa pares comparados, divergentes, percentual de concordância e limites aplicados; o painel exibe meta mínima de 90%.

## Limites

**Dentro do escopo:**

- Coordenadas persistidas para os bairros existentes.
- Open-Meteo e NASA POWER.
- Precipitação, umidade relativa e vento em granularidade diária.
- Backfill integral, atualização agendada e reprocessamento administrativo.
- Ligação das notificações à linha meteorológica pela data epidemiológica decidida.
- Migrações, testes, configuração Docker e documentação operacional.

**Fora do escopo:**

- Previsões meteorológicas futuras — serão armazenados apenas dias concluídos, evitando misturar previsão com observação/reanálise.
- Dados por endereço, rua ou coordenada do paciente — não existem no modelo e aumentariam risco de privacidade.
- Substituição das fontes por INMET — reservado para uma fase de validação observacional.
- Alterações de dashboards do frontend — os dados ficarão disponíveis no backend, mas a visualização é uma entrega separada.
- Correção estatística ou média entre provedores — exige validação científica própria.

## Restrições

- Java 17, Spring Boot 3.2, PostgreSQL e Flyway existentes.
- Fuso `America/Fortaleza` para definição do dia epidemiológico.
- Chamadas NASA de precipitação, umidade e vento devem ser separadas.
- A rotina precisa respeitar limites e termos dos provedores.
- Coordenadas ausentes impedem somente o bairro afetado e devem aparecer no status; não podem cair silenciosamente para o centro da cidade.
- A cobertura mede campos preenchidos; a concordância mede proximidade entre modelos e não equivale a precisão observacional.

## Critérios de aceite

- [ ] Open-Meteo e NASA POWER são consultadas e seus valores ficam auditáveis separadamente.
- [ ] A cobertura combinada por variável é superior a 90% no ensaio de 730 dias.
- [ ] Todas as notificações com data e bairro válidos, desde a mais antiga, recebem vínculo meteorológico.
- [ ] `dataPrimeiroSintoma` prevalece e `dataNotification` funciona como fallback.
- [ ] Cada bairro usa coordenadas próprias, sem fallback silencioso municipal.
- [ ] Backfill e reprocessamento respondem de modo assíncrono.
- [ ] A rotina diária é idempotente e reconcilia a defasagem recente da NASA.
- [ ] Uma fonte indisponível não impede o uso da outra.
- [ ] Valores inválidos e sentinelas não são persistidos como dados finais.
- [ ] Migrações sobem em banco existente e banco vazio.
- [ ] Testes automatizados do módulo meteorológico passam.
- [ ] Status e métricas permitem identificar cobertura e bairros pendentes.
- [ ] Diferenças entre provedores ficam persistidas e auditáveis por variável.
- [ ] O painel mostra concordância por período e alerta quando ficar abaixo de 90%.

## Relatório de ambiguidade

| Dimensão | Pontuação | Mínimo | Estado | Observação |
|---|---:|---:|---|---|
| Clareza do objetivo | 0,90 | 0,75 | ✓ | Três variáveis, duas APIs e cobertura mensurável |
| Clareza dos limites | 0,82 | 0,70 | ✓ | Escopo histórico, localização e exclusões definidos |
| Clareza das restrições | 0,75 | 0,65 | ✓ | Assíncrono, diário, fuso e proveniência definidos |
| Critérios de aceite | 0,70 | 0,70 | ✓ | Critérios binários definidos |
| **Ambiguidade** | **0,19** | **≤0,20** | **✓** | Gate aprovado |

## Registro da entrevista

| Rodada | Perspectiva | Decisão bloqueada |
|---|---|---|
| 1 | Pesquisador | Usar `dataPrimeiroSintoma` com fallback para `dataNotification` |
| 1 | Pesquisador | Manter um ponto meteorológico por bairro |
| 1 | Pesquisador | Fazer backfill desde a notificação mais antiga existente |
| Gate | Aprovação | Gerar pesquisa e Spec e, em seguida, implementar |

