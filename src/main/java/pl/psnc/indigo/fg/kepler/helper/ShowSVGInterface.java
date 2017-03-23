package pl.psnc.indigo.fg.kepler.helper;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

/**
 * An interface required to run {@link ShowSVG} actor in batch.
 */
public interface ShowSVGInterface {
    void initialize(TypedAtomicActor self);

    void show(Nameable self, String frameId, String imagePath)
            throws IllegalActionException;
}
