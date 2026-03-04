## Documentação das funcionalidades novas

Está centralizada no endpoint base `/determinantes`.

### 1. Upload de Dados
*   **Endpoint:** `POST /determinantes/upload`
*   **Parâmetros:**
    *   `file` (MultipartFile): O arquivo Excel (.xlsx).
    *   `ano` (Integer): O ano de referência dos dados (ex: 2026).
*   **Descrição:** Processa a planilha, limpa os dados antigos do mesmo ano e salva os novos registros.

### 2. Listagem Geral
*   **Endpoint:** `GET /determinantes`
*   **Descrição:** Retorna todos os registros salvos no banco de dados com todos os campos.

### 3. Consultas Segmentadas (Blocos)
Estes endpoints utilizam **DTOs otimizados** para retornar apenas os campos relevantes de cada categoria.

**Listagem Geral por Bloco:**
| Endpoint | Bloco | 
| :--- | :--- |
| `GET /determinantes/agua` | **Água** |
| `GET /determinantes/tratamento` | **Tratamento** |
| `GET /determinantes/escoamento` | **Saneamento** |
| `GET /determinantes/lixo` | **Lixo** |
| `GET /determinantes/renda` | **Renda** |
| `GET /determinantes/educacao` | **Educação** |

### 4. Consultas Avançadas (Filtros e Agregações)
Você pode filtrar dados de uma UBS específica ou ver o total acumulado de um bairro (agregação).

**Busca por UBS (Filtrada por nome):**
*   `GET /determinantes/{bloco}/ubs/{nome}`
*   *Exemplo:* `GET /determinantes/agua/ubs/Conquista`

**Agregação por Bairro (Soma de todas as UBS do bairro):**
*   `GET /determinantes/{bloco}/bairro/{nome}`
*   *Exemplo:* `GET /determinantes/renda/bairro/Abolicao`
*   *Descrição:* Este endpoint soma os valores de todas as UBS que pertencem ao bairro informado, retornando um consolidado por ano.

---
PET SUS - 2026