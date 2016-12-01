package com.pasi.pdfbox;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import com.pasi.pdfbox.bean.BloodPressureRecord;
import com.pasi.pdfbox.bean.PatientBloodPressureReport;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    float composeBody() throws IOException {
        String text = "Dear " + report.getPatientName() + ",";
        //float textWidth = getStringWidth(DEFAULT_FONT, text);
        //System.out.println("text width = " + textWidth);
        contents.beginText();
        contents.moveTextPositionByAmount(margin, currentY);
        //contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        contents.drawString(text);
        //contents.showText(text);
        contents.endText();

        currentY -= (DEFAULT_FONT_HEIGHT + PARAGRAPH_SPACING);

        text = "We are pleased to provide your Blood Pressure Report.  This report lists your blood pressure readings over the past 3 months.";
        float leading = getDefaultFontHeight() * 1.2f;
        //contents.setLeading(leading);
        contents.beginText();
        contents.moveTextPositionByAmount(margin, currentY);
        //contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        List<String> lines = getLines(text, DEFAULT_FONT, DEFAULT_FONT_SIZE, pageWidth);
        for (String line : lines) {
            contents.drawString(line.trim());
            //contents.showText(line.trim());
            if (lines.indexOf(line) < lines.size() - 1) {
                contents.moveTextPositionByAmount(0, -leading);
                //contents.newLine();
            }
        }
        contents.endText();

        currentY -= (lines.size() * leading + PARAGRAPH_SPACING);

        BloodPressureRecord average = report.getAverageBloodPressure();
        text = "Blood Pressure AVERAGE- " + average.getSystolic() + " / " + average.getDiastolic() + " DESIRED RANGE- " + report.getDesiredSystolic() + " / " + report.getDesiredDiastolic();
        contents.beginText();
        contents.moveTextPositionByAmount(margin, currentY);
        //contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT_BOLD, 11);
        contents.drawString(text);
        //contents.showText(text);
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
        //contents.setLeading(leading);
        contents.beginText();
        contents.moveTextPositionByAmount(margin, currentY);
        //contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        lines = getLines(text, DEFAULT_FONT, DEFAULT_FONT_SIZE, pageWidth);
        for (String line : lines) {
            contents.drawString(line.trim());
            //contents.showText(line.trim());
            if (lines.indexOf(line) < lines.size() - 1) {
                contents.moveTextPositionByAmount(0, -leading);
                //contents.newLine();
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
        //contents.setLeading(leading);
        contents.beginText();
        contents.moveTextPositionByAmount(margin, currentY);
        //contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        lines = getLines(text, DEFAULT_FONT, DEFAULT_FONT_SIZE, pageWidth);
        for (String line : lines) {
            contents.drawString(line.trim());
            //contents.showText(line.trim());
            if (lines.indexOf(line) < lines.size() - 1) {
                contents.moveTextPositionByAmount(0, -leading);
                //contents.newLine();
            }
        }
        contents.endText();

        currentY -= (lines.size() * leading + PARAGRAPH_SPACING);

        // Additional Comments
        text = "Additional Comments";
        contents.beginText();
        contents.moveTextPositionByAmount(margin, currentY);
        //contents.newLineAtOffset(margin, currentY);
        contents.setFont(DEFAULT_FONT, DEFAULT_FONT_SIZE);
        contents.drawString(text);
        //contents.showText(text);
        contents.endText();

        float y = currentY - 3f;
        float textWidth = DEFAULT_FONT.getStringWidth(text) / 1000 * DEFAULT_FONT_SIZE;
        contents.moveTo(margin, y);
        contents.lineTo(margin + textWidth, y);
        contents.stroke();

        currentY -= (DEFAULT_FONT_HEIGHT + PARAGRAPH_SPACING + 3);
        // add more contents for page 1 here

        // Merge page 2
        PDFMergerUtility merger = new PDFMergerUtility();
        PDDocument page2 = preprocessPage2();
        merger.appendDocument(document, page2);
        page2.close();
        contents.close();
        return 0;
    }

    /**
     * Pre-process page2 and prepare it to be merged.
     *
     * @return the processed page2
     */
    private PDDocument preprocessPage2() throws IOException {
        PDDocument page2 = PDDocument.load(getClass().getClassLoader().getResourceAsStream("bp-patient-education.pdf"));
        PDDocumentCatalog catalog = page2.getDocumentCatalog();
        List pages = catalog.getAllPages();
        for (Object pg : pages) { // process all pages so that logo images in all pages are replaced
            PDPage page = (PDPage) pg;
            PDResources resources = page.findResources();
            Map<String, PDXObject> xObjects = resources.getXObjects();
            COSDictionary dict = resources.getCOSDictionary();
            PDPageContentStream contentStream = new PDPageContentStream(page2, page, true, true, false);
            for (Map.Entry<String, PDXObject> entry : xObjects.entrySet()) {
                String key = entry.getKey();
                PDXObject object = entry.getValue();
                if (object instanceof PDXObjectImage) {
                    /**
                     * At first, we tried to remember the logo image position and then remove it from
                     * resources, and use drawImage to draw the new image at the logo image location
                     * in (140 x 32) dimension. But simply removing the xobject from resources is not
                     * enough, there are still references to the original logo image in the content
                     * stream which are hard to find and delete. In order to keep the references so
                     * that the PDF document is not massed up, we keep the reference name and replace
                     * the COSObject of the original logo image with the new one. This however, will
                     * make the new logo image to appear at the same location and the same size with
                     * the original one. So the placeholder logo image has to be placed in the right
                     * position and needs to be the desired size(140 x 32 for example)
                     */
                    //By replacing the original image, we no longer need to know its location
                    //XYLocation logoLocation = LocationFinder.findImageLocation(page, (PDXObjectImage) object);

                    /**
                     * removing xobject does not remove all its references from content stream which triggers
                     * the error prompt in Adobe Acrobat
                     */
                    //xObjects.remove(key);
                    //((COSDictionary) dict.getDictionaryObject(COSName.XOBJECT)).removeItem(COSName.getPDFName(key));
                    BufferedImage image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("pharmacy-logo.jpg"));
                    PDXObjectImage logo = new PDJpeg(page2, image);
                    // since we can't safely delete the original one, we can't insert the new one
                    //contentStream.drawXObject(logo, logoLocation.getX(), logoLocation.getY(), 140f, 32f);

                    // replace the original image COSObject with the new one
                    ((COSDictionary) dict.getDictionaryObject(COSName.XOBJECT)).setItem(COSName.getPDFName(key), logo.getCOSObject());
                    // this will work too, but the replaceWithStream is deprecated
                    //object.getCOSStream().replaceWithStream(logo.getCOSStream());
                    break; // assuming the first image is the logo image!
                }
            }
            contentStream.close();
        }
        return page2;
    }

    /**
     * Add logo images to header of each page;
     * Add page number as well as date at the footer of each page
     *
     * @return 0
     * @throws IOException
     */
    @Override
    float composeHeaderFooter() throws IOException {
        List allPages = document.getDocumentCatalog().getAllPages();
        PDFont font = DEFAULT_FONT;
        float headerFontSize = 18.0f;
        float footerFontSize = DEFAULT_FONT_SIZE;
        String date = new SimpleDateFormat("MMM d, yyyy").format(new Date());
        float dateWidth = font.getStringWidth(date) * footerFontSize / 1000f;
        for(int i = 0; i < allPages.size(); i++) {
            PDPage page = (PDPage)allPages.get( i );
            String pageNumber = "Page " + (i + 1) + " of " + allPages.size();
            PDPageContentStream contentStream = new PDPageContentStream(document, page, true, true, true);
            // compose header
            float headerY = footerHeight + pageHeight;
            String headerText = "Patient Blood Pressure Report";
            if (i == 0) {
                contentStream.beginText();
                contentStream.moveTextPositionByAmount(margin, headerY);
                contentStream.setFont(font, headerFontSize);
                contentStream.drawString(headerText);
                contentStream.endText();
                // insert logo image
                BufferedImage image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("pharmacy-logo.jpg"));
                PDXObjectImage logo = new PDJpeg(document, image);
                contentStream.drawXObject(logo, margin + pageWidth - 140, headerY - 16, 140f, 32f);
            }

            // compose footer
            float footerY = footerHeight - DEFAULT_MARGIN + footerFontSize;
            contentStream.beginText();
            contentStream.moveTextPositionByAmount(margin, footerY);
            // set font and font size
            contentStream.setFont(font, footerFontSize);
            contentStream.drawString(pageNumber);
            contentStream.endText();

            contentStream.beginText();
            contentStream.moveTextPositionByAmount(margin + pageWidth - dateWidth, footerY);
            contentStream.drawString(date);
            contentStream.endText();

            contentStream.close();
        }
        return 0;
    }

    private float createBloodPressureRecordsTable() throws IOException {
        BaseTable table = new BaseTable(currentY, pageHeight, 5, pageWidth-6f, margin, document, page, true, true);
        //Create Header row
        Row headerRow = table.createRow(getDefaultFontHeight() + 2);
        float rowHeight = 12f;
        float cellWidth = 100 * 0.2f;
        String[] headers = new String[] {"Date", "Systolic Pressure(mmHg)", "Diastolic Pressure(mmHg)", "Pulse(beats/min)", "Source"};
        for (String header : headers) {
            Cell cell = headerRow.createCell(cellWidth, header);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            //cell.setFillColor(Color.LIGHT_GRAY);
        }
        //table.addHeaderRow(headerRow);
        List<String[]> facts = getFacts();
        for (String[] fact : facts) {
            int rowIndex = facts.indexOf(fact);
            Row row = table.createRow(rowHeight);
            for (int i = 0; i < fact.length; i++) {
                Cell cell = row.createCell(cellWidth, fact[i]);
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
        Font labelFont = new Font(Font.DIALOG, Font.PLAIN, 20);
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
        BufferedImage image = chart.createBufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB, null);

        PDXObjectImage pdImage = new PDPixelMap(document, image);

        //PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
        //float scale = 1f;
        float x = DEFAULT_MARGIN + 20;
        contents.drawXObject(pdImage, x, currentY - height, width, height);
        //contents.drawImage(pdImage, x, currentY - height);

        contents.setStrokingColor(Color.black);
        contents.addRect(x - 5, currentY - height - 5, width + 10, height + 10);
        contents.closeAndStroke();

        currentY -= (height + 15 + PARAGRAPH_SPACING);
    }

}
