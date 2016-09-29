package com.pasi.pdfbox;

import com.pasi.pdfbox.bean.BloodPressureRecord;
import com.pasi.pdfbox.bean.PatientBloodPressureReport;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");

        PatientBloodPressureReport report = createPatientBloodPressureReport();
        PDDocument document = new PDDocument();
        DocWriter writer = new PBPRptDocWriter(document, report);
        writer.write("/Users/bean/Desktop/Patient_BP_Report-v0.08.pdf");

        /*
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        try {
            PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
            float pageHeight = page.getMediaBox().getHeight() - 20;

            PDText text = new PDText(20, pageHeight, "The Apache PDFBox® library is an open source Java tool for working with PDF documents. This project allows creation of new PDF documents, manipulation of existing documents and the ability to extract content from documents. Apache PDFBox also includes several command line utilities. Apache PDFBox is published under the Apache License v2.0.");

            float height = writeText(contents, text);

            PDText title = new PDText(20, pageHeight - height - 20, "Bar Chart Example");

            writeText(contents, title);

            JFreeChart chart = createBarChart();
            BufferedImage image = chart.createBufferedImage(600, 400);
            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
            float scale = 1f;
            //contents.drawImage(pdImage, 20, 500, pdImage.getWidth() * scale, pdImage.getHeight() * scale);

            contents.close();

            doc.save(new File("/Users/bean/Desktop/Patient_BP_Report-v0.08.pdf"));
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

    private static JFreeChart createBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(7445, "JFreeSVG", "Warm-up");
        dataset.addValue(24448, "Batik", "Warm-up");
        dataset.addValue(4297, "JFreeSVG", "Test");
        dataset.addValue(21022, "Batik", "Test");

        JFreeChart chart = ChartFactory.createBarChart(
                "Performance: JFreeSVG vs Batik", null /* x-axis label*/,
                "Milliseconds" /* y-axis label */, dataset);
        chart.addSubtitle(new TextTitle("Time to generate 1000 charts in SVG "
                + "format (lower bars = better performance)"));
        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        chart.getLegend().setFrame(BlockBorder.NONE);
        return chart;
    }

    private static PatientBloodPressureReport createPatientBloodPressureReport() {
        PatientBloodPressureReport report = new PatientBloodPressureReport();
        report.setPatientName("Bean Zeng");
        report.setDuration(3);
        report.setReportDate(new Date());
        report.setComments("N/A");
        report.setDesiredSystolic(80);
        report.setDesiredDiastolic(120);
        List<BloodPressureRecord> records = new ArrayList<BloodPressureRecord>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            BloodPressureRecord record = new BloodPressureRecord();
            record.setDate(new Date(System.currentTimeMillis() - (10 - i) * 24 * 60 * 60 * 1000));
            record.setSystolic(80 + random.nextInt(10));
            record.setDiastolic(120 + random.nextInt(20));
            record.setPulse(75 + random.nextInt(10));
            record.setSource("Source #" + i);
            records.add(record);
        }
        report.setBloodPressureRecords(records);
        return report;
    }

    private static void addHeader(PDPageContentStream contents) {

    }
}
