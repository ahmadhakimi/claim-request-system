package com.project.claim.system.service;

import com.project.claim.system.dto.ClaimDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PDFGeneratorService {

    public ResponseEntity<Resource> generateClaimsPDF(List<ClaimDTO> claims) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.newLineAtOffset(30, 750);
            contentStream.showText("CLAIMS RECORD");
            contentStream.endText();

            float startY = 725f; // Adjust the startY position for the table
            float lineHeight = 20f;

            // Define custom widths for each cell
            float[] cellWidths = {100f, 80f, 80f, 90f, 90f, 100f}; // Adjust as needed

            // Draw table header
            drawTableHeader(contentStream, startY, cellWidths);
            startY -= lineHeight; // Move startY position for drawing table rows

            // Draw table rows
            for (ClaimDTO claim : claims) {
                startY = drawTableRow(contentStream, claim, startY, cellWidths, lineHeight);
            }

            contentStream.close();

            // Add background image to each page (if needed)

            document.save(outputStream);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Monthly Claims Record.pdf");

            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(outputStream.size())
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private float drawTableRow(PDPageContentStream contentStream, ClaimDTO claim, float startY, float[] cellWidths, float lineHeight) throws IOException {
        float currentY = startY;
        float startX = 30; // Starting X position for the first cell

        // Draw border lines for each row
        contentStream.moveTo(startX, currentY);
        contentStream.lineTo(startX + calculateTotalWidth(cellWidths), currentY); // Top horizontal line
        contentStream.stroke();

        // Draw each cell in the row
        for (int i = 0; i < cellWidths.length; i++) {
            String cellValue = getCellValue(claim, i, cellWidths[i]);

            // Draw borderlines for each cell
            contentStream.moveTo(startX, currentY);
            contentStream.lineTo(startX, currentY - lineHeight); // Left vertical line
            contentStream.moveTo(startX, currentY - lineHeight);
            contentStream.lineTo(startX + cellWidths[i], currentY - lineHeight); // Bottom horizontal line
            contentStream.moveTo(startX + cellWidths[i], currentY - lineHeight);
            contentStream.lineTo(startX + cellWidths[i], currentY); // Right vertical line
            contentStream.stroke();

            // Draw text in the cell
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(startX + 2, currentY - 12); // Adjust for text positioning
            contentStream.showText(cellValue);
            contentStream.endText();

            startX += cellWidths[i]; // Move to the next cell's starting X position
        }

        return startY - lineHeight; // Return updated startY position for the next row
    }

    private float calculateTotalWidth(float[] cellWidths) {
        float totalWidth = 0;
        for (float width : cellWidths) {
            totalWidth += width;
        }
        return totalWidth;
    }



    private void drawTableHeader(PDPageContentStream contentStream, float startY, float[] cellWidths) throws IOException {
        float currentY = startY;
        float startX = 30; // Starting X position for the first cell

        // Draw border lines for the header row
        contentStream.moveTo(startX, currentY);
        contentStream.lineTo(startX + calculateTotalWidth(cellWidths), currentY); // Top horizontal line
        contentStream.stroke();

        // Draw each cell in the header row
        for (int i = 0; i < cellWidths.length; i++) {
            // Draw border lines for each cell
            contentStream.moveTo(startX, currentY);
            contentStream.lineTo(startX, currentY - 20); // Left vertical line
            contentStream.moveTo(startX, currentY - 20);
            contentStream.lineTo(startX + cellWidths[i], currentY - 20); // Bottom horizontal line
            contentStream.moveTo(startX + cellWidths[i], currentY - 20);
            contentStream.lineTo(startX + cellWidths[i], currentY); // Right vertical line
            contentStream.stroke();

            // Draw text in the cell
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.newLineAtOffset(startX + 2, currentY - 12); // Adjust for text positioning
            contentStream.showText(getHeaderName(i)); // Use the method to get header cell values
            contentStream.endText();

            startX += cellWidths[i]; // Move to the next cell's starting X position
        }
    }


    private String getCellValue(ClaimDTO claim, int columnIndex, float cellWidth) {
        switch (columnIndex) {
            case 0:
                return capitalize(String.valueOf(claim.getName()));
            case 1:
                return String.valueOf(claim.getAmount());
            case 2:
                return claim.getReceiptNo();
            case 3:
                return formatDate(claim.getReceiptDate());
            case 4:
                return capitalize(claim.getStatus().toString());
            case 5:
                return claim.getFullName();
            default:
                return "";
        }
    }

    public String capitalize (String str) {
        if (str == null){
            return null;
        }
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return (date != null) ? dateFormat.format(date) : "";
    }

    private String getHeaderName(int index) {
        // Define header names based on the index
        switch (index) {
            case 0:
                return "Name";
            case 1:
                return "Amount";
            case 2:
                return "Receipt No";
            case 3:
                return "Receipt Date";
            case 4:
                return "Status";
            case 5:
                return "Full Name";
            default:
                return "";
        }
    }
}
