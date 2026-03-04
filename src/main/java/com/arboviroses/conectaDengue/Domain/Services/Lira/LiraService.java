package com.arboviroses.conectaDengue.Domain.Services.Lira;

import com.arboviroses.conectaDengue.Domain.Entities.Lira.Lira;
import com.arboviroses.conectaDengue.Domain.Repositories.Lira.LiraRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiraService {

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

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Lira lira = new Lira();

                try {
                    String bairro = getCellValueAsString(row.getCell(1));
                    if (bairro == null || bairro.trim().isEmpty() || bairro.toLowerCase().contains("estrato")) {
                        continue;
                    }
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
                    System.err.println("Erro ao processar linha do LIRA: " + row.getRowNum() + " - " + e.getMessage());
                }
            }
        }
        return liraRepository.saveAll(liras);
    }

    public List<Lira> getLiraByAno(Integer ano) {
        return liraRepository.findByAno(ano);
    }

    public List<Lira> getLiraByAnoAndNumber(Integer ano, Integer liraNumber) {
        return liraRepository.findByAnoAndLiraNumber(ano, liraNumber);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Integer getCellValueAsInt(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
