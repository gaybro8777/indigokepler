package pl.psnc.indigo.fg.kepler;

import org.json.JSONArray;
import org.json.JSONObject;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.SingletonAttribute;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;

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

    TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);

    try {
      Task taskToGet = new Task();
      taskToGet.setUser(userString);
      Task[] result = restAPI.getAllTasks();

      JSONArray array = new JSONArray();

      for (Task task : result) {
        JSONObject object = new JSONObject();
        object.put(task.getId(), task.getStatus());
        array.put(object);
      }

      String jsonString = array.toString();

      output.send(0, new StringToken(jsonString));
    } catch (Exception ex) {
      throw new IllegalActionException("There was an issue during task submission");
    }
  }
}
