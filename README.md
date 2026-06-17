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

## Docker

O sistema completo pode ser executado com Docker Compose a partir deste repositório, subindo:

- frontend React servido por Nginx
- backend Spring Boot
- API Python FastAPI
- PostgreSQL
- DbGate para gerenciar o banco pelo navegador

### Pré-requisitos

- Docker
- Docker Compose
- As pastas irmãs `../ArbovirosesFront` e `../ArbovirosesPython` presentes no mesmo diretório pai deste projeto

### Como subir

1. Ajuste as variáveis de ambiente em `.env.docker`.
2. Execute:

```bash
docker compose --env-file .env.docker up -d --build --remove-orphans
```

3. A aplicação ficará disponível em `http://localhost:8080` por padrão.

Esse comando tambem pode ser usado para atualizar o ambiente: se os containers ja existirem, o Docker Compose recria o que mudou; se nao existirem, ele cria e sobe tudo.

### Gerenciador do banco de dados

O projeto sobe o DbGate junto com os containers. Ele fica disponivel por padrao apenas no proprio servidor:

```text
http://localhost:8082
```

No `docker-compose.yml`, o DbGate fica publicado em `127.0.0.1`, entao ele nao deve ficar acessivel diretamente pela internet em um deploy remoto.

Se o sistema estiver em um servidor remoto e voce quiser abrir o DbGate na sua maquina, use um tunel SSH:

```bash
ssh -L 8082:127.0.0.1:8082 usuario@ip-do-servidor
```

Depois acesse no seu navegador local:

```text
http://localhost:8082
```

Para acessar o PostgreSQL no DbGate, crie uma conexao usando:

```text
Server type: PostgreSQL
Server: db
Port: 5432
User: postgres
Password: postgres
Database: arboviroses_db
```

O campo `Server` deve ser `db` porque o DbGate roda dentro da mesma rede do Docker Compose. Use `localhost` apenas para ferramentas instaladas diretamente no Windows.

A porta local do DbGate pode ser alterada em `.env.docker`:

```env
DBGATE_PORT=8082
```

Tambem e possivel acessar o banco pelo terminal:

```bash
docker exec -it arboviroses-db psql -U postgres -d arboviroses_db
```

Se o DbGate mostrar o erro `password authentication failed for user "postgres"`, provavelmente o volume do PostgreSQL ja foi criado com uma senha antiga. Alterar `POSTGRES_PASSWORD` no `.env.docker` nao muda a senha interna automaticamente.

Para ajustar a senha real do usuario `postgres` para `postgres`, execute:

```bash
docker compose --env-file .env.docker exec -T db psql -U postgres -d arboviroses_db -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

Se preferir fazer manualmente, entre no `psql`:

```bash
docker exec -it arboviroses-db psql -U postgres -d arboviroses_db
```

E execute:

```sql
ALTER USER postgres WITH PASSWORD 'postgres';
```

### O que mudar para producao

Os links publicos ficaram centralizados em `.env.docker`:

- `PUBLIC_ORIGIN`: URL publica da aplicacao
- `PUBLIC_PORT`: porta publicada pelo container do frontend
- `DBGATE_PORT`: porta local do DbGate, publicada apenas em `127.0.0.1`
- `SECURITY_CORS_ALLOWED_ORIGINS`: origens permitidas pelo backend
- `REACT_APP_API_URL`: URL da API Java consumida pelo frontend
- `REACT_APP_PYTHON_API_URL`: URL da API Python consumida pelo frontend

Se o deploy final usar um unico dominio com proxy no frontend, o recomendado e manter:

```env
REACT_APP_API_URL=/api
REACT_APP_PYTHON_API_URL=/python-api
```

Exemplo de ajuste para producao em um dominio real:

```env
PUBLIC_ORIGIN=https://app.seudominio.com
PUBLIC_PORT=80
DBGATE_PORT=8082
SECURITY_CORS_ALLOWED_ORIGINS=https://app.seudominio.com
REACT_APP_API_URL=/api
REACT_APP_PYTHON_API_URL=/python-api
```

### Como parar

```bash
docker compose down
```

Para remover também o volume do PostgreSQL:

```bash
docker compose down -v
```

### Serviços expostos

- Frontend/Nginx: `${PUBLIC_ORIGIN}`
- Backend via proxy: `${PUBLIC_ORIGIN}/api`
- API Python via proxy: `${PUBLIC_ORIGIN}/python-api`
- DbGate: `http://localhost:${DBGATE_PORT}` apenas no proprio servidor ou via tunel SSH

### Observações

- O `docker-compose.yml` deste repositório usa `../ArbovirosesFront` e `../ArbovirosesPython` como contextos de build.
- Em produção, troque pelo menos `POSTGRES_PASSWORD` e `SECURITY_JWT_SECRET_KEY`.
- O frontend foi configurado para consumir as APIs via proxy do Nginx, evitando dependência de URLs externas hardcoded.
