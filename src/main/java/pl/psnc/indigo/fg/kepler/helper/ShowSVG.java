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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Actor which displays an image.
 */
public class ShowSVG extends LimitedFiringSource {
    private static class SvgFrame extends JFrame {
        private final JSVGCanvas canvas;
        private final JPanel panel;

        private SvgFrame(final String frameId) {
            super("ShowSVG - " + frameId);
            canvas = new JSVGCanvas();
            panel = new JPanel(new BorderLayout());
            panel.add(canvas, BorderLayout.CENTER);
            getContentPane().add(panel);
        }

        public void setImage(File image) throws IOException {
            SVGDocument document = SvgFrame.fromFile(image);
            canvas.setSVGDocument(document);

            if (!isVisible()) {
                pack();
                setVisible(true);
            }
        }

        private static SVGDocument fromFile(final File file)
                throws IOException {
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
                    XMLResourceDescriptor.getXMLParserClassName());
            return (SVGDocument) factory
                    .createDocument("file://" + file.getAbsolutePath());
        }
    }

    @AllowedPublicField
    public TypedIOPort frameIdPort;
    @AllowedPublicField
    public TypedIOPort imagePathPort;

    private final Map<String, SvgFrame> mapIdFrame = new HashMap<>();

    public ShowSVG(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        frameIdPort = new TypedIOPort(this, "frameId", true, false);
        frameIdPort.setTypeEquals(BaseType.STRING);
        imagePathPort = new TypedIOPort(this, "imagePath", true, false);
        imagePathPort.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String frameId = PortHelper.readStringMandatory(frameIdPort);
        String imagePath = PortHelper.readStringMandatory(imagePathPort);
        File image = new File(imagePath);

        if (!mapIdFrame.containsKey(frameId)) {
            mapIdFrame.put(frameId, new SvgFrame(frameId));
        }

        try {
            mapIdFrame.get(frameId).setImage(image);
            output.broadcast(new BooleanToken(true));
        } catch (IOException e) {
            throw new IllegalActionException(this, e,
                                             "Failed to show SVG image");
        }
    }
}
