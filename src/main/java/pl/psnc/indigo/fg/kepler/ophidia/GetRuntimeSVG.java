package pl.psnc.indigo.fg.kepler.ophidia;

import org.apache.commons.io.FileUtils;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.RuntimeData;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.FutureGatewayActor;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

/**
 * Actor which reads runtime data of an Ophidia task and decodes the contents
 * of the SVG visualization of the runtime status.
 */
public class GetRuntimeSVG extends FutureGatewayActor {
    private static final String SVG = "svg";

    /**
     * Task id (mandatory).
     */
    private final TypedIOPort idPort;
    /**
     * Path where the SVG will be saved (mandatory).
     */
    private final TypedIOPort outputPathPort;

    public GetRuntimeSVG(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false); //NON-NLS
        idPort.setTypeEquals(BaseType.STRING);

        outputPathPort =
                new TypedIOPort(this, "outputPath", true, false); //NON-NLS
        outputPathPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);

        PortHelper.makePortNameVisible(idPort, outputPathPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        final String id = PortHelper.readStringMandatory(idPort);
        final File outputFile =
                new File(PortHelper.readStringMandatory(outputPathPort));

        try {
            final String uri = getFutureGatewayUri();
            final String token = getAuthorizationToken();
            final TasksAPI api = new TasksAPI(URI.create(uri), token);
            final Task task = api.getTask(id);

            for (final RuntimeData runtimeData : task.getRuntimeData()) {
                if (Objects.equals(GetRuntimeSVG.SVG, runtimeData.getName())) {
                    final byte[] svgRaw = DatatypeConverter
                            .parseBase64Binary(runtimeData.getValue());
                    FileUtils.writeByteArrayToFile(outputFile, svgRaw);
                    output.broadcast(new BooleanToken(true));
                    return;
                }
            }

            final InputStream stream = GetRuntimeSVG.class.getClassLoader()
                                                          .getResourceAsStream(
                                                                  "empty.svg");
            //NON-NLS
            FileUtils.copyInputStreamToFile(stream, outputFile);
            output.broadcast(new BooleanToken(false));
        } catch (FutureGatewayException | IOException e) {
            final String message =
                    Messages.getString("failed.to.get.runtime.svg");
            throw new IllegalActionException(this, e, message);
        }
    }
}
