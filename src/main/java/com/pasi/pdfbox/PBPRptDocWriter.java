package com.pasi.pdfbox;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.utils.FontUtils;
import com.pasi.pdfbox.bean.BloodPressureRecord;
import com.pasi.pdfbox.bean.PatientBloodPressureReport;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bean on 9/27/16.
 */
public class PBPRptDocWriter extends DocWriter {
    private PatientBloodPressureReport report;

    public PBPRptDocWriter(PDDocument document, PatientBloodPressureReport report) {
        super.document = document;
        this.report = report;
        addNewPage();
    }

    @Override
    float composeHeader() throws IOException {
        return 0;
    }

    @Override
    float composeBody() throws IOException {
        String text = "Dear " + report.getPatientName() + ",";
        //float textWidth = getStringWidth(DEFAULT_FONT, text);
        //System.out.println("text width = " + textWidth);
        contents.beginText();
        contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        contents.showText(text);
        contents.endText();

        currentY -= (DEFAULT_FONT_HEIGHT + PARAGRAPH_SPACING);

        text = "We are pleased to provide your Blood Pressure Report.  This report lists your blood pressure readings over the past 3 months.";
        float leading = getDefaultFontHeight() + 2;
        contents.setLeading(leading);
        contents.beginText();
        contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        List<String> lines = getLines(text, DEFAULT_FONT, DEFAULT_FONT_SIZE, pageWidth);
        for (String line : lines) {
            contents.showText(line.trim());
            if (lines.indexOf(line) < lines.size() - 1) {
                contents.newLine();
            }
        }
        contents.endText();

        currentY -= (lines.size() * leading + PARAGRAPH_SPACING);

        BloodPressureRecord average = report.getAverageBloodPressure();
        text = "Blood Pressure AVERAGE- " + average.getSystolic() + " / " + average.getDiastolic() + " DESIRED RANGE- " + report.getDesiredSystolic() + " / " + report.getDesiredDiastolic();
        contents.beginText();
        contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT_BOLD, 12);
        contents.showText(text);
        contents.endText();

        currentY -= DEFAULT_FONT_HEIGHT;

        createBloodPressureRecordsTable();

        text = "The shaded areas in the graph below show your desired blood pressure range, and the lines show that your blood pressure is, on average, #result# for the dates indicated in the chart above.";
        int result = report.getResult();
        if (result == 0) {
            text = text.replace("#result#", "within your desired range");
        } else if (result > 0) {
            text = text.replace("#result#", "outside of your desired range");
        } else {
            text = text.replace("#result#", "low");
        }
        contents.setLeading(leading);
        contents.beginText();
        contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        lines = getLines(text, DEFAULT_FONT, DEFAULT_FONT_SIZE, pageWidth);
        for (String line : lines) {
            contents.showText(line.trim());
            if (lines.indexOf(line) < lines.size() - 1) {
                contents.newLine();
            }
        }
        contents.endText();

        currentY -= (lines.size() * leading);

        createBloodPressureLineChart();

        if (result == 0) {
            text = "Great!  Your blood pressure is within the desired range.";
        } else if (result > 0) {
            text = "Your readings are high compared to your desired range. Make an appointment with your doctor to discuss your high blood pressure and develop a plan to control your blood pressure.";
        } else {
            text = "Your readings are low compared to your desired range. Make an appointment with your doctor to discuss your blood pressure.";
        }
        contents.setLeading(leading);
        contents.beginText();
        contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        lines = getLines(text, DEFAULT_FONT, DEFAULT_FONT_SIZE, pageWidth);
        for (String line : lines) {
            contents.showText(line.trim());
            if (lines.indexOf(line) < lines.size() - 1) {
                contents.newLine();
            }
        }
        contents.endText();

        currentY -= (lines.size() * leading + PARAGRAPH_SPACING);

        // Additional Comments
        text = "Additional Comments";
        contents.beginText();
        contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        contents.showText(text);
        contents.endText();

        float y = currentY - 3f;
        float textWidth = DEFAULT_FONT.getStringWidth(text) / 1000 * DEFAULT_FONT_SIZE;
        contents.moveTo(margin, y);
        contents.lineTo(margin + textWidth, y);
        contents.stroke();

        currentY -= (DEFAULT_FONT_HEIGHT + PARAGRAPH_SPACING + 3);

        return 0;
    }

    @Override
    float composeFooter() throws IOException {
        return 0;
    }

    private float createBloodPressureRecordsTable() throws IOException {
        BaseTable table = new BaseTable(currentY, pageHeight, 5, pageWidth, margin, document, page, true,
                true);
        //Create Header row
        Row<PDPage> headerRow = table.createRow(getDefaultFontHeight() + 2);
        float rowHeight = 10f;
        float cellWidth = 100 * 0.2f;
        String[] headers = new String[] {"Date", "Systolic Pressure(mmHg)", "Diastolic Pressure(mmHg)", "Pulse(beats/min)", "Source"};
        for (String header : headers) {
            Cell<PDPage> cell = headerRow.createCell(cellWidth, header);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            //cell.setFillColor(Color.LIGHT_GRAY);
        }
        table.addHeaderRow(headerRow);
        List<String[]> facts = getFacts();
        for (String[] fact : facts) {
            int rowIndex = facts.indexOf(fact);
            Row<PDPage> row = table.createRow(rowHeight);
            for (int i = 0; i < fact.length; i++) {
                Cell<PDPage> cell = row.createCell(cellWidth, fact[i]);
                if (rowIndex % 2 == 0 && rowIndex != facts.size() - 1) {
                    cell.setFillColor(Color.LIGHT_GRAY);
                }
                if (rowIndex == facts.size() - 1 && i == 0) {
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                }
            }
        }

        currentY = table.draw() - rowHeight - PARAGRAPH_SPACING;
        return currentY;
    }

    private List<String[]> getFacts() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String[]> facts = new ArrayList<String[]>();
        List<BloodPressureRecord> records = report.getBloodPressureRecords();
        for (BloodPressureRecord record : records) {
            String[] row = new String[5];
            row[0] = format.format(record.getDate());
            row[1] = record.getSystolic() + "";
            row[2] = record.getDiastolic() + "";
            row[3] = record.getPulse() + "";
            row[4] = record.getSource() == null ? "" : record.getSource();
            facts.add(row);
        }
        String[] row = new String[5];
        BloodPressureRecord average = report.getAverageBloodPressure();
        row[0] = "Average:";
        row[1] = average.getSystolic() + "";
        row[2] = average.getDiastolic() + "";
        row[3] = average.getPulse() + "";
        row[4] = "";
        facts.add(row);
        return facts;
    }

    private void createBloodPressureLineChart() throws IOException {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String systolicSeries = "Systolic";
        String diastolicSeries = "Diastolic";
        List<BloodPressureRecord> records = report.getBloodPressureRecords();
        for (BloodPressureRecord record : records) {
            dataset.addValue(record.getSystolic(), systolicSeries, format.format(record.getDate()));
            dataset.addValue(record.getDiastolic(), diastolicSeries, format.format(record.getDate()));
        }

        JFreeChart chart = ChartFactory.createLineChart(null, null, null, dataset);
        Font labelFont = new Font(Font.DIALOG, Font.PLAIN, 10);
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setItemFont(labelFont);
        legend.setBorder(0, 0, 0, 0);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setOutlineVisible(false);
        plot.setAxisOffset(RectangleInsets.ZERO_INSETS);

        plot.getDomainAxis().setTickLabelFont(labelFont);
        plot.getRangeAxis().setTickLabelFont(labelFont);
        CategoryAxis categoryAxis = plot.getDomainAxis();
        ValueAxis valueAxis = plot.getRangeAxis();

        final Marker low = new ValueMarker(80.0);
        low.setPaint(Color.BLACK);
        low.setStroke(new BasicStroke(2));
        plot.addRangeMarker(low);

        final Marker high = new ValueMarker(120.0);
        high.setPaint(Color.BLACK);
        high.setStroke(new BasicStroke(2));
        plot.addRangeMarker(high);

        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        int width = Float.valueOf(pageWidth).intValue() - 40;
        int height = 300;
        BufferedImage image = chart.createBufferedImage(width, height);
        PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
        float scale = 1f;
        float x = DEFAULT_MARGIN + 20;
        contents.drawImage(pdImage, x, currentY - height, pdImage.getWidth() * scale, pdImage.getHeight() * scale);

        contents.setStrokingColor(Color.black);
        contents.addRect(x - 5, currentY - height - 5, width + 10, height + 10);
        contents.closeAndStroke();

        currentY -= (height + 15 + PARAGRAPH_SPACING);
    }

}
