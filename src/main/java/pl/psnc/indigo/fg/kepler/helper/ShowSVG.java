package pl.psnc.indigo.fg.kepler.helper;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Objects;

import static java.lang.System.getProperty;

/**
 * Actor which displays an image.
 */
public class ShowSVG extends LimitedFiringSource {
    /** A title for the window. */
    private final TypedIOPort frameIdPort;
    /** Path to the SVG image. */
    private final TypedIOPort imagePathPort;

    private ShowSVGInterface implementation;

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
    public final void initialize() throws IllegalActionException {
        super.initialize();

        if (implementation == null) {
            if (Objects.equals("true", getProperty("java.awt.headless"))) {
                implementation = new ShowSVGBatch();
            } else {
                implementation = new ShowSVGWithGui();
            }
        }
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String frameId = PortHelper.readStringMandatory(frameIdPort);
        String imagePath = PortHelper.readStringMandatory(imagePathPort);
        implementation.show(this, frameId, imagePath);
        output.broadcast(new BooleanToken(true));
    }
}
