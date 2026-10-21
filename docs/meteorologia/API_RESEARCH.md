# Pesquisa de APIs meteorológicas

**Data da pesquisa:** 20/10/2026  
**Atualização dos critérios de concordância:** 21/10/2026
**Local do ensaio inicial:** Mossoró/RN (`-5.1875`, `-37.3442`)
**Validação ampliada:** 30 pontos de bairro cadastrados em Mossoró/RN
**Período avaliado:** 20/10/2024 a 19/10/2026 (730 dias completos)  
**Variáveis:** precipitação diária, umidade relativa média e velocidade média do vento a 10 m

## Objetivo

Selecionar no mínimo duas fontes independentes capazes de enriquecer as notificações epidemiológicas com dados meteorológicos diários, alcançar cobertura combinada superior a 90% e manter divergência agregada aceitável entre os valores retornados.

## Fontes pesquisadas

### Open-Meteo Historical Weather API

- Endpoint histórico: `https://archive-api.open-meteo.com/v1/archive`.
- Variáveis escolhidas: `precipitation_sum`, `relative_humidity_2m_mean` e `wind_speed_10m_mean`.
- Retorno diário em fuso configurável, usado com `America/Fortaleza`.
- Combina reanálises ERA5/ERA5-Land e, para períodos recentes, modelos ECMWF IFS. A documentação informa séries globais sem lacunas, resolução de aproximadamente 9 km nos dados recentes e ERA5/ERA5-Land com atraso de cerca de cinco dias.
- A licença gratuita pública é voltada a uso não comercial e exige atribuição. Uma implantação comercial precisa contratar o plano apropriado ou hospedar o serviço.

Documentação oficial:

- https://open-meteo.com/en/docs/historical-weather-api
- https://open-meteo.com/en/terms

### NASA POWER Daily API

- Endpoint: `https://power.larc.nasa.gov/api/temporal/daily/point`.
- Variáveis escolhidas: `PRECTOTCORR`, `RH2M` e `WS10M`.
- Fonte independente baseada principalmente em MERRA-2/GEOS-IT e produtos de precipitação da NASA.
- Disponibilidade diária desde 1981 até próximo do tempo real; a documentação informa que a extensão de baixa latência normalmente fica pronta em aproximadamente dois dias.
- Preenchimento ausente é representado por `-999` e precisa ser descartado.
- A resolução de origem é mais grossa que a da Open-Meteo. Bairros próximos podem compartilhar a mesma célula da NASA, apesar de cada registro manter o ponto solicitado.

Documentação oficial:

- https://power.larc.nasa.gov/docs/services/api/temporal/daily/
- https://power.larc.nasa.gov/docs/methodology/data/sources/
- https://power.larc.nasa.gov/docs/methodology/meteorology/assessment/

### INMET

O INMET foi considerado como fonte observacional brasileira. Ele publica dados de estações, inclusive precipitação, umidade e vento. Porém, o próprio Instituto informa que dados automáticos podem conter `9999`, `Null` ou campos vazios, não passam por validação antes da publicação e os históricos são oferecidos principalmente pelo portal/BDMEP, com atualização informada de até 90 dias. Não foi selecionado como uma das duas integrações automáticas desta fase porque não há contrato de API pública estável documentado equivalente aos dois provedores escolhidos.

O INMET continua recomendado para validações amostrais e futuras auditorias observacionais.

Referências oficiais:

- https://portal.inmet.gov.br/noticias/saiba-como-acessar-os-dados-meteorol%C3%B3gicos-dispon%C3%ADveis-no-site-do-inmet
- https://portal.inmet.gov.br/servicos/bdmep-dados-historicos

### Visual Crossing

A API oferece histórico e previsão no mesmo endpoint, incluindo precipitação, umidade e vento, mas exige chave e possui limites associados ao plano contratado. É uma alternativa comercial de contingência caso os termos ou a disponibilidade da Open-Meteo mudem.

Documentação oficial: https://www.visualcrossing.com/resources/documentation/weather-api/timeline-weather-api/

## Metodologia do ensaio

1. Foi utilizado um intervalo móvel de 730 dias completos, encerrado no dia anterior à pesquisa.
2. As duas APIs foram consultadas para o mesmo ponto e período.
3. Um valor foi considerado disponível quando era numérico, não nulo e diferente do sentinela `-999` da NASA.
4. A cobertura foi calculada separadamente por variável e provedor.
5. A cobertura combinada considera o dia preenchido quando ao menos um provedor possui valor válido.
6. Vento da NASA (`m/s`) foi convertido para `km/h` apenas para a comparação com a Open-Meteo.
7. A comparação inicial usou os 729 dias disponíveis nas duas fontes; em 21/10/2026, a validação ampliada usou 21.900 pares bairro-dia (30 bairros × 730 dias).
8. A precipitação NASA foi solicitada em intervalos que não misturam as fontes internas MERRA-2 e POWER/GEOS-IT.

## Resultado de completude

| Variável | Open-Meteo | NASA POWER | Combinada | Meta |
|---|---:|---:|---:|---:|
| Precipitação | 730/730 (100%) | 729/730 (99,863%) | 730/730 (100%) | >90% |
| Umidade relativa | 730/730 (100%) | 729/730 (99,863%) | 730/730 (100%) | >90% |
| Vento a 10 m | 730/730 (100%) | 729/730 (99,863%) | 730/730 (100%) | >90% |

O único dia ausente na NASA foi 19/10/2026, compatível com a defasagem próxima do tempo real documentada pelo provedor. A Open-Meteo preencheu esse dia nas três variáveis. A combinação atingiu 100%, superando a meta de 90%.

## Comparação entre os valores

| Variável | Dias comparados | Erro absoluto médio | Viés Open-Meteo − NASA | Correlação de Pearson |
|---|---:|---:|---:|---:|
| Precipitação | 729 | 1,713 mm | +0,960 mm | 0,634 |
| Umidade relativa | 729 | 4,116 p.p. | +3,170 p.p. | 0,912 |
| Vento | 729 | 9,202 km/h | −9,202 km/h | 0,908 |

As diferenças não significam necessariamente erro: os provedores usam modelos, grades e métodos distintos. Por isso, os valores não serão calculados pela média entre fontes. A Open-Meteo será a fonte primária; a NASA será armazenada como fonte independente de comparação e utilizada como fallback quando a primária estiver ausente ou inválida.

### Validação ampliada nos 30 bairros

| Variável | Pares bairro-dia | Erro absoluto médio | Viés Open-Meteo − NASA | Correlação | Dentro do limite operacional |
|---|---:|---:|---:|---:|---:|
| Precipitação | 21.900 | 1,628 mm | +0,860 mm | 0,636 | 96,47% |
| Umidade relativa | 21.900 | 4,094 p.p. | +2,922 p.p. | 0,909 | 95,44% |
| Vento | 21.900 | 9,219 km/h | −9,219 km/h | 0,910 | 100% |

Todos os 30 bairros passaram no gate agregado e mantiveram pelo menos 90% dos pares dentro do limite operacional das três variáveis. A variação entre bairros foi pequena porque a cidade ocupa área menor que a grade da NASA; a Open-Meteo ainda diferencia as células mais finas usadas por cada ponto.

### Critério de concordância para seleção

A seleção não considerou somente completude. Além dos 729 pares diários, foram usados erro absoluto médio, viés e correlação. Para tornar “não divergir muito” uma regra verificável, foram adotados os seguintes limites agregados para o ensaio piloto:

| Variável | Limite de erro absoluto médio | Correlação mínima | Resultado observado | Decisão |
|---|---:|---:|---:|---|
| Precipitação | ≤ 3 mm | ≥ 0,60 | 1,713 mm / 0,634 | Aprovada, com cautela para eventos diários localizados |
| Umidade relativa | ≤ 5 p.p. | ≥ 0,85 | 4,116 p.p. / 0,912 | Aprovada |
| Vento a 10 m | ≤ 10 km/h | ≥ 0,85 | 9,202 km/h / 0,908 | Aprovada, com viés sistemático monitorado |

As três variáveis passaram pelo gate agregado. Isso não significa que todo dia terá valores próximos. Precipitação diária é especialmente sensível à grade e ao horário do evento; a própria validação da NASA mostra desempenho inferior para precipitação diária em comparação com agregações maiores. O vento apresentou boa correlação, mas um deslocamento sistemático relevante, coerente com diferenças de grade, rugosidade e modelagem.

### Controle operacional por registro

Cada linha diária em que os dois provedores possuem valor será comparada e armazenará a diferença absoluta. Os limites iniciais, configuráveis por ambiente, são:

- precipitação: divergente quando a diferença for maior que `10 mm/dia`;
- umidade relativa: divergente quando a diferença for maior que `10 pontos percentuais`;
- vento a 10 m: divergente quando a diferença for maior que `15 km/h`.

O painel mede a proporção de pares dentro desses limites, com meta operacional de pelo menos 90% de concordância. Um valor divergente não será descartado nem misturado por média, pois nenhuma das duas reanálises representa verdade observacional por si só. O sistema mantém a Open-Meteo como fonte final primária, preserva os dois valores e sinaliza a divergência para auditoria. Se a concordância cair abaixo de 90%, a fonte e o período devem ser revisados antes de usar os dados em análises epidemiológicas sensíveis.

## Descobertas críticas na NASA POWER

Ao solicitar `PRECTOTCORR`, `RH2M` e `WS10M` juntos para o intervalo completo, a precipitação retornou valores incompatíveis com consultas isoladas do mesmo dia. Consultas contendo somente `PRECTOTCORR` produziram valores plausíveis e coerentes com a unidade declarada (`mm/day`). Umidade e vento não apresentaram essa diferença.

Na ampliação por bairro, foi encontrada uma segunda condição: uma consulta isolada de `PRECTOTCORR` que atravessa a transição entre as fontes internas declaradas no cabeçalho (`MERRA2` e `POWER`) pode retornar precipitação exatamente 24 vezes maior, ainda declarando `mm/day`. Consultas subdivididas, cada uma com uma única fonte no cabeçalho, retornaram valores coerentes. Portanto, o cliente não divide valores por 24; ele descarta a resposta mista e subdivide o intervalo até obter respostas com uma única fonte.

Consequência para a implementação:

- A integração NASA fará requisições isoladas por variável.
- Respostas de precipitação com múltiplas fontes internas serão subdivididas recursivamente antes do uso.
- Valores fora dos limites físicos configurados serão rejeitados.
- Nenhum fator de correção empírico será aplicado.
- Testes de contrato devem detectar se respostas individuais e multiparâmetro voltarem a divergir.

## Localização por bairro

Foi decidido usar um ponto por bairro. Os pontos iniciais foram obtidos uma única vez no OpenStreetMap/Nominatim, com cache no banco e sem consulta de geocodificação durante a rotina diária. A extração respeitou o limite público de uma requisição por segundo. Coordenadas permanecem editáveis para permitir correção administrativa.

Política oficial: https://operations.osmfoundation.org/policies/nominatim/

Devido à resolução dos modelos, o valor representa a célula meteorológica que contém o ponto do bairro, não uma medição feita dentro de cada rua. Em especial, vários bairros podem receber o mesmo valor da NASA.

## Recomendação

Usar:

1. **Open-Meteo como fonte primária**, por sua resolução recente mais fina, atualização frequente e cobertura integral observada.
2. **NASA POWER como fonte secundária**, para redundância, comparação e fallback, aceitando a defasagem próxima do tempo real.
3. **INMET como referência observacional de auditoria**, sem dependência operacional nesta fase.

O sistema deve armazenar os valores de cada provedor e o valor final selecionado, com data, bairro, coordenadas, unidade, fonte e horário da coleta. A completude será recalculada após cada execução e deve permanecer acima de 90% por variável.

A concordância entre os provedores também será recalculada após cada execução. O requisito de qualidade passa a ser duplo: completude superior a 90% e concordância operacional de pelo menos 90% entre os pares disponíveis, conforme limites configuráveis por variável.

