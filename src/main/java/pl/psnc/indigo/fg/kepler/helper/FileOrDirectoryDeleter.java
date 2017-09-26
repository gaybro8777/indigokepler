package pl.psnc.indigo.fg.kepler.helper;

import org.apache.commons.io.FileUtils;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.File;
import java.io.IOException;

/**
 * A Kepler actor which deletes a file or directory (recursively).
 */
public class FileOrDirectoryDeleter extends LimitedFiringSource {
    private final TypedIOPort pathPort;

    public FileOrDirectoryDeleter(final CompositeEntity container,
                                  final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        pathPort = new TypedIOPort(this, "path", true, false);
        pathPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);

        PortHelper.makePortNameVisible(pathPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        final String path = PortHelper.readStringMandatory(pathPort);

        try {
            final File file = new File(path);
            FileUtils.forceDelete(file);
            output.broadcast(new BooleanToken(true));
        } catch (final IOException e) {
            throw new IllegalActionException(this, e, Messages.format(
                    "failed.to.delete.file.or.directory.0", path));
        }
    }
}
