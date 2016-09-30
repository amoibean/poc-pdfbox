package com.pasi.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bean on 9/27/16.
 *
 * http://svn.apache.org/repos/asf/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/AddMessageToEachPage.java
 */
public abstract class DocWriter {
    public static final PDFont DEFAULT_FONT = PDType1Font.HELVETICA;
    public static final PDFont DEFAULT_FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    public static final float DEFAULT_WIDTH = PDRectangle.A4.getWidth();
    public static final float DEFAULT_FONT_SIZE = 10F;
    public static final float DEFAULT_MARGIN = 20;
    public static final float PARAGRAPH_SPACING = 5F;
    public static final float DEFAULT_FONT_HEIGHT = DEFAULT_FONT.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * DEFAULT_FONT_SIZE;
    protected float margin = DEFAULT_MARGIN;
    protected float headerHeight = 40;
    protected float footerHeight = 40;
    protected float pageWidth = 500;
    protected float pageHeight = 500;
    protected float currentX = 0; // the current X position
    protected float currentY = 0; // the current Y position

    protected PDDocument document;
    protected PDPage page;
    protected PDPageContentStream contents;

    protected void addNewPage() {
        try {
            if (contents != null) {
                contents.close();
            }
            page = new PDPage(PDRectangle.A4);
            pageWidth = page.getMediaBox().getWidth() - margin * 2;
            pageHeight = page.getMediaBox().getHeight() - headerHeight - footerHeight;
            currentY = pageHeight;
            contents = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
            document.addPage(page);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create page content stream");
        }
    }

    protected void checkPageHeight(float min) {
        if (currentY < min) {
            addNewPage();
        }
    }

    protected float getStringWidth(PDFont font, String text) {
        return font.getFontDescriptor().getFontBoundingBox().getWidth() / 1000 * text.length();
    }

    protected float getDefaultFontHeight() {
        return DEFAULT_FONT.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * DEFAULT_FONT_SIZE;
    }

    protected float getFontHeight(PDFont font, float fontSize) {
        return font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    }

    public List<String> getLines(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> result = new LinkedList<String>();
        String[] split = text.split("(?<=\\W)");
        int[] possibleWrapPoints = new int[split.length];
        possibleWrapPoints[0] = split[0].length();
        for (int i = 1; i < split.length; i++) {
            possibleWrapPoints[i] = possibleWrapPoints[i - 1] + split[i].length();
        }
        int start = 0;
        int end = 0;
        for (int i : possibleWrapPoints) {
            float width = font.getStringWidth(text.substring(start, i)) / 1000 * fontSize;
            if (start < end && width > maxWidth) {
                result.add(text.substring(start, end));
                start = end;
            }
            end = i;
        }
        result.add(text.substring(start));
        return result;
    }

    abstract float composeHeader() throws IOException;
    abstract float composeBody() throws IOException;
    abstract float composeFooter() throws IOException;

    public void write(String file) throws IOException {
        composeHeader();
        composeBody();
        composeFooter();
        contents.close();
        document.save(file);
        document.close();
    }

    public void write(String file, String ownerPassword, String userPassword) throws IOException {
        composeHeader();
        composeBody();
        composeFooter();
        int keyLength = 128;

        AccessPermission ap = new AccessPermission();

        // disable some permission
        ap.setCanPrint(false);
        ap.setCanModify(false);
        ap.setCanExtractContent(false);

        // owner password (to open the file with all permissions) is "12345"
        // user password (to open the file but with restricted permissions)
        StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPassword, userPassword, ap);
        spp.setEncryptionKeyLength(keyLength);
        spp.setPermissions(ap);
        document.protect(spp);

        contents.close();
        document.save(file);
        document.close();
    }

    public float getMargin() {
        return margin;
    }

    public void setMargin(float margin) {
        this.margin = margin;
    }
}
