package pl.psnc.indigo.fg.kepler.helper;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A graphical implementation of the {@link ShowSVGInterface}.
 */
public class ShowSVGWithGui implements ShowSVGInterface {
    /**
     * An extension of {@link JFrame} which displays and scales inside an SVG
     * image.
     */
    private static final class SVGFrame extends JFrame {
        private static final long serialVersionUID = 5831307351055768729L;

        private final JSVGCanvas canvas;
        private final JPanel panel;

        private SVGFrame(final String frameId) {
            super("ShowSVG - " + frameId); //NON-NLS
            canvas = new JSVGCanvas();
            panel = new JPanel(new BorderLayout());
            panel.add(canvas, BorderLayout.CENTER);
            getContentPane().add(panel);
        }

        private void setImage(final File image) throws IOException {
            final SVGDocument document = SVGFrame.fromFile(image);
            canvas.setSVGDocument(document);

            if (!isVisible()) {
                pack();
                setVisible(true);
            }
        }

        /**
         * Load SVG image from file.
         *
         * @param file A path to an SVG image.
         * @return A parsed {@link SVGDocument} object.
         * @throws IOException If the file parsing does not work.
         */
        private static SVGDocument fromFile(final File file)
                throws IOException {
            final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
                    XMLResourceDescriptor.getXMLParserClassName());

            final URI uri = file.toURI();
            final String uriString = uri.toString();
            return (SVGDocument) factory.createDocument(uriString);
        }
    }

    private final Map<String, SVGFrame> mapIdFrame = new HashMap<>();

    @Override
    public final void show(final Nameable self, final String frameId,
                           final String imagePath)
            throws IllegalActionException {
        if (!mapIdFrame.containsKey(frameId)) {
            mapIdFrame.put(frameId, new SVGFrame(frameId));
        }

        try {
            final File image = new File(imagePath);
            mapIdFrame.get(frameId).setImage(image);
        } catch (final IOException e) {
            throw new IllegalActionException(self, e, Messages.getString(
                    "failed.to.show.svg.image"));
        }
    }
}
