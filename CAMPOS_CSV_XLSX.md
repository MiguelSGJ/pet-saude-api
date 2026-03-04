# Documentação dos Campos dos Arquivos CSV e XLSX

## Visão Geral

Este documento descreve os campos utilizados nos arquivos CSV e XLSX que são processados pelo sistema ConectaDengue para importação de dados de notificações de arboviroses (Dengue, Zika e Chikungunya).

## Formatos de Arquivo Suportados

- **CSV**: Arquivos de texto separados por vírgula (`.csv`)
- **XLSX**: Planilhas do Microsoft Excel (`.xlsx`)

## Campos Obrigatórios

### Campos Principais da Notificação

| Campo | Tipo | Descrição | Exemplo | Observações |
|-------|------|-----------|---------|-------------|
| `NU_NOTIFIC` | Long | Número único da notificação | `500` | Campo obrigatório, usado como ID primário |
| `ID_AGRAVO` | String | Código CID-10 do agravo/doença | `A90`, `A92.0`, `A928` | **A90** = Dengue, **A92.0** = Chikungunya, **A928** = Zika |
| `DT_NOTIFIC` | Date | Data da notificação | `10/10/2006` | Formato: dd/MM/yyyy |
| `DT_NASC` | Date | Data de nascimento do paciente | `15/05/1980` | Formato: dd/MM/yyyy |
| `CLASSI_FIN` | String | Classificação final do caso | `100` | Código da classificação epidemiológica |
| `CS_SEXO` | String | Sexo do paciente | `M`, `F` | **M** = Masculino, **F** = Feminino |
| `NM_BAIRRO` | String | Nome do bairro de residência | `ABOLIÇÃO` | Nome será validado contra bairros de Mossoró |
| `ID_BAIRRO` | Integer | Código numérico do bairro | `1`, `27` | Pode ser vazio (0 se não informado) |
| `EVOLUCAO` | String | Evolução do caso | `1`, `2` | Código do desfecho do caso |
| `IDADE` | Integer | Idade do paciente | `27`, `42.5` | Pode conter decimais (ex: 27.5) |

## Processamento dos Campos

### 1. Data de Nascimento (`DT_NASC`) e Data de Notificação (`DT_NOTIFIC`)
- **Formato**: dd/MM/yyyy
- **Processamento**: Convertidas para objetos `Date` usando a classe `StringToDateCSV`
- **Uso**: Cálculo automático da idade quando o campo `IDADE` não está disponível

### 2. Idade (`IDADE`)
- **Processamento**: 
  - Se o campo contém ponto decimal (ex: `27.5`), apenas a parte inteira é considerada
  - Se o campo está vazio ou é 0, a idade é calculada automaticamente pela diferença entre `DT_NOTIFIC` e `DT_NASC`
- **Resultado**: Valor inteiro representando anos completos

### 3. Código do Agravo (`ID_AGRAVO`)
- **Códigos aceitos**:
  - `A90`: Dengue
  - `A92.0`: Chikungunya  
  - `A928`: Zika
- **Validação**: O sistema possui conversão automática de nomes para códigos:
  - "DENGUE" → "A90"
  - "CHIKUNGUNYA" → "A92.0"
  - "ZIKA" → "A928"

### 4. Bairro (`NM_BAIRRO`)
- **Validação**: O nome do bairro é validado contra uma lista de bairros de Mossoró
- **Processamento**: Usa a classe `NeighborhoodsMossoro` para buscar e normalizar nomes de bairros
- **Comportamento**: Apenas notificações com bairros válidos são processadas

### 5. Semana Epidemiológica
- **Campo**: Calculado automaticamente (não precisa estar no arquivo)
- **Cálculo**: Baseado na `DT_NOTIFIC`, calcula-se a semana epidemiológica seguindo o padrão:
  - Encontra o primeiro domingo do ano
  - Conta quantas semanas se passaram desde então
  - Adiciona 1 para obter o número da semana

## Exemplo de Estrutura de Arquivo

### CSV
```csv
NU_NOTIFIC,ID_AGRAVO,DT_NOTIFIC,DT_NASC,CLASSI_FIN,CS_SEXO,NM_BAIRRO,ID_BAIRRO,EVOLUCAO,IDADE
500,A90,10/10/2006,10/10/2006,100,M,ABOLIÇÃO,1,1,27
501,A92.0,15/03/2023,20/01/1985,200,F,CENTRO,5,2,38.5
```

### XLSX
A primeira linha deve conter os cabeçalhos (nomes dos campos) e as linhas subsequentes os dados.

## Tratamento de Erros

### Notificações com Erro
- Notificações que falham na validação são armazenadas na tabela `notifications_with_error`
- Campos que causam erros comuns:
  - Datas em formato inválido
  - Bairros não encontrados na base de Mossoró
  - Códigos de agravo inválidos
  - Campos obrigatórios vazios

### Log de Iterações
- Cada importação recebe um número de iteração único
- Permite rastreamento de quando cada dado foi importado
- Facilita identificação de erros por lote de importação

## Campos Calculados/Derivados

| Campo Derivado | Origem | Descrição |
|----------------|--------|-----------|
| `semanaEpidemiologica` | `DT_NOTIFIC` | Semana epidemiológica baseada na data de notificação |
| `idadePaciente` | `IDADE` ou (`DT_NOTIFIC` - `DT_NASC`) | Idade em anos, calculada se não fornecida |

## Filtros e Consultas Disponíveis

O sistema permite filtrar dados por:
- **Ano**: Baseado na `DT_NOTIFIC`
- **Agravo**: Dengue, Zika ou Chikungunya
- **Bairro**: Nome específico do bairro
- **Sexo**: Masculino ou Feminino
- **Evolução**: Código de desfecho do caso
- **Faixa Etária**: Intervalos pré-definidos de idade

## Validações Importantes

1. **Formato de Data**: Deve seguir rigorosamente dd/MM/yyyy
2. **Bairros**: Devem existir na base de dados de Mossoró
3. **Códigos CID**: Apenas A90, A92.0 e A928 são aceitos
4. **Sexo**: Apenas 'M' ou 'F'
5. **Campos Numéricos**: NU_NOTIFIC, ID_BAIRRO, IDADE devem ser válidos

## Notas Técnicas

- **Codificação**: Arquivos devem estar em UTF-8
- **Separador CSV**: Vírgula (,)
- **Células Vazias**: Tratadas como strings vazias ou valores padrão (0 para numéricos)
- **Fórmulas XLSX**: São avaliadas e o resultado é usado como valor
- **Linhas de Cabeçalho**: A primeira linha é sempre considerada cabeçalho

## Endpoints da API

O sistema expõe diversos endpoints para consulta dos dados importados:

- `POST /api/savecsvdata`: Upload de arquivos CSV/XLSX
- `GET /api/notifications`: Listagem paginada de notificações
- `GET /api/notifications/count`: Contadores diversos (sexo, bairro, evolução, etc.)
- `GET /api/notifications/errors`: Notificações com erro de processamento

---

*Para mais informações sobre a estrutura do banco de dados e entidades, consulte as classes `Notification` e `NotificationWithError` no código fonte.*
