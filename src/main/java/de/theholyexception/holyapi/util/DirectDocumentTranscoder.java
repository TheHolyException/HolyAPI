package de.theholyexception.holyapi.util;

import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class DirectDocumentTranscoder extends SVGAbstractTranscoder {

    /**
     * Transcode a Document via batik to a BufferedImage
     * @param document document to convert
     * @param w image width
     * @param h image height
     * @return rendered Document
     * @throws TranscoderException on error... i guess
     * @throws NoninvertibleTransformException other error... :/
     */
    public BufferedImage transcode(Document document, int w, int h) throws TranscoderException, NoninvertibleTransformException {
        addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, (float)w);
        addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, (float)h);

        super.transcode(document, null, null);

        ImageRenderer renderer = new ConcreteImageRendererFactory().createStaticImageRenderer();
        renderer.updateOffScreen(w, h);

        renderer.setTransform(curTxf);
        renderer.setTree(this.root);
        this.root = null; // We're done with it...

        // now we are sure that the aoi is the image size
        Shape raoi = new Rectangle2D.Float(0, 0,w,h);
        // Warning: the renderer's AOI must be in user space
        renderer.repaint(curTxf.createInverse()
                .createTransformedShape(raoi));
        BufferedImage image = renderer.getOffScreen();

        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = GraphicsUtil.createGraphics(dest);
        g2d.drawRenderedImage(image, new AffineTransform());
        g2d.dispose();
        image = dest;
        return image;
    }
}