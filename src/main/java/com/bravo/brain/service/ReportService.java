package com.bravo.brain.service;

import com.bravo.brain.domain.entity.Department;
import com.bravo.brain.domain.entity.WasteLog;
import com.bravo.brain.domain.repository.DepartmentRepository;
import com.bravo.brain.domain.repository.WasteLogRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final WasteLogRepository wasteRepo;
    private final DepartmentRepository departmentRepo;

    public byte[] generateWastePdf(String storeName, Long departmentId, String period) {
        try {
            // ── TARİX ARALIĞI ─────────────────────────────
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime from = switch (period.toUpperCase()) {
                case "WEEKLY"    -> now.minusWeeks(1);
                case "QUARTERLY" -> now.minusMonths(3);
                case "YEARLY"    -> now.minusYears(1);
                default          -> now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            };

            // ── DATA GƏTİR ────────────────────────────────
            List<WasteLog> logs;
            String deptName = "All Departments";

            if (departmentId != null) {
                Department dept = departmentRepo.findById(departmentId)
                        .orElseThrow(() -> new RuntimeException("Şöbə tapılmadı"));
                deptName = dept.getName();
                logs = wasteRepo.findByDepartmentAndDateBetween(deptName, from, now);
            } else {
                logs = wasteRepo.findByStoreAndDateBetween(storeName, from, now);
            }

            double totalLoss = logs.stream()
                    .mapToDouble(WasteLog::getTotalLoss).sum();

            // ── PDF YARAT ─────────────────────────────────
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fontlar
            Font titleFont   = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD,
                    new BaseColor(26, 83, 52));
            Font headingFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD,
                    new BaseColor(26, 83, 52));
            Font normalFont  = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL,
                    BaseColor.DARK_GRAY);
            Font boldFont    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font smallFont   = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL,
                    BaseColor.GRAY);
            Font redFont     = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,
                    new BaseColor(220, 53, 69));
            Font greenFont   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,
                    new BaseColor(26, 83, 52));

            // ── HEADER ────────────────────────────────────
            Paragraph title = new Paragraph("FreshGuard", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Waste Management Report", normalFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));

            // Separator xətti
            LineSeparator line = new LineSeparator();
            line.setLineColor(new BaseColor(26, 83, 52));
            document.add(new Chunk(line));
            document.add(new Paragraph(" "));

            // ── REPORT INFO ───────────────────────────────
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            addInfoCell(infoTable, "Store:", storeName, boldFont, normalFont);
            addInfoCell(infoTable, "Department:", deptName, boldFont, normalFont);
            addInfoCell(infoTable, "Period:", period, boldFont, normalFont);
            addInfoCell(infoTable, "Generated:",
                    now.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                    boldFont, normalFont);
            addInfoCell(infoTable, "From:",
                    from.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    boldFont, normalFont);
            addInfoCell(infoTable, "To:",
                    now.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    boldFont, normalFont);

            document.add(infoTable);

            // ── KPI SUMMARY ───────────────────────────────
            Paragraph kpiTitle = new Paragraph("Summary", headingFont);
            kpiTitle.setSpacingAfter(8);
            document.add(kpiTitle);

            PdfPTable kpiTable = new PdfPTable(3);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(20);

            addKpiCell(kpiTable, "Total Waste",
                    String.format("%.2f AZN", totalLoss), redFont);
            addKpiCell(kpiTable, "Total Incidents",
                    String.valueOf(logs.size()), boldFont);
            addKpiCell(kpiTable, "Avg per Incident",
                    logs.isEmpty() ? "0.00 AZN" :
                            String.format("%.2f AZN", totalLoss / logs.size()), boldFont);

            document.add(kpiTable);

            // ── WASTE LOGS TABLE ──────────────────────────
            if (!logs.isEmpty()) {
                Paragraph tableTitle = new Paragraph("Waste Log Details", headingFont);
                tableTitle.setSpacingAfter(8);
                document.add(tableTitle);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 2f, 1.5f, 1.5f, 2f});
                table.setSpacingAfter(20);

                // Header row
                String[] headers = {"Product", "Department", "Quantity", "Loss (AZN)", "Reason"};
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
                    cell.setBackgroundColor(new BaseColor(26, 83, 52));
                    cell.setPadding(8);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    Font whiteFont = new Font(Font.FontFamily.HELVETICA, 10,
                            Font.BOLD, BaseColor.WHITE);
                    cell.setPhrase(new Phrase(h, whiteFont));
                    table.addCell(cell);
                }

                // Data rows
                boolean alternate = false;
                for (WasteLog log2 : logs) {
                    BaseColor rowColor = alternate
                            ? new BaseColor(245, 250, 247)
                            : BaseColor.WHITE;

                    addTableCell(table, log2.getProduct().getName(), normalFont, rowColor);
                    addTableCell(table, log2.getDepartmentName() != null
                            ? log2.getDepartmentName() : "-", normalFont, rowColor);
                    addTableCell(table,
                            String.format("%.1f", log2.getQuantity()), normalFont, rowColor);
                    addTableCell(table,
                            String.format("%.2f", log2.getTotalLoss()), redFont, rowColor);
                    addTableCell(table,
                            log2.getReason() != null ? log2.getReason().name() : "-",
                            normalFont, rowColor);

                    alternate = !alternate;
                }
                document.add(table);
            } else {
                Paragraph noData = new Paragraph(
                        "No waste records found for this period.", normalFont);
                noData.setSpacingAfter(20);
                document.add(noData);
            }

            // ── FOOTER ────────────────────────────────────
            document.add(new Chunk(line));
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph(
                    "Generated by FreshGuard AI • Bravo Supermarket • " +
                            now.format(DateTimeFormatter.ofPattern("yyyy")), smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF generate xətası: {}", e.getMessage());
            throw new RuntimeException("PDF yaradıla bilmədi: " + e.getMessage());
        }
    }

    // ── HELPERS ───────────────────────────────────────────
    private void addInfoCell(PdfPTable table, String label, String value,
                             Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addKpiCell(PdfPTable table, String label,
                            String value, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBackgroundColor(new BaseColor(245, 250, 247));

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n",
                new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY)));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text,
                              Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}