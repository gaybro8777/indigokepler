package pl.psnc.indigo.fg.kepler.helper;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Actor which displays an image.
 */
public class ShowSVG extends LimitedFiringSource {
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
            SVGDocument document = SVGFrame.fromFile(image);
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
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
                    XMLResourceDescriptor.getXMLParserClassName());

            URI uri = file.toURI();
            String uriString = uri.toString();
            return (SVGDocument) factory.createDocument(uriString);
        }
    }

    /** A title for the window. */
    private final TypedIOPort frameIdPort;
    /** Path to the SVG image. */
    private final TypedIOPort imagePathPort;

    private final Map<String, SVGFrame> mapIdFrame = new HashMap<>();

    public ShowSVG(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        frameIdPort = new TypedIOPort(this, "frameId", true, false); //NON-NLS
        frameIdPort.setTypeEquals(BaseType.STRING);

        imagePathPort =
                new TypedIOPort(this, "imagePath", true, false); //NON-NLS
        imagePathPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);

        PortHelper.makePortNameVisible(frameIdPort, imagePathPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String frameId = PortHelper.readStringMandatory(frameIdPort);
        String imagePath = PortHelper.readStringMandatory(imagePathPort);

        if (!mapIdFrame.containsKey(frameId)) {
            mapIdFrame.put(frameId, new SVGFrame(frameId));
        }

        try {
            File image = new File(imagePath);
            mapIdFrame.get(frameId).setImage(image);
            output.broadcast(new BooleanToken(true));
        } catch (final IOException e) {
            throw new IllegalActionException(this, e, Messages.getString(
                    "failed.to.show.svg.image"));
        }
    }
}
