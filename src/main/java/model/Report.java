package model;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Report {
    public static void createPattern(String fileName) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();

        // spreadsheet object
        HSSFSheet spreadsheet
                = workbook.createSheet(" report pattern ");

        // creating a row object
        HSSFRow row;
        row = spreadsheet.createRow(0);
        int cellnum = 0;
        Cell cell = row.createCell(cellnum++);
        cell.setCellValue("Name");
        cell = row.createCell(cellnum++);
        cell.setCellValue("Amount");
        cell = row.createCell(cellnum++);
        cell.setCellValue("Creation date");
        cell = row.createCell(cellnum++);
        cell.setCellValue("Type");
        cell = row.createCell(cellnum++);
        cell.setCellValue("Sum");
        FileOutputStream out = new FileOutputStream(
                new File("C:\\ilm\\mokslas\\7semestr\\AccountingHibernate\\src\\main\\resources\\patterns\\report pattern.csv"));

        workbook.write(out);
        out.close();
        File file = new File("C:\\ilm\\mokslas\\7semestr\\AccountingHibernate\\src\\main\\resources\\patterns\\report pattern.csv");
        Desktop.getDesktop().open(file);
    }
    public static String getFolderPath()
    {
        JFileChooser file = new JFileChooser();
        file.setMultiSelectionEnabled(true);
        file.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        file.setFileHidingEnabled(false);
        if (file.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            java.io.File f = file.getSelectedFile();
            return f.getPath();
        }
        return "error";
    }
}
