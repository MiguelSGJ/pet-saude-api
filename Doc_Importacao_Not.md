# Documentação de Importação de Notificações

## Formatos de Arquivo Suportados

| Formato | Endpoint | Comportamento |
|---------|----------|---------------|
| `.xlsx` | `POST /api/uploadXlsx` | Assíncrono — retorna `202 Accepted` imediatamente |
| `.dbf`  | `POST /api/uploadDbf`  | Assíncrono — retorna `202 Accepted` imediatamente |
| `.csv`  | `POST /api/uploadCsv`  | Assíncrono — retorna `202 Accepted` imediatamente |

Todos os endpoints recebem o arquivo via `multipart/form-data` com a chave `file`.

---

## Colunas Aceitas

A tabela abaixo descreve todas as colunas que o sistema reconhece. **O nome da coluna deve ser exatamente igual** (maiúsculas) em qualquer formato enviado.

| Coluna | Tipo | Obrigatório | Descrição |
|--------|------|-------------|-----------|
| `NU_NOTIFIC` | Long | Não¹ | Número único da notificação no SINAN |
| `ID_AGRAVO` | String | Sim | Código CID-10: `A90` (Dengue), `A92.0` (Chikungunya), `A928` (Zika) |
| `DT_SIN_PRI` | Data | Não | Data dos primeiros sintomas — formato `dd/MM/yyyy` |
| `DT_NASC` | Data | Não | Data de nascimento do paciente — formato `dd/MM/yyyy` |
| `CLASSI_FIN` | String | Não | Classificação epidemiológica final |
| `CS_SEXO` | String | Não | Sexo: `M` = Masculino, `F` = Feminino |
| `NM_BAIRRO` | String | Não | Nome do bairro de residência |
| `ID_BAIRRO` | Integer | Não | Código numérico do bairro |
| `EVOLUCAO` | String | Não | Código do desfecho do caso |
| `NU_IDADE_N` | Integer | Não | Idade no formato SINAN (ex: `4027` = 27 anos) |

> ¹ `NU_NOTIFIC` é obrigatório internamente como chave primária. Se ausente (comum em arquivos `.dbf`), o sistema gera IDs sequenciais a partir do maior ID já existente no banco.

**Colunas extras no arquivo são ignoradas.** Só as listadas acima são processadas.

---

## Particularidades por Formato

### XLSX
- Lido com Apache POI (`XSSFWorkbook`)
- Primeira linha é o cabeçalho
- Suporta células com fórmulas — são avaliadas antes de ler o valor
- Datas podem estar como número serial do Excel ou string `dd/MM/yyyy`

### DBF
- Lido com `javadbf` (`com.github.albfernandez:javadbf:1.14.0`)
- Nomes de colunas ficam no header do arquivo, não em uma linha de dados
- Charset detectado automaticamente pelo Language Driver ID do header (byte 29)
- Campos numéricos retornados como `Double` — convertidos para inteiro internamente
- `NU_NOTIFIC` não existe nos exports padrão do SINAN: IDs gerados automaticamente

### CSV
- Lido com OpenCSV
- Primeira linha é o cabeçalho
- **Separador detectado automaticamente** entre `;`, `,`, `\t` e `|` — escolhe o que aparecer mais vezes na primeira linha
- Charset: ISO-8859-1 (padrão de exports do SINAN)

---

## Campos Calculados Automaticamente

Estes campos não precisam estar no arquivo — o sistema os calcula:

| Campo | Calculado a partir de | Regra |
|-------|-----------------------|-------|
| `semanaEpidemiologica` | `DT_SIN_PRI` | Semana epidemiológica brasileira (inicia no domingo) |
| `idadePaciente` | `NU_IDADE_N` ou (`DT_SIN_PRI` − `DT_NASC`) | Usa `NU_IDADE_N` se disponível; senão calcula pela diferença de datas |

### Formato `NU_IDADE_N` (padrão SINAN)
O valor é um inteiro de 4 dígitos: os 3 últimos são o valor, o primeiro indica a unidade.

| Prefixo | Unidade |
|---------|---------|
| `4` | Anos |
| `3` | Meses |
| `2` | Dias |
| `1` | Horas |

Exemplo: `4027` = 27 anos. O sistema extrai a idade em anos apenas quando o prefixo é `4`; nos demais casos salva `0`.

---

## Tratamento de Dados Ausentes

| Campo ausente | Comportamento |
|---------------|---------------|
| `NU_NOTIFIC` | ID gerado sequencialmente (`max(id) + 1` para cada registro sem ID) |
| `ID_BAIRRO` | Salvo como `0` |
| `NU_IDADE_N` + `DT_NASC` ambos ausentes | Idade salva como `999` (indica desconhecida) |
| `NU_IDADE_N` ausente mas `DT_NASC` presente | Idade calculada pela diferença entre `DT_NASC` e `DT_SIN_PRI` |
| Qualquer outro campo | Salvo como `null` — não impede o registro de ser salvo |

---

## Tratamento de Erros de Registro

O processamento **não para em caso de erro individual**. Para cada registro:

1. Tenta converter e salvar normalmente
2. Se a conversão falhar, o registro é descartado silenciosamente (erro logado no console)
3. Se o registro for salvo mas tiver inconsistências (bairro inválido, agravo inválido, etc.), é gravado na tabela `notifications_with_error` **e também** na tabela principal

Cada importação recebe um número de **iteração** — permite identificar quais erros vieram de qual lote.

### Consultar erros da última importação
```
GET /api/notifications/errors
```

---

## Endpoints de Consulta

| Endpoint | Descrição |
|----------|-----------|
| `POST /api/uploadXlsx` | Importar arquivo `.xlsx` |
| `POST /api/uploadDbf` | Importar arquivo `.dbf` |
| `POST /api/uploadCsv` | Importar arquivo `.csv` |
| `GET /api/notifications` | Listagem paginada de notificações |
| `GET /api/notifications/count` | Total de notificações por agravo |
| `GET /api/notifications/count/sexo` | Contagem por sexo |
| `GET /api/notifications/count/epidemiologicalWeek` | Contagem por semana epidemiológica |
| `GET /api/notifications/count/ageRange` | Contagem por faixa etária |
| `GET /api/notifications/count/neighborhood` | Contagem por bairro |
| `GET /api/notifications/count/evolucao` | Contagem por evolução do caso |
| `GET /api/notifications/errors` | Notificações com erro da última importação |
