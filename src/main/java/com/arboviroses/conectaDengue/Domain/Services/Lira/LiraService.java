package com.arboviroses.conectaDengue.Domain.Services.Lira;

import com.arboviroses.conectaDengue.Api.DTO.response.LiraDisponibilidadeResponse;
import com.arboviroses.conectaDengue.Domain.Entities.Lira.Lira;
import com.arboviroses.conectaDengue.Domain.Repositories.Lira.LiraRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiraService {

    private static final Logger log = LoggerFactory.getLogger(LiraService.class);

    private final LiraRepository liraRepository;

    public List<Lira> saveLiraData(MultipartFile file, Integer ano, Integer liraNumber) throws IOException {
        // Primeiro, remove todos os dados existentes para o ano e número do LIRA especificados
        liraRepository.deleteByAnoAndLiraNumber(ano, liraNumber);
        
        List<Lira> liras = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next();
            if (rowIterator.hasNext()) rowIterator.next();

            int descartadas = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Lira lira = new Lira();

                try {
                    String bairro = getCellValueAsString(row.getCell(1));
                    if (bairro == null) continue;
                    bairro = bairro.trim();
                    if (bairro.isEmpty() || pareceCabecalho(bairro)) continue;
                    lira.setBairro(bairro);

                    lira.setTotalImoveisInsp(getCellValueAsInt(row.getCell(2)));
                    lira.setTotalImoveisPos(getCellValueAsInt(row.getCell(3)));
                    lira.setIndiceInfestacaoPredial(getCellValueAsDouble(row.getCell(4)));
                    lira.setDepositoA1(getCellValueAsInt(row.getCell(5)));
                    lira.setDepositoA2(getCellValueAsInt(row.getCell(6)));
                    lira.setDepositoB(getCellValueAsInt(row.getCell(7)));
                    lira.setDepositoC(getCellValueAsInt(row.getCell(8)));
                    lira.setDepositoD1(getCellValueAsInt(row.getCell(9)));
                    lira.setDepositoD2(getCellValueAsInt(row.getCell(10)));
                    lira.setDepositoE(getCellValueAsInt(row.getCell(11)));
                    lira.setTotalDepositosPos(getCellValueAsInt(row.getCell(12)));
                    lira.setIndiceBreteau(getCellValueAsDouble(row.getCell(13)));
                    lira.setAno(ano);
                    lira.setLiraNumber(liraNumber);

                    liras.add(lira);
                } catch (Exception e) {
                    descartadas++;
                    log.warn("LIRA {}/ciclo {}: linha {} descartada - {}", ano, liraNumber, row.getRowNum() + 1, e.toString());
                }
            }
            log.info("LIRA {}/ciclo {}: {} salvas, {} descartadas", ano, liraNumber, liras.size(), descartadas);
        }
        return liraRepository.saveAll(liras);
    }

    private boolean pareceCabecalho(String valor) {
        String v = valor.toLowerCase(Locale.ROOT);
        return v.contains("estrato") || v.contains("bairro") || v.contains("ciclo")
            || v.contains("total")   || v.equals("data")     || v.startsWith("zona ");
    }

    public List<Lira> getLiraByAno(Integer ano) {
        return liraRepository.findByAno(ano);
    }

    public List<Lira> getLiraByAnoAndNumber(Integer ano, Integer liraNumber) {
        return liraRepository.findByAnoAndLiraNumber(ano, liraNumber);
    }

    public List<LiraDisponibilidadeResponse> getDisponibilidade() {
        Map<Integer, List<Integer>> ciclosPorAno = new LinkedHashMap<>();

        for (Object[] item : liraRepository.findDisponibilidade()) {
            Integer ano = ((Number) item[0]).intValue();
            Integer ciclo = ((Number) item[1]).intValue();
            ciclosPorAno.computeIfAbsent(ano, ignored -> new ArrayList<>()).add(ciclo);
        }

        return ciclosPorAno.entrySet().stream()
                .map(entry -> new LiraDisponibilidadeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private CellType tipoEfetivo(Cell cell) {
        return cell.getCellType() == CellType.FORMULA
                ? cell.getCachedFormulaResultType()
                : cell.getCellType();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        CellType tipo = tipoEfetivo(cell);
        if (tipo == CellType.STRING) return cell.getStringCellValue();
        if (tipo == CellType.NUMERIC) return String.valueOf(cell.getNumericCellValue());
        return null;
    }

    private Integer getCellValueAsInt(Cell cell) {
        Double valor = getCellValueAsDouble(cell);
        return valor == null ? null : (int) Math.round(valor);
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return null;
        CellType tipo = tipoEfetivo(cell);
        if (tipo == CellType.NUMERIC) return cell.getNumericCellValue();
        if (tipo == CellType.STRING) {
            String bruto = cell.getStringCellValue().trim().replace(",", ".");
            if (bruto.isEmpty()) return null;
            try {
                return Double.parseDouble(bruto);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;   // BLANK, BOOLEAN, ERROR (#DIV/0!, #VALUE!)
    }
}
