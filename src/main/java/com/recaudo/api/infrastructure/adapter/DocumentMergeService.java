package com.recaudo.api.infrastructure.adapter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class DocumentMergeService {

    /**
     * Combina múltiples archivos (imágenes o PDFs) en un solo PDF
     */
    public byte[] mergeDocumentsToPdf(List<MultipartFile> files) throws IOException {
        PDDocument mergedDocument = new PDDocument();

        try {
            for (MultipartFile file : files) {
                String contentType = file.getContentType();

                if (contentType != null && contentType.startsWith("image/")) {
                    // Es una imagen (PNG, JPG, etc.)
                    addImageAsPdfPage(mergedDocument, file);
                } else if (contentType != null && contentType.equals("application/pdf")) {
                    // Es un PDF
                    addPdfPages(mergedDocument, file);
                } else {
                    throw new IllegalArgumentException(
                        "Tipo de archivo no soportado: " + contentType
                    );
                }
            }

            // Convertir el documento a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mergedDocument.save(outputStream);
            return outputStream.toByteArray();

        } finally {
            mergedDocument.close();
        }
    }

    /**
     * Agrega una imagen como página de PDF
     */
    private void addImageAsPdfPage(PDDocument document, MultipartFile imageFile) 
            throws IOException {
        
        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        
        // Crear página con tamaño carta (ajustable)
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Convertir imagen a PDImageXObject
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = getImageFormat(imageFile.getContentType());
        ImageIO.write(image, formatName, baos);
        
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(
            document, 
            baos.toByteArray(), 
            imageFile.getOriginalFilename()
        );

        // Calcular dimensiones para ajustar la imagen a la página
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        
        float imageWidth = pdImage.getWidth();
        float imageHeight = pdImage.getHeight();
        
        // Mantener proporción
        float scale = Math.min(
            pageWidth / imageWidth, 
            pageHeight / imageHeight
        );
        
        float scaledWidth = imageWidth * scale;
        float scaledHeight = imageHeight * scale;
        
        // Centrar imagen
        float x = (pageWidth - scaledWidth) / 2;
        float y = (pageHeight - scaledHeight) / 2;

        // Dibujar imagen en la página
        PDPageContentStream contentStream = new PDPageContentStream(
            document, 
            page, 
            PDPageContentStream.AppendMode.APPEND, 
            true
        );
        
        contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
        contentStream.close();
    }

    /**
     * Agrega todas las páginas de un PDF existente
     */
    private void addPdfPages(PDDocument targetDocument, MultipartFile pdfFile) 
            throws IOException {
        
        PDDocument sourceDocument = PDDocument.load(pdfFile.getInputStream());
        
        try {
            for (PDPage page : sourceDocument.getPages()) {
                targetDocument.addPage(page);
            }
        } finally {
            sourceDocument.close();
        }
    }

    /**
     * Obtiene el formato de imagen del content type
     */
    private String getImageFormat(String contentType) {
        if (contentType == null) return "jpg";
        
        if (contentType.contains("png")) return "png";
        if (contentType.contains("jpeg") || contentType.contains("jpg")) return "jpg";
        if (contentType.contains("gif")) return "gif";
        
        return "jpg"; // Default
    }
}