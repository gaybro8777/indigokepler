package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

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
    public void fire() throws IllegalActionException {
        super.fire();

        String userString = null;
        String idString = null;

        if (userPort.getWidth() > 0) {
            StringToken userToken = (StringToken) userPort.get(0);
            userString = userToken.stringValue();
        }

        if (idPort.getWidth() > 0) {
            StringToken idToken = (StringToken) idPort.get(0);
            idString = idToken.stringValue();
        }

        Task taskToGet = new Task();
        taskToGet.setUser(userString);
        taskToGet.setId(idString);

        Task result = null;
        try {
            TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);
            result = restAPI.getTask(taskToGet);
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to get task");
        }

        output.send(0, new StringToken(result.getId()));
        statusPort.send(0, new StringToken(result.getStatus().name()));
    }
}
