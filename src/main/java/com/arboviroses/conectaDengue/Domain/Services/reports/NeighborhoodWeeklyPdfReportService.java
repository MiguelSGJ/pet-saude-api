package com.arboviroses.conectaDengue.Domain.Services.reports;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.arboviroses.conectaDengue.Api.DTO.request.NeighborhoodWeeklyPdfReportRequest;
import com.arboviroses.conectaDengue.Api.Exceptions.InvalidAgravoException;
import com.arboviroses.conectaDengue.Api.Exceptions.InvalidNeighborhoodWeeklyReportException;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepositoryCustom.NeighborhoodWeeklyCountRow;
import com.arboviroses.conectaDengue.Domain.Services.Bairros.BairroService;
import com.arboviroses.conectaDengue.Utils.ConvertNameToIdAgravo;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NeighborhoodWeeklyPdfReportService {
    private final BairroService bairroService;
    private final NotificationRepository notificationRepository;

    public byte[] generateReport(NeighborhoodWeeklyPdfReportRequest reportRequest) throws InvalidAgravoException {
        if (reportRequest.getSemanaFinal() == null || reportRequest.getSemanaFinal() < 1) {
            throw new InvalidNeighborhoodWeeklyReportException("Informe uma semana epidemiológica final maior ou igual a 1.");
        }

        int semanaInicial = reportRequest.getSemanaInicial() != null ? reportRequest.getSemanaInicial() : 1;

        if (semanaInicial > reportRequest.getSemanaFinal()) {
            throw new InvalidNeighborhoodWeeklyReportException("A semana inicial deve ser menor ou igual à semana final.");
        }

        ReportFilters filters = extractReportFilters(reportRequest);
        int maxAvailableWeek = notificationRepository.buscarMaiorSemanaEpidemiologica(
            filters.agravoId(),
            filters.year(),
            filters.bairro()
        );

        if (maxAvailableWeek == 0) {
            throw new InvalidNeighborhoodWeeklyReportException("Nenhuma semana epidemiológica foi encontrada para os filtros informados.");
        }

        if (reportRequest.getSemanaFinal() > maxAvailableWeek) {
            throw new InvalidNeighborhoodWeeklyReportException(
                "A semana epidemiológica solicitada deve ser menor ou igual à maior semana disponível no banco para os filtros informados. Semana disponível até: "
                    + maxAvailableWeek + "."
            );
        }

        List<NeighborhoodWeeklyRow> reportRows = buildNeighborhoodWeeklyRows(filters, semanaInicial, reportRequest.getSemanaFinal());
        return createNeighborhoodWeeklyPdf(reportRows, semanaInicial, reportRequest.getSemanaFinal(), filters);
    }

    private ReportFilters extractReportFilters(NeighborhoodWeeklyPdfReportRequest reportRequest) throws InvalidAgravoException {
        String agravoId = null;

        if (reportRequest.getAgravo() != null && !reportRequest.getAgravo().isBlank()) {
            agravoId = ConvertNameToIdAgravo.convert(reportRequest.getAgravo());
        }

        return new ReportFilters(agravoId, reportRequest.getYear(), reportRequest.getBairro());
    }

    private List<NeighborhoodWeeklyRow> buildNeighborhoodWeeklyRows(ReportFilters filters, int semanaInicial, int semanaFinal) {
        List<NeighborhoodWeeklyCountRow> rawRows = notificationRepository.buscarContagemSemanalPorBairro(
            filters.agravoId(),
            filters.year(),
            filters.bairro(),
            semanaInicial,
            semanaFinal
        );
        Map<String, NeighborhoodWeeklyRow> rowsByNeighborhood = initializeRows(filters, semanaInicial, semanaFinal);

        for (NeighborhoodWeeklyCountRow tuple : rawRows) {
            NeighborhoodWeeklyRow row = rowsByNeighborhood.computeIfAbsent(
                tuple.bairro(),
                ignored -> NeighborhoodWeeklyRow.empty(tuple.bairro(), semanaInicial, semanaFinal)
            );

            row.weekCounts().put(tuple.semana(), tuple.total());
            row.setTotal(row.total() + tuple.total());
        }

        List<NeighborhoodWeeklyRow> rows = new ArrayList<>(rowsByNeighborhood.values());
        rows.sort((left, right) -> left.bairro().compareToIgnoreCase(right.bairro()));
        return rows;
    }

    private Map<String, NeighborhoodWeeklyRow> initializeRows(ReportFilters filters, int semanaInicial, int semanaFinal) {
        List<String> neighborhoods = filters.bairro() != null && !filters.bairro().isBlank()
            ? List.of(filters.bairro())
            : bairroService.listNeighborhoodNames();

        Map<String, NeighborhoodWeeklyRow> rows = new LinkedHashMap<>();
        for (String neighborhood : neighborhoods) {
            if (neighborhood == null || neighborhood.isBlank()) {
                continue;
            }
            rows.put(neighborhood, NeighborhoodWeeklyRow.empty(neighborhood, semanaInicial, semanaFinal));
        }
        return rows;
    }

    private byte[] createNeighborhoodWeeklyPdf(List<NeighborhoodWeeklyRow> rows, int semanaInicial, int semanaFinal, ReportFilters filters) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int numSemanas = semanaFinal - semanaInicial + 1;
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            Paragraph title = new Paragraph("Relatório por bairro e semana epidemiológica", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph(buildReportSubtitle(filters, semanaInicial, semanaFinal), subtitleFont);
            subtitle.setSpacingAfter(12f);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(numSemanas + 2);
            table.setWidthPercentage(100f);

            float[] widths = new float[numSemanas + 2];
            widths[0] = 3.8f;
            for (int index = 1; index <= numSemanas; index++) {
                widths[index] = 1.1f;
            }
            widths[numSemanas + 1] = 1.3f;
            table.setWidths(widths);

            addHeaderCell(table, "Bairro", headerFont);
            for (int semana = semanaInicial; semana <= semanaFinal; semana++) {
                addHeaderCell(table, "SE " + semana, headerFont);
            }
            addHeaderCell(table, "Total", headerFont);

            for (NeighborhoodWeeklyRow row : rows) {
                addBodyCell(table, row.bairro(), bodyFont, Element.ALIGN_LEFT);
                for (int semana = semanaInicial; semana <= semanaFinal; semana++) {
                    addBodyCell(table, String.valueOf(row.weekCounts().getOrDefault(semana, 0L)), bodyFont, Element.ALIGN_CENTER);
                }
                addBodyCell(table, String.valueOf(row.total()), bodyFont, Element.ALIGN_CENTER);
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new InvalidNeighborhoodWeeklyReportException("Nao foi possivel gerar o relatorio PDF: " + exception.getMessage());
        } catch (Exception exception) {
            throw new InvalidNeighborhoodWeeklyReportException("Erro ao montar o relatorio PDF: " + exception.getMessage());
        }
    }

    private String buildReportSubtitle(ReportFilters filters, int semanaInicial, int semanaFinal) {
        List<String> details = new ArrayList<>();
        details.add("Semanas " + semanaInicial + " a " + semanaFinal);

        if (filters.year() != null) {
            details.add("Ano " + filters.year());
        }
        if (filters.agravoId() != null) {
            details.add("Agravo " + filters.agravoId());
        }
        if (filters.bairro() != null && !filters.bairro().isBlank()) {
            details.add("Bairro " + filters.bairro());
        }

        return String.join(" | ", details);
    }

    private void addHeaderCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        cell.setBackgroundColor(new Color(41, 76, 122));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String value, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private record ReportFilters(String agravoId, Integer year, String bairro) {
    }

    private static final class NeighborhoodWeeklyRow {
        private final String bairro;
        private final Map<Integer, Long> weekCounts;
        private long total;

        private NeighborhoodWeeklyRow(String bairro, Map<Integer, Long> weekCounts, long total) {
            this.bairro = bairro;
            this.weekCounts = weekCounts;
            this.total = total;
        }

        static NeighborhoodWeeklyRow empty(String bairro, int semanaInicial, int semanaFinal) {
            Map<Integer, Long> weekCounts = new LinkedHashMap<>();
            for (int semana = semanaInicial; semana <= semanaFinal; semana++) {
                weekCounts.put(semana, 0L);
            }
            return new NeighborhoodWeeklyRow(bairro, weekCounts, 0L);
        }

        String bairro() {
            return bairro;
        }

        Map<Integer, Long> weekCounts() {
            return weekCounts;
        }

        long total() {
            return total;
        }

        void setTotal(long total) {
            this.total = total;
        }
    }
}
