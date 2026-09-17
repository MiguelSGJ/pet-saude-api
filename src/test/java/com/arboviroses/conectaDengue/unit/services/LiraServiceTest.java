package com.arboviroses.conectaDengue.unit.services;

import com.arboviroses.conectaDengue.Domain.Entities.Lira.Lira;
import com.arboviroses.conectaDengue.Domain.Repositories.Lira.LiraRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Lira.LiraParametrosRepository;
import com.arboviroses.conectaDengue.Domain.Services.Lira.LiraService;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class LiraServiceTest {

    private static final Object[] LINHA_VALIDA =
            {null, "Centro", 300.0, 10.0, 3.3, 1.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3.0, 1.0};

    private LiraRepository mockRepository() {
        LiraRepository repository = mock(LiraRepository.class);
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return repository;
    }

    private LiraParametrosRepository mockParametrosRepository() {
        LiraParametrosRepository repository = mock(LiraParametrosRepository.class);
        when(repository.findByAnoAndLiraNumber(anyInt(), anyInt())).thenReturn(java.util.Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return repository;
    }

    private LiraService service(LiraRepository repository) {
        return new LiraService(repository, mockParametrosRepository());
    }

    private MockMultipartFile toMultipartFile(XSSFWorkbook workbook) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new MockMultipartFile("file", "lira.xlsx", null, out.toByteArray());
    }

    private void escreverLinha(Row row, Object... valores) {
        for (int i = 0; i < valores.length; i++) {
            Object v = valores[i];
            if (v == null) continue;
            if (v instanceof String s) {
                row.createCell(i).setCellValue(s);
            } else {
                row.createCell(i).setCellValue((Double) v);
            }
        }
    }

    @Test
    void celulaDeFormulaComValorCacheadoELidaCorretamente() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LIRA");
        sheet.createRow(0);
        sheet.createRow(1);
        Row dados = sheet.createRow(2);
        escreverLinha(dados, LINHA_VALIDA);
        dados.createCell(2).setCellFormula("100+250"); // TotalImoveisInsp calculado

        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluator.evaluateFormulaCell(dados.getCell(2));

        LiraRepository repository = mockRepository();
        LiraService service = service(repository);

        List<Lira> salvos = service.saveLiraData(toMultipartFile(workbook), 2022, 1, 1.0, 4.0);

        assertThat(salvos).hasSize(1);
        assertThat(salvos.get(0).getTotalImoveisInsp()).isEqualTo(350);
    }

    @Test
    void indiceBreteauVazioSalvaComNullSemDescartarALinha() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LIRA");
        sheet.createRow(0);
        sheet.createRow(1);
        Row dados = sheet.createRow(2);
        Object[] linha = LINHA_VALIDA.clone();
        linha[13] = null; // indiceBreteau sem amostragem: celula fica em branco
        escreverLinha(dados, linha);

        LiraRepository repository = mockRepository();
        LiraService service = service(repository);

        List<Lira> salvos = service.saveLiraData(toMultipartFile(workbook), 2022, 1, 1.0, 4.0);

        assertThat(salvos).hasSize(1);
        assertThat(salvos.get(0).getIndiceBreteau()).isNull();
    }

    @Test
    void linhaDeCabecalhoEDescartada() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LIRA");
        sheet.createRow(0);
        sheet.createRow(1);
        escreverLinha(sheet.createRow(2), (Object[]) new Object[]{null, "Bairros"});
        escreverLinha(sheet.createRow(3), (Object[]) new Object[]{null, "Número do ciclo"});
        escreverLinha(sheet.createRow(4), LINHA_VALIDA);

        LiraRepository repository = mockRepository();
        LiraService service = service(repository);

        List<Lira> salvos = service.saveLiraData(toMultipartFile(workbook), 2022, 1, 1.0, 4.0);

        assertThat(salvos).hasSize(1);
        assertThat(salvos.get(0).getBairro()).isEqualTo("Centro");
    }

    @Test
    void bairroComEspacoNoFimESalvoComTrim() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LIRA");
        sheet.createRow(0);
        sheet.createRow(1);
        Row dados = sheet.createRow(2);
        Object[] linha = LINHA_VALIDA.clone();
        linha[1] = "Itapetinga ";
        escreverLinha(dados, linha);

        LiraRepository repository = mockRepository();
        LiraService service = service(repository);

        List<Lira> salvos = service.saveLiraData(toMultipartFile(workbook), 2022, 1, 1.0, 4.0);

        assertThat(salvos).hasSize(1);
        assertThat(salvos.get(0).getBairro()).isEqualTo("Itapetinga");
    }

    @Test
    void trintaLinhasValidasGeramTrintaRegistrosSalvos() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LIRA");
        sheet.createRow(0);
        sheet.createRow(1);
        for (int i = 0; i < 30; i++) {
            Object[] linha = LINHA_VALIDA.clone();
            linha[1] = "Centro " + i;
            escreverLinha(sheet.createRow(2 + i), linha);
        }

        LiraRepository repository = mockRepository();
        LiraParametrosRepository parametrosRepository = mockParametrosRepository();
        LiraService service = new LiraService(repository, parametrosRepository);

        List<Lira> salvos = service.saveLiraData(toMultipartFile(workbook), 2022, 1, 1.5, 5.0);

        assertThat(salvos).hasSize(30);
        verify(repository).deleteByAnoAndLiraNumber(2022, 1);
        verify(parametrosRepository).save(argThat(parametros ->
                parametros.getAno().equals(2022)
                        && parametros.getLiraNumber().equals(1)
                        && parametros.getLimiteAlerta().equals(1.5)
                        && parametros.getLimiteRisco().equals(5.0)
        ));
    }

    @Test
    void disponibilidadeAgrupaCiclosPorAnoDoMaisRecenteParaOMaisAntigo() {
        LiraRepository repository = mockRepository();
        when(repository.findDisponibilidade()).thenReturn(List.of(
                new Object[]{2025, 6},
                new Object[]{2025, 4},
                new Object[]{2024, 5}
        ));

        LiraService service = service(repository);
        var disponibilidade = service.getDisponibilidade();

        assertThat(disponibilidade).hasSize(2);
        assertThat(disponibilidade.get(0).ano()).isEqualTo(2025);
        assertThat(disponibilidade.get(0).ciclos()).containsExactly(6, 4);
        assertThat(disponibilidade.get(1).ano()).isEqualTo(2024);
        assertThat(disponibilidade.get(1).ciclos()).containsExactly(5);
    }

    @Test
    void rejeitaLimitesDeClassificacaoInvertidos() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LIRA");
        sheet.createRow(0);
        sheet.createRow(1);
        escreverLinha(sheet.createRow(2), LINHA_VALIDA);

        LiraRepository repository = mockRepository();
        LiraService service = service(repository);

        assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                service.saveLiraData(toMultipartFile(workbook), 2025, 1, 4.0, 1.0)
        )).isInstanceOf(IllegalArgumentException.class);

        verify(repository, never()).deleteByAnoAndLiraNumber(anyInt(), anyInt());
    }

    @Test
    void parametrosAusentesUsamFaixasPadrao() {
        LiraService service = service(mockRepository());

        var parametros = service.getParametros(2025, 4);

        assertThat(parametros.limiteAlerta()).isEqualTo(1.0);
        assertThat(parametros.limiteRisco()).isEqualTo(4.0);
    }
}
