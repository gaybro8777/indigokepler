package pl.psnc.indigo.fg.kepler.helper;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

/**
 * An interface required to run {@link ShowSVG} actor in batch.
 */
@FunctionalInterface
public interface ShowSVGInterface {
    /**
     * This method is responsible for showing an SVG image if Kepler is run in
     * GUI mode or silently ignore the image altogeher if non-GUI mode is on.
     *
     * @param self      Reference to Kepler actor.
     * @param frameId   Name of the window where SVG is drawn. Repeated calls to
     *                  {@link ShowSVGInterface#show(Nameable, String, String)}
     *                  with the same frameId will result in the image being
     *                  redrawn instead of popping new windows.
     * @param imagePath Path where the image is stored.
     * @throws IllegalActionException If anything wrong happens during Kepler
     *                                actor processing.
     */
    void show(Nameable self, String frameId, String imagePath)
            throws IllegalActionException;
}
