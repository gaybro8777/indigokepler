package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.net.URI;
import java.util.List;

/**
 * Actor which retrieves task's output files and sends them in a
 * {@link RecordToken}. See {@link TasksAPI#getOutputsForTask(String)}.
 */
@SuppressWarnings({"WeakerAccess", "PublicField",
                   "ThisEscapedInObjectConstruction",
                   "ResultOfObjectAllocationIgnored", "unused"})
public class GetOutputsList extends FutureGatewayActor {
    private static final String[] LABELS = {"url", "name"};
    private static final Type[] TYPES = {BaseType.STRING, BaseType.STRING};
    /**
     * A {@link Type} of token produced by this actor.
     */
    public static final Type OUTPUT_FILE_TYPE = new RecordType(
            GetOutputsList.LABELS, GetOutputsList.TYPES);

    /**
     * Task id (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort idPort;

    public GetOutputsList(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false);
        new SingletonAttribute(idPort, "_showName");
        idPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(new ArrayType(GetOutputsList.OUTPUT_FILE_TYPE));
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);

        try {
            TasksAPI restAPI = new TasksAPI(URI.create(getFutureGatewayUri()));

            List<OutputFile> files = restAPI.getOutputsForTask(id);
            RecordToken[] tokens = new RecordToken[files.size()];

            for (int i = 0; i < files.size(); i++) {
                String name = files.get(i).getName();
                String url = files.get(i).getUrl().toString();

                StringToken[] file = new StringToken[2];
                file[0] = new StringToken(url);
                file[1] = new StringToken(name);
                tokens[i] = new RecordToken(GetOutputsList.LABELS, file);
            }

            output.broadcast(
                    new ArrayToken(GetOutputsList.OUTPUT_FILE_TYPE, tokens));
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e,
                                             "Failed to get output list");
        }
    }
}
