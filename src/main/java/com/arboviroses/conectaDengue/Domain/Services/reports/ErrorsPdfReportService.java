package com.arboviroses.conectaDengue.Domain.Services.reports;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.arboviroses.conectaDengue.Api.DTO.response.NotificationErrorWithCategoryDTO;
import com.arboviroses.conectaDengue.Domain.Services.Notifications.NotificationsErrorService;
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
public class ErrorsPdfReportService {

    private final NotificationsErrorService notificationsErrorService;

    private static final Map<String, String> CATEGORY_LABELS = Map.of(
        "BAIRRO_FALTANDO",        "Bairro faltando",
        "DOENCA_NAO_INFORMADA",   "Doença não informada",
        "CLASSIFICACAO_FALTANDO", "Classificação faltando",
        "DATA_FALTANDO",          "Data faltando",
        "SEXO_NAO_INFORMADO",     "Sexo não informado",
        "EVOLUCAO_NAO_INFORMADA",   "Evolução não informada",
        "DATA_NASCIMENTO_FALTANDO", "Data de nascimento faltando",
        "OUTROS",                   "Outros"
    );

    private static final Map<String, String> DOENCA_LABELS = Map.of(
        "A90",   "Dengue",
        "A92.0", "Chikungunya",
        "A928",  "Zika"
    );

    public byte[] generateReport(String category, Long startDate, Long endDate, String idAgravo) {
        List<NotificationErrorWithCategoryDTO> records =
            notificationsErrorService.getAllErrorsFiltered(category, startDate, endDate, idAgravo);
        return createPdf(records, category, startDate, endDate, idAgravo);
    }

    private byte[] createPdf(List<NotificationErrorWithCategoryDTO> records, String category, Long startDate, Long endDate, String idAgravo) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font headerFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font bodyFont     = FontFactory.getFont(FontFactory.HELVETICA, 8);

            Paragraph title = new Paragraph("Relatório — Notificações com Erros", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            SimpleDateFormat sdfLabel = new SimpleDateFormat("dd/MM/yyyy");
            List<String> filterParts = new java.util.ArrayList<>();
            filterParts.add("Problema: " + (category != null && !category.isBlank()
                ? CATEGORY_LABELS.getOrDefault(category, category) : "Todos"));
            filterParts.add("Doença: " + (idAgravo != null && !idAgravo.isBlank()
                ? DOENCA_LABELS.getOrDefault(idAgravo, idAgravo) : "Todas"));
            if (startDate != null) filterParts.add("De: " + sdfLabel.format(new java.util.Date(startDate)));
            if (endDate != null)   filterParts.add("Até: " + sdfLabel.format(new java.util.Date(endDate)));
            filterParts.add("Gerado em: " + sdfLabel.format(new java.util.Date()));
            filterParts.add("Total: " + records.size() + " registro(s)");

            Paragraph subtitle = new Paragraph(String.join("   |   ", filterParts), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(12f);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{2f, 2f, 2.5f, 1.5f, 2.5f, 2f, 1.5f, 3f});

            for (String h : new String[]{"Doença", "Data Notif.", "Bairro", "Sexo", "Classificação", "Evolução", "Sem. Epid.", "Problema"}) {
                addHeaderCell(table, h, headerFont);
            }

            for (NotificationErrorWithCategoryDTO r : records) {
                String agravo = r.getIdAgravo() != null
                    ? DOENCA_LABELS.getOrDefault(r.getIdAgravo(), r.getIdAgravo())
                    : "—";
                addBodyCell(table, agravo, bodyFont, Element.ALIGN_LEFT);
                addBodyCell(table, r.getDataNotification() != null ? sdfLabel.format(r.getDataNotification()) : "—", bodyFont, Element.ALIGN_CENTER);
                addBodyCell(table, r.getNomeBairro() != null ? r.getNomeBairro() : "—", bodyFont, Element.ALIGN_LEFT);
                addBodyCell(table, r.getSexo() != null ? r.getSexo() : "—", bodyFont, Element.ALIGN_CENTER);
                addBodyCell(table, r.getClassificacao() != null ? r.getClassificacao() : "—", bodyFont, Element.ALIGN_LEFT);
                addBodyCell(table, r.getEvolucao() != null ? r.getEvolucao() : "—", bodyFont, Element.ALIGN_LEFT);
                addBodyCell(table, r.getSemanaEpidemiologica() > 0 ? String.valueOf(r.getSemanaEpidemiologica()) : "—", bodyFont, Element.ALIGN_CENTER);
                addBodyCell(table, CATEGORY_LABELS.getOrDefault(r.getCategory() != null ? r.getCategory() : "OUTROS", "Outros"), bodyFont, Element.ALIGN_LEFT);
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Não foi possível gerar o relatório PDF: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao montar o relatório PDF: " + e.getMessage());
        }
    }

    private void addHeaderCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setNoWrap(true);
        cell.setPadding(4f);
        cell.setBackgroundColor(new Color(41, 76, 122));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String value, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setNoWrap(false);
        cell.setPadding(4f);
        table.addCell(cell);
    }
}
