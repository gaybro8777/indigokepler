package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

@SuppressWarnings({ "WeakerAccess", "PublicField",
                    "ThisEscapedInObjectConstruction",
                    "ResultOfObjectAllocationIgnored", "unused" })
public class DeleteTask extends LimitedFiringSource {
    public TypedIOPort userPort;
    public TypedIOPort idPort;

    public DeleteTask(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        idPort = new TypedIOPort(this, "id", true, false);
        new SingletonAttribute(idPort, "_showName");
        idPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);

        try {
            TasksAPI api = new TasksAPI(RootAPI.LOCALHOST_ADDRESS);
            boolean isSuccess = api.deleteTask(id);
            output.broadcast(new BooleanToken(isSuccess));
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to delete task");
        }
    }
}
