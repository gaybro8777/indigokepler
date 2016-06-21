package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

@SuppressWarnings({ "WeakerAccess", "PublicField",
                    "ThisEscapedInObjectConstruction",
                    "ResultOfObjectAllocationIgnored", "unused" })
public class GetTask extends LimitedFiringSource {
    public TypedIOPort userPort;
    public TypedIOPort idPort;
    public TypedIOPort statusPort;

    public GetTask(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        idPort = new TypedIOPort(this, "id", true, false);
        new SingletonAttribute(idPort, "_showName");
        idPort.setTypeEquals(BaseType.STRING);

        statusPort = new TypedIOPort(this, "status", false, true);
        new SingletonAttribute(statusPort, "_showName");
        statusPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.STRING);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);

        try {
            TasksAPI restAPI = new TasksAPI(RootAPI.LOCALHOST_ADDRESS);
            Task task = restAPI.getTask(id);
            String status = task.getStatus().name();

            output.send(0, new StringToken(id));
            statusPort.send(0, new StringToken(status));
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to get task");
        }
    }
}
