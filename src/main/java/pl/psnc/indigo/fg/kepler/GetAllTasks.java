package pl.psnc.indigo.fg.kepler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import java.util.List;

public class GetAllTasks extends LimitedFiringSource {

    public TypedIOPort userPort;

    public GetAllTasks(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.STRING);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        String userString = null;

        if (userPort.getWidth() > 0) {
            StringToken userToken = (StringToken) userPort.get(0);
            userString = userToken.stringValue();
        }

        Task taskToGet = new Task();
        taskToGet.setUser(userString);

        JSONArray array;
        try {
            TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);
            List<Task> result = restAPI.getAllTasks();

            array = new JSONArray();

            for (Task task : result) {
                JSONObject object = new JSONObject();
                object.put(task.getId(), task.getStatus());
                array.put(object);
            }
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to get all tasks");
        } catch (JSONException e) {
            throw new IllegalActionException(this, e, "Failed to get all tasks");
        }

        output.send(0, new StringToken(array.toString()));
    }
}
