package com.proyecto.TurboMechanics.service;
 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
 
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
 
import com.proyecto.TurboMechanics.dto.CriticalStockResponseDTO;
import com.proyecto.TurboMechanics.dto.PopularSpacePartsResponseDTO;
 
import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class ExportService {
    
    /**
     * Genera un archivo Excel con el reporte de repuestos en stock crítico.
     *
     * @param data lista de repuestos con stock crítico
     * @return archivo Excel en formato byte[] listo para descarga
     * @throws IOException si ocurre un error al crear el archivo Excel
     */
    public byte[] exportCriticalStockExcel(List<CriticalStockResponseDTO> data)
            throws IOException {
 
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
 
            Sheet sheet = workbook.createSheet("Stock Crítico");
 
            String[] headers = {"ID", "Nombre", "Referencia", "Cantidad", "Stock Mínimo", "Estado"};
            Row headerRow = sheet.createRow(0);
            XSSFCellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
 
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
            }
 
            int rowIdx = 1;
            for (CriticalStockResponseDTO item : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getSpacePartsId());
                row.createCell(1).setCellValue(item.getName());
                row.createCell(2).setCellValue(item.getReference());
                row.createCell(3).setCellValue(item.getCurrentStock());
                row.createCell(4).setCellValue(item.getStockMin());
                row.createCell(5).setCellValue(item.getStatus());
            }
 
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
 
            workbook.write(out);
            return out.toByteArray();
        }
    }
 
    /**
     * Genera un archivo Excel con el reporte de los repuestos más utilizados.
     *
     * @param datos lista de repuestos más usados
     * @return archivo Excel en formato byte[] listo para descarga
     * @throws IOException si ocurre un error al crear el archivo Excel
     */
    public byte[] exportSparesPopularExcel(List<PopularSpacePartsResponseDTO> datos)
            throws IOException {
 
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
 
            Sheet sheet = workbook.createSheet("Repuestos Más Usados");
            String[] headers = {"ID", "Nombre", "Referencia", "Total Salidas"};
            Row headerRow = sheet.createRow(0);
            XSSFCellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
 
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
            }
 
            int rowIdx = 1;
            for (PopularSpacePartsResponseDTO item : datos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getSpacePartsId());
                row.createCell(1).setCellValue(item.getName());
                row.createCell(2).setCellValue(item.getReference());
                row.createCell(3).setCellValue(item.getTotalOutput());
            }
 
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        }
    }
}