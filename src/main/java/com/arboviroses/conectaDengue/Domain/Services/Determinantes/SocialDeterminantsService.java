package com.arboviroses.conectaDengue.Domain.Services.Determinantes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.arboviroses.conectaDengue.Api.DTO.AguaDTO;
import com.arboviroses.conectaDengue.Api.DTO.EducacaoDTO;
import com.arboviroses.conectaDengue.Api.DTO.EscoamentoDTO;
import com.arboviroses.conectaDengue.Api.DTO.LixoDTO;
import com.arboviroses.conectaDengue.Api.DTO.RendaDTO;
import com.arboviroses.conectaDengue.Api.DTO.TratamentoDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Determinantes;
import com.arboviroses.conectaDengue.Domain.Repositories.Determinantes.DeterminantesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialDeterminantsService {

    private final DeterminantesRepository repository;

    @Transactional
    public List<Determinantes> saveSocialDeterminants(MultipartFile file, Integer ano) throws IOException {
        
        if (ano != null) {
            repository.deleteByAno(ano);
        }

        List<Determinantes> results = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            for (int i = 0; i < 3 && rowIterator.hasNext(); i++) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String ubsName = getCellValueAsString(row.getCell(0));

                if (ubsName.toLowerCase().contains("total") || (ubsName.isEmpty() && getCellValueAsInt(row.getCell(2)) == 0)) {
                    continue;
                }

                Determinantes d = new Determinantes();

                d.setAno(ano);
                d.setUbs(ubsName.isEmpty() ? "NÃO INFORMADO" : ubsName);
                String bairroOriginal = getCellValueAsString(row.getCell(1));
                d.setBairro(bairroOriginal.isEmpty() ? "MOSSORÓ" : bairroOriginal.toUpperCase());

                // Água
                d.setAguaRede(getCellValueAsInt(row.getCell(2)));
                d.setAguaPoco(getCellValueAsInt(row.getCell(3)));
                d.setAguaCisterna(getCellValueAsInt(row.getCell(4)));
                d.setAguaCarroPipe(getCellValueAsInt(row.getCell(5)));
                d.setAguaOutro(getCellValueAsInt(row.getCell(6)));
                d.setAguaNaoInformado(getCellValueAsInt(row.getCell(7)));
                d.setAguaTotal(getCellValueAsInt(row.getCell(8)));

                // Tratamento
                d.setTratamentoFiltrada(getCellValueAsInt(row.getCell(9)));
                d.setTratamentoFervida(getCellValueAsInt(row.getCell(10)));
                d.setTratamentoClorada(getCellValueAsInt(row.getCell(11)));
                d.setTratamentoMineral(getCellValueAsInt(row.getCell(12)));
                d.setTratamentoSemTratamento(getCellValueAsInt(row.getCell(13)));
                d.setTratamentoNaoInformado(getCellValueAsInt(row.getCell(14)));
                d.setTratamentoTotal(getCellValueAsInt(row.getCell(15)));

                // Escoamento
                d.setEscoamentoRedeColetora(getCellValueAsInt(row.getCell(16)));
                d.setEscoamentoFossaSeptica(getCellValueAsInt(row.getCell(17)));
                d.setEscoamentoFossaRudimentar(getCellValueAsInt(row.getCell(18)));
                d.setEscoamentoRioMar(getCellValueAsInt(row.getCell(19)));
                d.setEscoamentoCeuAberto(getCellValueAsInt(row.getCell(20)));
                d.setEscoamentoOutra(getCellValueAsInt(row.getCell(21)));
                d.setEscoamentoNaoInformado(getCellValueAsInt(row.getCell(22)));
                d.setEscoamentoTotal(getCellValueAsInt(row.getCell(23)));

                // Lixo
                d.setLixoColetado(getCellValueAsInt(row.getCell(24)));
                d.setLixoQueimadoEnterrado(getCellValueAsInt(row.getCell(25)));
                d.setLixoCeuAberto(getCellValueAsInt(row.getCell(26)));
                d.setLixoOutro(getCellValueAsInt(row.getCell(27)));
                d.setLixoNaoInformado(getCellValueAsInt(row.getCell(28)));
                d.setLixoTotal(getCellValueAsInt(row.getCell(29)));

                // Renda
                d.setRendaUmQuartoSalario(getCellValueAsInt(row.getCell(30)));
                d.setRendaMeioSalario(getCellValueAsInt(row.getCell(31)));
                d.setRendaUmSalario(getCellValueAsInt(row.getCell(32)));
                d.setRendaDoisSalarios(getCellValueAsInt(row.getCell(33)));
                d.setRendaTresSalarios(getCellValueAsInt(row.getCell(34)));
                d.setRendaQuatroSalarios(getCellValueAsInt(row.getCell(35)));
                d.setRendaAusencia(getCellValueAsInt(row.getCell(36)));
                d.setRendaAcimaQuatro(getCellValueAsInt(row.getCell(37)));
                d.setRendaNaoInformado(getCellValueAsInt(row.getCell(38)));
                d.setRendaTotal(getCellValueAsInt(row.getCell(39)));

                // Educação
                d.setEduCreche(getCellValueAsInt(row.getCell(40)));
                d.setEduPreEscola(getCellValueAsInt(row.getCell(41)));
                d.setEduAlfabetizacao(getCellValueAsInt(row.getCell(42)));
                d.setEdu1a4(getCellValueAsInt(row.getCell(43)));
                d.setEdu5a8(getCellValueAsInt(row.getCell(44)));
                d.setEduFundamentalCompleto(getCellValueAsInt(row.getCell(45)));
                d.setEduFundamentalEspecial(getCellValueAsInt(row.getCell(46)));
                d.setEduEja1a4(getCellValueAsInt(row.getCell(48)));
                d.setEduEja5a8(getCellValueAsInt(row.getCell(49)));
                d.setEduMedio(getCellValueAsInt(row.getCell(50)));
                d.setEduMedioEspecial(getCellValueAsInt(row.getCell(51)));
                d.setEduMedioEja(getCellValueAsInt(row.getCell(52)));
                d.setEduSuperior(getCellValueAsInt(row.getCell(53)));
                d.setEduMobral(getCellValueAsInt(row.getCell(54)));
                d.setEduNenhum(getCellValueAsInt(row.getCell(55)));
                d.setEduNaoInformado(getCellValueAsInt(row.getCell(56)));
                d.setEduTotal(getCellValueAsInt(row.getCell(57)));

                results.add(d);
            }
        }
        
        return repository.saveAll(results);
    }

    public List<Determinantes> listAll() {
        return repository.findAllByOrderByAnoDescUbsAsc();
    }

    public List<Determinantes> listAllByUbs(String ubs) {
        return repository.findByUbsContainingIgnoreCaseOrderByAnoDesc(ubs);
    }

    public List<Determinantes> listAllByBairro(String bairro) {
        return repository.findByBairroContainingIgnoreCaseOrderByAnoDescUbsAsc(bairro);
    }

    public List<AguaDTO> listAgua() {
        return repository.findAllAgua();
    }

    public List<TratamentoDTO> listTratamento() {
        return repository.findAllTratamento();
    }

    public List<EscoamentoDTO> listEscoamento() {
        return repository.findAllEscoamento();
    }

    public List<LixoDTO> listLixo() {
        return repository.findAllLixo();
    }

    public List<RendaDTO> listRenda() {
        return repository.findAllRenda();
    }

    public List<EducacaoDTO> listEducacao() {
        return repository.findAllEducacao();
    }

    // Filtros por UBS
    public List<AguaDTO> listAguaByUbs(String ubs) {
        return repository.findAguaByUbs(ubs);
    }

    public List<TratamentoDTO> listTratamentoByUbs(String ubs) {
        return repository.findTratamentoByUbs(ubs);
    }

    public List<EscoamentoDTO> listEscoamentoByUbs(String ubs) {
        return repository.findEscoamentoByUbs(ubs);
    }

    public List<LixoDTO> listLixoByUbs(String ubs) {
        return repository.findLixoByUbs(ubs);
    }

    public List<RendaDTO> listRendaByUbs(String ubs) {
        return repository.findRendaByUbs(ubs);
    }

    public List<EducacaoDTO> listEducacaoByUbs(String ubs) {
        return repository.findEducacaoByUbs(ubs);
    }

    // Agregações por Bairro
    public List<AguaDTO> aggregateAguaByBairro(String bairro) {
        return repository.aggregateAguaByBairro(bairro);
    }

    public List<TratamentoDTO> aggregateTratamentoByBairro(String bairro) {
        return repository.aggregateTratamentoByBairro(bairro);
    }

    public List<EscoamentoDTO> aggregateEscoamentoByBairro(String bairro) {
        return repository.aggregateEscoamentoByBairro(bairro);
    }

    public List<LixoDTO> aggregateLixoByBairro(String bairro) {
        return repository.aggregateLixoByBairro(bairro);
    }

    public List<RendaDTO> aggregateRendaByBairro(String bairro) {
        return repository.aggregateRendaByBairro(bairro);
    }

    public List<EducacaoDTO> aggregateEducacaoByBairro(String bairro) {
        return repository.aggregateEducacaoByBairro(bairro);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    return String.valueOf((int) cell.getNumericCellValue());
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private Integer getCellValueAsInt(Cell cell) {
        if (cell == null)
            return 0;
        try {
            if (cell.getCellType() == CellType.NUMERIC)
                return (int) cell.getNumericCellValue();

            if (cell.getCellType() == CellType.FORMULA)
                return (int) cell.getNumericCellValue();

            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim().replaceAll("[^0-9]", "");
                return s.isEmpty() ? 0 : Integer.parseInt(s);
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}
