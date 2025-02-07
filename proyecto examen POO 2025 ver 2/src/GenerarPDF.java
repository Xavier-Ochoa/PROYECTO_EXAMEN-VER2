import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GenerarPDF {
    public void generarPDF(JTable tabla, JList listaFacturas, JLabel usuarioLabel, JLabel correoLabel) {
        try {
            // Obtener los datos para el encabezado
            String nombreUsuario = usuarioLabel.getText();
            String correoUsuario = correoLabel.getText();
            String encabezado = "Reporte de Productos\nUsuario: " + nombreUsuario + "\nCorreo: " + correoUsuario;

            // Elegir la ubicación del archivo PDF
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar PDF");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));

            int seleccion = fileChooser.showSaveDialog(null);
            if (seleccion == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                String rutaArchivo = archivo.getAbsolutePath();
                if (!rutaArchivo.toLowerCase().endsWith(".pdf")) {
                    rutaArchivo += ".pdf";
                }

                // Crear documento PDF
                Document documento = new Document();
                PdfWriter.getInstance(documento, new FileOutputStream(rutaArchivo));
                documento.open();

                // Agregar encabezado
                documento.add(new com.itextpdf.text.Paragraph(encabezado));

                // Crear la tabla con los datos de la JTable
                PdfPTable tablaPDF = new PdfPTable(tabla.getColumnCount());

                // Agregar nombres de las columnas a la tabla PDF
                TableModel modelo = tabla.getModel();
                for (int i = 0; i < modelo.getColumnCount(); i++) {
                    tablaPDF.addCell(new Phrase(modelo.getColumnName(i), FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                }

                // Agregar los datos de la tabla
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    for (int j = 0; j < modelo.getColumnCount(); j++) {
                        tablaPDF.addCell(modelo.getValueAt(i, j).toString());
                    }
                }

                // Añadir la tabla al documento PDF
                documento.add(tablaPDF);

                // Cerrar el documento
                documento.close();

                // Confirmar que el PDF fue guardado correctamente
                JOptionPane.showMessageDialog(null, "PDF guardado en: " + rutaArchivo);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage());
        }
    }
}
