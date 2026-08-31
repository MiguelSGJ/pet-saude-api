#!/usr/bin/env python3
"""
Separa a planilha consolidada do LIRA de Mossoro em um arquivo .xlsx por (ano, ciclo),
no layout exato que o importador do backend (LiraService) espera.

Uso:
    pip install openpyxl
    python scripts/split_lira.py --anos 2022 2023            # os anos do Andre
    python scripts/split_lira.py --anos 2024 2025            # os anos do Miguel
    python scripts/split_lira.py --anos 2022 --so-relatorio  # nao grava, so audita

Layout de saida (obrigatorio - nao altere):
    linha 1  titulo               (ignorada pelo importador)
    linha 2  cabecalho legivel    (ignorada pelo importador)
    linha 3+ dados
    col A=Data(vazia) B=Bairro C=TotalInsp D=TotalPos E=IIP
    col F=A1 G=A2 H=B I=C J=D1 K=D2 L=E M=TotalDepositos+ N=IndiceBreteau
"""
import argparse
import re
import sys
import unicodedata
from pathlib import Path

try:
    from openpyxl import Workbook, load_workbook
except ImportError:
    sys.exit("Falta a dependencia: pip install openpyxl")

RAIZ = Path(__file__).resolve().parent.parent
ENTRADA_PADRAO = RAIZ / "dados" / "DADOS DO LIRA MOSSORO.xlsx"
SAIDA_PADRAO = RAIZ / "dados" / "lira"

# Cada ano ocupa 15 colunas na horizontal. Valores 0-indexados.
BASE_POR_ANO = {2021: 0, 2022: 15, 2023: 30, 2024: 45, 2025: 60}
# Linha do cabecalho "Numero do ciclo" de cada bloco vertical (1-indexado, como no Excel).
LINHAS_CABECALHO_BLOCO = [2, 36, 70, 104]
QTD_BAIRROS = 30

# Offsets dentro do bloco do ano -> indice de coluna que o LiraService le.
CAMPOS = [
    ("Total Imoveis Inspecionados", 2, "int"),
    ("Total Imoveis Positivos", 3, "int"),
    ("Indice Infestacao Predial", 4, "float"),
    ("Deposito A1", 5, "int"),
    ("Deposito A2", 6, "int"),
    ("Deposito B", 7, "int"),
    ("Deposito C", 8, "int"),
    ("Deposito D1", 9, "int"),
    ("Deposito D2", 10, "int"),
    ("Deposito E", 11, "int"),
    ("Total Depositos Positivos", 12, "int"),
    ("Indice Breteau", 13, "float"),
]

# Grafia canonica: chave = nome normalizado da planilha, valor = grafia do catalogo
# NeighborhoodsMossoro.java do backend. Mantem LIRA e notificacoes cruzaveis por bairro.
CANONICO = {
    "ABOLICAO": "Abolição",
    "AEROPORTO": "Aeroporto",
    "ALAGADOS": "Alagados",
    "ALTO DA CONCEICAO": "Alto da Conceição",
    "ALTO DE SAO MANOEL": "Alto de São Manoel",
    "ALTO DE SUMARE": "Alto do Sumaré",
    "BARROCAS": "Barrocas",
    "BELA VISTA": "Bela Vista",
    "BELO HORIZONTE": "Belo Horizonte",
    "BOA VISTA": "Boa Vista",
    "BOM JARDIM": "Bom Jardim",
    "BOM JESUS": "Bom Jesus",
    "CENTRO": "Centro",
    "DIXSEPTROSADO": "Dix-Sept Rosado",
    "DOM JAIME CAMARA": "Dom Jaime Câmara",
    "DOZE ANOS": "Doze Anos",
    "ILHA DE SANTA LUZIA": "Ilha de Santa Luzia",
    "ITAPETINGA": "Itapetinga",
    "LAGOA DO MATO": "Lagoa do Mato",
    "MONSENHOR AMERICO SIMONETTI": "Monsenhor Américo",
    "NOVA BETANIA": "Nova Betânia",
    "PAREDOES": "Paredões",
    "PINTOS": "Pintos",
    "PLANALTO 13 DE MAIO": "Planalto Treze de Maio",
    "PRESIDENTE COSTA E SILVA": "Presidente Costa e Silva",
    "REDENCAO": "Redenção",
    "RINCAO": "Rincão",
    "SANTA DELMIRA": "Santa Delmira",
    "SANTA JULIA": "Santa Júlia",
    "SANTO ANTONIO": "Santo Antônio",
}

# ---------------------------------------------------------------------------
# CORRECOES MANUAIS
# ---------------------------------------------------------------------------
# Os .xlsx gerados NAO sao versionados: eles sao insumo descartavel de importacao,
# os dados de verdade ficam no PostgreSQL. Logo, corrigir um numero editando o .xlsx
# a mao se perde na proxima regeracao e ninguem consegue auditar de onde veio o valor.
#
# Toda correcao entra AQUI. Assim `python scripts/split_lira.py` sempre reproduz
# exatamente os mesmos arquivos, e o diff do git mostra quem mudou o que e por que.
#
# Formato:  (ano, ciclo, "Bairro canonico", "Nome Do Campo"): (valor, "justificativa")
# valor None = celula vazia (NULL no banco = "nao amostrado").
# O nome do campo tem que bater com a 1a coluna da lista CAMPOS abaixo.
CORRECOES: dict[tuple, tuple] = {
    # Exemplo do formato (descomente e adapte):
    # (2025, 1, "Alto de São Manoel", "Total Imoveis Inspecionados"):
    #     (353, "planilha trazia 35306; confirmado com a Secretaria em dd/mm/aaaa"),

    # --- André, revisão dos 18 avisos de 2022/2023 (RELATORIO_2022_2023.txt) ---
    # Regra usada em cada caso: comparo o total de depositos positivos (M) com a soma
    # das colunas de deposito A1..E, e com o Breteau recalculado (100*M/TotalInsp).
    # Duas dessas tres fontes sempre concordam - o valor que sobra sozinho e o corrigido.

    (2022, 1, "Redenção", "Total Depositos Positivos"):
        (3, "A1+A2 = 1+2 = 3, e Breteau=3.5 bate com 100*3/85=3.53. O total gravado (13) e o errado."),
    (2022, 1, "Monsenhor Américo", "Total Depositos Positivos"):
        (4, "planilha trazia 0 mas A2=4; Breteau de 1.7 confirma que o total certo e 4 (100*4/234=1.71)."),
    (2022, 1, "Alto da Conceição", "Total Depositos Positivos"):
        (1, "A2=1 e Breteau=0.6 bate com 100*1/172=0.58. O total gravado (0) e o errado."),
    (2022, 2, "Dom Jaime Câmara", "Indice Breteau"):
        (8.1, "A2=20 e o total gravado (20) concordam entre si; 100*20/247=8.10, entao o Breteau digitado (2.1) e o errado."),
    (2022, 3, "Belo Horizonte", "Indice Breteau"):
        (4.0, "A2=6 e o total gravado (6) concordam; 100*6/151=3.97, entao o Breteau digitado (5.0) e o errado."),
    (2022, 4, "Belo Horizonte", "Indice Infestacao Predial"):
        (2.8, "TotalPos=4, TotalInsp=144 -> 100*4/144=2.78. O IIP digitado (1.7) nao bate com nada."),
    (2022, 4, "Belo Horizonte", "Indice Breteau"):
        (2.8, "A2=4 e o total gravado (4) concordam; 100*4/144=2.78, entao o Breteau digitado (1.7) e o errado (mesmo valor incorreto do IIP, provavel copia errada)."),
    (2023, 6, "Santo Antônio", "Total Depositos Positivos"):
        (19, "A2=19 mas o total gravado era 8, e nenhum dos dois batia com o Breteau (3.1) digitado. Assumido A2 como fonte mais confiavel (dado granular) sobre o total agregado."),
    (2023, 6, "Santo Antônio", "Indice Breteau"):
        (4.3, "Com o total corrigido para 19: 100*19/438=4.34. O Breteau digitado (3.1) nao batia com nenhuma hipotese de total."),
    (2023, 5, "Abolição", "Indice Breteau"):
        (0.9, "A2=4 e o total gravado (4) concordam; 100*4/440=0.91, entao o Breteau digitado (1.5) e o errado."),
    (2023, 2, "Nova Betânia", "Indice Breteau"):
        (0.7, "A2=1 e o total gravado (1) concordam; 100*1/135=0.74, entao o Breteau digitado (0.0) e o errado."),
    (2023, 2, "Aeroporto", "Indice Breteau"):
        (4.4, "A1+A2+D2=19+1=20, igual ao total gravado (20); 100*20/453=4.42, entao o Breteau digitado (0.7) e o errado."),
    (2023, 2, "Pintos", "Indice Breteau"):
        (3.9, "A2=2 e o total gravado (2) concordam; 100*2/51=3.92, entao o Breteau digitado (2.0) e o errado."),
    (2023, 2, "Presidente Costa e Silva", "Total Depositos Positivos"):
        (6, "A2+E = 3+3 = 6; com total=6, 100*6/216=2.78 bate com o Breteau gravado (3.0, diferenca de 0.22). O total gravado (3) e o errado."),
    (2023, 4, "Alto da Conceição", "Total Depositos Positivos"):
        (3, "A2=3 e Breteau=2.1 bate com 100*3/143=2.10. O total gravado (2) e o errado."),
    (2023, 4, "Alto do Sumaré", "Indice Breteau"):
        (2.8, "A2=13 e o total gravado (13) concordam; 100*13/458=2.84, entao o Breteau digitado (3.6) e o errado."),

    # Nao corrigidos - decisao consciente: sem amostragem, fica vazio (NULL), nunca 0.
    # (2022, 2, "Alagados") | (2022, 3, "Alagados") | (2023, 6, "Alagados"): linha inteira vazia.
}

# Textos usados na planilha para dizer "nao houve amostragem". Viram celula vazia (NULL).
SENTINELAS_VAZIO = {"-", "--", "---", "s/a", "s/amost.", "s/amost", "sem amostra", "na", "n/a", ""}


def normalizar(nome: str) -> str:
    s = unicodedata.normalize("NFD", nome)
    s = "".join(c for c in s if unicodedata.category(c) != "Mn")
    return re.sub(r"[^A-Z0-9 ]", "", s.upper()).strip()


def bairro_canonico(bruto: str, avisos: list) -> str:
    limpo = re.sub(r"\s+", " ", str(bruto)).strip()
    chave = normalizar(limpo)
    if chave in CANONICO:
        return CANONICO[chave]
    avisos.append(f"bairro fora do catalogo, mantido como veio: {limpo!r}")
    return limpo


def numero_do_ciclo(valor) -> int | None:
    """'1o.' -> 1, '6°' -> 6, '2°.' -> 2"""
    if valor is None:
        return None
    m = re.search(r"\d+", str(valor))
    return int(m.group()) if m else None


def converter(valor, tipo, ctx, avisos):
    """Devolve numero, ou None quando a celula representa ausencia de dado."""
    if valor is None:
        return None
    if isinstance(valor, (int, float)):
        return int(round(valor)) if tipo == "int" else float(valor)
    texto = str(valor).strip()
    if texto.lower() in SENTINELAS_VAZIO:
        return None
    if texto.startswith("#"):  # #DIV/0!, #VALUE!
        avisos.append(f"{ctx}: erro de formula {texto!r} -> gravado vazio")
        return None
    limpo = texto.replace(",", ".")
    try:
        n = float(limpo)
    except ValueError:
        m = re.match(r"\s*(\d+(?:[.,]\d+)?)", limpo)
        if not m:
            avisos.append(f"{ctx}: texto nao numerico {texto!r} -> gravado vazio")
            return None
        n = float(m.group(1).replace(",", "."))
        avisos.append(f"{ctx}: valor composto {texto!r} -> usado {n:g}, CONFIRA NA MAO")
    return int(round(n)) if tipo == "int" else n


def extrair(aba, ano, linha_cabecalho):
    """Le um bloco (ano, ciclo) e devolve (ciclo, linhas, avisos)."""
    base = BASE_POR_ANO[ano]
    avisos = []
    ciclo = numero_do_ciclo(aba.cell(row=linha_cabecalho, column=base + 3).value)
    primeira = linha_cabecalho + 3
    linhas = []
    for i in range(QTD_BAIRROS):
        r = primeira + i
        bruto = aba.cell(row=r, column=base + 2).value  # coluna "Bairros"
        if bruto is None or not str(bruto).strip():
            continue
        nome = bairro_canonico(bruto, avisos)
        valores = []
        for rotulo, offset, tipo in CAMPOS:
            ctx = f"{ano} ciclo {ciclo} | {nome} | {rotulo}"
            chave = (ano, ciclo, nome, rotulo)
            if chave in CORRECOES:
                valor, motivo = CORRECOES[chave]
                avisos.append(f"{ctx}: CORRIGIDO para {valor!r} - {motivo}")
                valores.append(valor)
                continue
            valores.append(converter(aba.cell(row=r, column=base + 1 + offset).value, tipo, ctx, avisos))
        linhas.append((nome, valores))
    return ciclo, linhas, avisos


def auditar(ano, ciclo, linhas, avisos):
    """Regras de sanidade. Nao corrige nada: so aponta para revisao humana."""
    for nome, v in linhas:
        insp, pos, iip, *_ = v
        total_dep, breteau = v[10], v[11]
        if insp is not None and insp > 2000:
            avisos.append(f"{ano} ciclo {ciclo} | {nome}: {insp} imoveis inspecionados e absurdo (media ~350). PROVAVEL ERRO DE DIGITACAO")
        if insp is not None and pos is not None and pos > insp:
            avisos.append(f"{ano} ciclo {ciclo} | {nome}: positivos ({pos}) > inspecionados ({insp})")
        if insp and pos is not None and iip is not None:
            esperado = 100 * pos / insp
            if abs(esperado - iip) > 0.5:
                avisos.append(f"{ano} ciclo {ciclo} | {nome}: IIP={iip} mas 100*{pos}/{insp}={esperado:.2f}")
        if insp and total_dep is not None and breteau is not None:
            esperado = 100 * total_dep / insp
            if abs(esperado - breteau) > 0.5:
                avisos.append(f"{ano} ciclo {ciclo} | {nome}: Breteau={breteau} mas 100*{total_dep}/{insp}={esperado:.2f}")
        if all(x is None for x in v):
            avisos.append(f"{ano} ciclo {ciclo} | {nome}: linha sem nenhum valor")
    return avisos


def gravar(destino: Path, ano, ciclo, linhas):
    wb = Workbook()
    aba = wb.active
    aba.title = f"LIRA {ano} C{ciclo}"
    aba.append([f"LIRA Mossoro - {ano} - Ciclo {ciclo}"])                       # linha 1
    aba.append(["Data", "Bairros"] + [rotulo for rotulo, _, _ in CAMPOS])       # linha 2
    for nome, valores in linhas:                                                # linha 3+
        aba.append([None, nome] + valores)
    aba.column_dimensions["B"].width = 30
    wb.save(destino)


def main():
    p = argparse.ArgumentParser(description="Separa a planilha do LIRA em um arquivo por ano e ciclo.")
    p.add_argument("--anos", nargs="+", type=int, required=True, choices=sorted(BASE_POR_ANO))
    p.add_argument("--entrada", type=Path, default=ENTRADA_PADRAO)
    p.add_argument("--saida", type=Path, default=SAIDA_PADRAO)
    p.add_argument("--so-relatorio", action="store_true", help="audita sem gravar os .xlsx")
    args = p.parse_args()

    if not args.entrada.exists():
        sys.exit(f"Nao achei a planilha: {args.entrada}")

    rotulos = {rotulo for rotulo, _, _ in CAMPOS}
    for (ano, ciclo, bairro, campo) in CORRECOES:
        if campo not in rotulos:
            sys.exit(f"CORRECOES: campo desconhecido {campo!r} em ({ano}, {ciclo}, {bairro!r}). "
                     f"Use um destes: {sorted(rotulos)}")
        if bairro not in CANONICO.values():
            sys.exit(f"CORRECOES: bairro fora da grafia canonica: {bairro!r}")

    # data_only=True devolve o VALOR calculado das formulas, nao a formula.
    wb = load_workbook(args.entrada, data_only=True)
    aba = wb[wb.sheetnames[0]]
    args.saida.mkdir(parents=True, exist_ok=True)

    todos_avisos = []
    gerados = 0
    for ano in sorted(args.anos):
        for linha_cabecalho in LINHAS_CABECALHO_BLOCO:
            ciclo, linhas, avisos = extrair(aba, ano, linha_cabecalho)
            if ciclo is None:
                todos_avisos.append(f"{ano} linha {linha_cabecalho}: nao consegui ler o numero do ciclo")
                continue
            com_dado = [l for l in linhas if any(v is not None for v in l[1])]
            if not com_dado:
                print(f"  {ano} ciclo {ciclo}: bloco vazio, nada gerado")
                continue
            avisos = auditar(ano, ciclo, linhas, avisos)
            todos_avisos += avisos
            destino = args.saida / f"LIRA_{ano}_C{ciclo}.xlsx"
            if not args.so_relatorio:
                gravar(destino, ano, ciclo, linhas)
                gerados += 1
            print(f"  {destino.name}: {len(linhas)} bairros, {len(com_dado)} com dado, {len(avisos)} avisos")

    rel = args.saida / f"RELATORIO_{'_'.join(str(a) for a in sorted(args.anos))}.txt"
    rel.write_text("\n".join(todos_avisos) + "\n" if todos_avisos else "sem avisos\n", encoding="utf-8")
    print(f"\n{gerados} arquivos gerados em {args.saida}")
    print(f"{len(todos_avisos)} avisos -> {rel}")
    print("Revise TODOS os avisos antes de importar.")


if __name__ == "__main__":
    main()
