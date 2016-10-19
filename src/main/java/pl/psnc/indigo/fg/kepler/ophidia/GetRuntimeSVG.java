package pl.psnc.indigo.fg.kepler.ophidia;

import org.apache.commons.io.FileUtils;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.RuntimeData;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.FutureGatewayActor;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
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
import java.net.URI;

/**
 * Actor which reads runtime data of an Ophidia task and decodes the contents
 * of the SVG visualization of the runtime status.
 */
public class GetRuntimeSVG extends FutureGatewayActor {
    private static final String SVG = "svg";
    private static final String EMPTY_SVG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
            + "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"\n"
            + "   viewBox=\"0 0 500 500\" width=\"500\" height=\"500\">\n"
            + "</svg>";

    /**
     * Task id (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort idPort;
    /**
     * Path where the SVG will be saved (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort outputPathPort;

    public GetRuntimeSVG(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false);
        idPort.setTypeEquals(BaseType.STRING);

        outputPathPort = new TypedIOPort(this, "outputPath", true, false);
        outputPathPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);
        File outputFile = new File(
                PortHelper.readStringMandatory(outputPathPort));

        try {
            TasksAPI api = new TasksAPI(URI.create(getFutureGatewayUri()),
                                        getAuthorizationToken());
            Task task = api.getTask(id);

            for (final RuntimeData runtimeData : task.getRuntimeData()) {
                if (GetRuntimeSVG.SVG.equals(runtimeData.getName())) {
                    byte[] svgRaw = DatatypeConverter
                            .parseBase64Binary(runtimeData.getValue());
                    FileUtils.writeByteArrayToFile(outputFile, svgRaw);
                    output.broadcast(new BooleanToken(true));
                    return;
                }
            }

            FileUtils.write(outputFile, GetRuntimeSVG.EMPTY_SVG, "UTF-8");
            output.broadcast(new BooleanToken(false));
        } catch (FutureGatewayException | IOException e) {
            throw new IllegalActionException(this, e,
                                             "Failed to get runtime SVG");
        }
    }
}
