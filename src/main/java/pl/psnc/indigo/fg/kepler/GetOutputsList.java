package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings({ "WeakerAccess", "PublicField",
                    "ThisEscapedInObjectConstruction",
                    "ResultOfObjectAllocationIgnored", "unused" })
public class GetOutputsList extends LimitedFiringSource {
    private static final Logger LOGGER = Logger
            .getLogger(GetOutputsList.class.getName());

    public TypedIOPort userPort;
    public TypedIOPort idPort;

    public GetOutputsList(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        idPort = new TypedIOPort(this, "id", true, false);
        new SingletonAttribute(idPort, "_showName");
        idPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.GENERAL);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);

        try {
            TasksAPI restAPI = new TasksAPI(RootAPI.LOCALHOST_ADDRESS);

            List<OutputFile> files = restAPI.getOutputsForTask(id);
            RecordToken[] tokens = new RecordToken[files.size()];

            for (int i = 0; i < files.size(); i++) {
                String name = files.get(i).getName();
                String url = files.get(i).getUrl().toString();

                StringToken[] file = new StringToken[2];
                file[0] = new StringToken(url);
                file[1] = new StringToken(name);
                tokens[i] = new RecordToken(new String[] { "url", "name" },
                                            file);
            }

            output.broadcast(new ArrayToken(tokens));
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e,
                                             "Failed to get output " + "list");
        }
    }
}
