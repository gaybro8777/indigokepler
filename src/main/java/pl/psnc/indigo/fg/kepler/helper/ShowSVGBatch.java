package pl.psnc.indigo.fg.kepler.helper;

import au.edu.jcu.kepler.hydrant.ReplacementManager;
import au.edu.jcu.kepler.hydrant.ReplacementUtils;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

import java.util.HashMap;

/**
 * A batch implementation of the {@link ShowSVGInterface}.
 */
public class ShowSVGBatch implements ShowSVGInterface {
    private ReplacementManager replacementManager;

    @Override
    public void initialize(final TypedAtomicActor self) {
        replacementManager = ReplacementUtils.getReplacementManager(self);
    }

    @Override
    public void show(
            final Nameable self, final String frameId, final String imagePath)
            throws IllegalActionException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", String.format("%s-%s", self.getDisplayName(), frameId));
        map.put("type", "txt");
        map.put("append", true);
        map.put("output", "SVG displayed");
        replacementManager.writeData(map);
    }
}
