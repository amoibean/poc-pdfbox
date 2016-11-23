package com.pasi.pdfbox.util;

import com.pasi.pdfbox.bean.XYLocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.exceptions.WrappedIOException;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utilities to find text/object locations.
 */
public class LocationFinder {
    private static final Log LOG = LogFactory.getLog(LocationFinder.class);

    /**
     * Find and return the image location coordinate.
     */
    public static XYLocation findImageLocation(PDPage page, PDXObjectImage image) throws IOException {
        ImageLocationFinder finder = new ImageLocationFinder(image);
        finder.processStream(page, page.findResources(), page.getContents().getStream());
        return finder.getLocation();
    }

    static class ImageLocationFinder extends PDFStreamEngine {
        private PDXObjectImage image;
        private XYLocation location;

        public ImageLocationFinder(PDXObjectImage image) throws IOException {
            super(ResourceLoader.loadProperties("org/apache/pdfbox/resources/PDFTextStripper.properties", true));
            this.image = image;
        }

        /**
         * This is used to handle an operation.
         *
         * @param operator  The operation to perform.
         * @param arguments The list of arguments.
         * @throws IOException If there is an error processing the operation.
         */
        protected void processOperator(PDFOperator operator, List arguments) throws IOException {
            String operation = operator.getOperation();
            if (operation.equals("Do")) {
                COSName objectName = (COSName) arguments.get(0);
                Map xObjects = getResources().getXObjects();
                PDXObject xObject = (PDXObject) xObjects.get(objectName.getName());
                if (xObject instanceof PDXObjectImage) {
                    if (xObject.equals(image)) {
                        try {
                            PDPage page = getCurrentPage();
                            Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
                            double rotationInRadians = (page.findRotation() * Math.PI) / 180;

                            AffineTransform rotation = new AffineTransform();
                            rotation.setToRotation(rotationInRadians);
                            AffineTransform rotationInverse = rotation.createInverse();
                            Matrix rotationInverseMatrix = new Matrix();
                            rotationInverseMatrix.setFromAffineTransform(rotationInverse);
                            Matrix rotationMatrix = new Matrix();
                            rotationMatrix.setFromAffineTransform(rotation);

                            Matrix unrotatedCTM = ctm.multiply(rotationInverseMatrix);
                            float xScale = unrotatedCTM.getXScale();
                            float yScale = unrotatedCTM.getYScale();

                            location = new XYLocation(unrotatedCTM.getXPosition(), unrotatedCTM.getYPosition());

                            LOG.info("Found image[" + objectName.getName() + "] " +
                                    "at " + unrotatedCTM.getXPosition() + "," + unrotatedCTM.getYPosition() +
                                    " size=" + (xScale / 100f * image.getWidth()) + "," + (yScale / 100f * image.getHeight()));
                        } catch (NoninvertibleTransformException e) {
                            throw new WrappedIOException(e);
                        }
                    }
                }
            } else {
                super.processOperator(operator, arguments);
            }
        }

        public XYLocation getLocation() {
            return this.location;
        }
    }
}
