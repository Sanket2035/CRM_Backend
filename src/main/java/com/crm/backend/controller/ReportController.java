package com.crm.backend.controller;

import com.crm.backend.model.Campaigns;
import com.crm.backend.model.Contacts;
import com.crm.backend.model.Leads;
import com.crm.backend.repository.CampaignsRepository;
import com.crm.backend.repository.ContactsRepository;
import com.crm.backend.repository.LeadsRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('Admin', 'Sales')")
public class ReportController {

    @Autowired
    private LeadsRepository leadRepository;

    @Autowired
    private ContactsRepository contactRepository;

    @Autowired
    private CampaignsRepository campaignRepository;

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ----------------------------------------------------
            // SHEET 1: LEADS DIRECTORY
            // ----------------------------------------------------
            Sheet leadSheet = workbook.createSheet("CRM Leads");
            Row leadHeader = leadSheet.createRow(0);
            String[] leadHeaders = {"ID", "Name", "Company", "Email", "Value ($)", "Score", "Stage", "Phone", "Created"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < leadHeaders.length; i++) {
                Cell cell = leadHeader.createCell(i);
                cell.setCellValue(leadHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Leads> leads = leadRepository.findAll();
            int rowIdx = 1;
            for (Leads lead : leads) {
                Row row = leadSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(lead.getId() != null ? lead.getId() : 0);
                row.createCell(1).setCellValue(lead.getName());
                row.createCell(2).setCellValue(lead.getCompany());
                row.createCell(3).setCellValue(lead.getEmail());
                row.createCell(4).setCellValue(lead.getDealValue());
                row.createCell(5).setCellValue(lead.getScore());
                row.createCell(6).setCellValue(lead.getStage());
                row.createCell(7).setCellValue(lead.getPhone());
                row.createCell(8).setCellValue(lead.getCreated());
            }

            // Auto-size columns
            for (int i = 0; i < leadHeaders.length; i++) {
                leadSheet.autoSizeColumn(i);
            }

            // ----------------------------------------------------
            // SHEET 2: CONTACTS DIRECTORY
            // ----------------------------------------------------
            Sheet contactSheet = workbook.createSheet("CRM Contacts");
            Row contactHeader = contactSheet.createRow(0);
            String[] contactHeaders = {"ID", "Name", "Company", "Email", "Phone", "Status"};

            for (int i = 0; i < contactHeaders.length; i++) {
                Cell cell = contactHeader.createCell(i);
                cell.setCellValue(contactHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Contacts> contacts = contactRepository.findAll();
            rowIdx = 1;
            for (Contacts c : contacts) {
                Row row = contactSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId() != null ? c.getId() : 0);
                row.createCell(1).setCellValue(c.getName());
                row.createCell(2).setCellValue(c.getCompany());
                row.createCell(3).setCellValue(c.getEmail());
                row.createCell(4).setCellValue(c.getPhone());
                row.createCell(5).setCellValue(c.getStatus());
            }

            for (int i = 0; i < contactHeaders.length; i++) {
                contactSheet.autoSizeColumn(i);
            }

            // ----------------------------------------------------
            // SHEET 3: MARKETING CAMPAIGNS
            // ----------------------------------------------------
            Sheet campaignSheet = workbook.createSheet("CRM Campaigns");
            Row campaignHeader = campaignSheet.createRow(0);
            String[] campaignHeaders = {"ID", "Campaign Name", "Type", "Status", "Budget ($)", "Revenue ($)", "Reach", "Conversions"};

            for (int i = 0; i < campaignHeaders.length; i++) {
                Cell cell = campaignHeader.createCell(i);
                cell.setCellValue(campaignHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Campaigns> campaigns = campaignRepository.findAll();
            rowIdx = 1;
            for (Campaigns camp : campaigns) {
                Row row = campaignSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(camp.getId() != null ? camp.getId() : 0);
                row.createCell(1).setCellValue(camp.getName());
                row.createCell(2).setCellValue(camp.getType());
                row.createCell(3).setCellValue(camp.getStatus());
                row.createCell(4).setCellValue(camp.getBudget());
                row.createCell(5).setCellValue(camp.getRevenue());
                row.createCell(6).setCellValue(camp.getReach());
                row.createCell(7).setCellValue(camp.getConversions());
            }

            for (int i = 0; i < campaignHeaders.length; i++) {
                campaignSheet.autoSizeColumn(i);
            }

            workbook.write(out);
            byte[] bytes = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ApexCRM_Enterprise_Report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/pdf/{leadId}")
    public ResponseEntity<?> exportLeadToPdf(@PathVariable Long leadId) {
        return leadRepository.findById(leadId).map(lead -> {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 50, 50, 50, 50);
                com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
                document.open();

                // Add branding Title
                com.lowagie.text.Font titleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 22, new java.awt.Color(99, 102, 241));
                com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("ApexCRM Enterprise Agreement", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(25);
                document.add(title);

                // Add metadata block
                com.lowagie.text.Font subtitleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 12, new java.awt.Color(31, 41, 55));
                com.lowagie.text.Font textFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 10, new java.awt.Color(75, 85, 99));

                com.lowagie.text.Paragraph meta = new com.lowagie.text.Paragraph();
                meta.add(new com.lowagie.text.Chunk("Document ID: ", subtitleFont));
                meta.add(new com.lowagie.text.Chunk("AGR-" + lead.getId() + "-" + System.currentTimeMillis() % 100000 + "\n", textFont));
                meta.add(new com.lowagie.text.Chunk("Date Issued: ", subtitleFont));
                meta.add(new com.lowagie.text.Chunk(lead.getCreated() != null ? lead.getCreated() : "Just Now", textFont));
                meta.add("\n");
                meta.setSpacingAfter(20);
                document.add(meta);

                // Add Horizontal Rule
                com.lowagie.text.Paragraph hr = new com.lowagie.text.Paragraph("----------------------------------------------------------------------------------------------------------------------------------", textFont);
                hr.setSpacingAfter(15);
                document.add(hr);

                // Section: Parties details
                com.lowagie.text.Paragraph parties = new com.lowagie.text.Paragraph();
                parties.add(new com.lowagie.text.Chunk("CLIENT INVOICE & CONTRACTUAL DETAILS\n", subtitleFont));
                parties.add(new com.lowagie.text.Chunk("Client Name: ", subtitleFont));
                parties.add(new com.lowagie.text.Chunk(lead.getName() + "\n", textFont));
                parties.add(new com.lowagie.text.Chunk("Organization: ", subtitleFont));
                parties.add(new com.lowagie.text.Chunk(lead.getCompany() + "\n", textFont));
                parties.add(new com.lowagie.text.Chunk("Email Address: ", subtitleFont));
                parties.add(new com.lowagie.text.Chunk(lead.getEmail() + "\n", textFont));
                parties.add(new com.lowagie.text.Chunk("Contact Phone: ", subtitleFont));
                parties.add(new com.lowagie.text.Chunk(lead.getPhone() != null ? lead.getPhone() : "N/A", textFont));
                parties.setSpacingAfter(25);
                document.add(parties);

                // Table for Services
                com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(3);
                table.setWidthPercentage(100);
                table.setSpacingAfter(30);

                // Headers
                com.lowagie.text.pdf.PdfPCell h1 = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("Service Description", subtitleFont));
                h1.setBackgroundColor(new java.awt.Color(243, 244, 246));
                com.lowagie.text.pdf.PdfPCell h2 = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("Status", subtitleFont));
                h2.setBackgroundColor(new java.awt.Color(243, 244, 246));
                com.lowagie.text.pdf.PdfPCell h3 = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("Deal Value", subtitleFont));
                h3.setBackgroundColor(new java.awt.Color(243, 244, 246));

                table.addCell(h1);
                table.addCell(h2);
                table.addCell(h3);

                // Data Rows
                table.addCell(new com.lowagie.text.Phrase("Enterprise Licensing & CRM Access Scope Setup (" + lead.getCompany() + ")", textFont));
                table.addCell(new com.lowagie.text.Phrase(lead.getStage(), textFont));
                table.addCell(new com.lowagie.text.Phrase("$" + String.format("%,d", lead.getDealValue()), textFont));

                document.add(table);

                // Total Summary
                com.lowagie.text.Paragraph summary = new com.lowagie.text.Paragraph();
                summary.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
                summary.add(new com.lowagie.text.Chunk("Total Subscribed Revenue: ", subtitleFont));
                summary.add(new com.lowagie.text.Chunk("$" + String.format("%,d", lead.getDealValue()) + " USD\n", titleFont));
                summary.setSpacingAfter(50);
                document.add(summary);

                // Footer / Signatures
                com.lowagie.text.Paragraph footer = new com.lowagie.text.Paragraph();
                footer.add(new com.lowagie.text.Chunk("TERMS & CONDITIONS\n", subtitleFont));
                footer.add(new com.lowagie.text.Chunk("This agreement serves as a valid invoice representation of the enterprise lead. ApexCRM warrants the delivery of licensed CRM scopes upon signature ratification.\n\n\n\n", textFont));
                document.add(footer);

                // Two columns for signatures
                com.lowagie.text.pdf.PdfPTable sigTable = new com.lowagie.text.pdf.PdfPTable(2);
                sigTable.setWidthPercentage(100);

                com.lowagie.text.pdf.PdfPCell clientSig = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("_________________________________________\nClient Authorized Signature", textFont));
                clientSig.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                com.lowagie.text.pdf.PdfPCell sellerSig = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("_________________________________________\nApexCRM Representative Signature", textFont));
                sellerSig.setBorder(com.lowagie.text.Rectangle.NO_BORDER);

                sigTable.addCell(clientSig);
                sigTable.addCell(sellerSig);
                document.add(sigTable);

                document.close();
                byte[] bytes = out.toByteArray();

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ApexCRM_Agreement_" + leadId + ".pdf");

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(bytes);

            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
