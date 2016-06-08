package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URISyntaxException;

public class DeleteTask extends LimitedFiringSource {
    public TypedIOPort userPort;
    public TypedIOPort idPort;

    public DeleteTask(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        userPort.setTypeEquals(BaseType.STRING);
        idPort = new TypedIOPort(this, "id", true, false);
        idPort.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readString(userPort);
        String id = PortHelper.readString(idPort);

        Task task = new Task();
        task.setUser(user);
        task.setId(id);

        try {
            TasksAPI api = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);
            boolean isSuccess = api.deleteTask(task);
            output.broadcast(new BooleanToken(isSuccess));
        } catch (FutureGatewayException | URISyntaxException e) {
            throw new IllegalActionException(this, e, "Failed to delete task");
        }
    }
}
