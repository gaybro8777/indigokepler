package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
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

import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

public class GetOutputsList extends LimitedFiringSource {
    private final static Logger LOGGER = Logger.getLogger(GetOutputsList.class.getName());

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
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readString(userPort);
        String id = PortHelper.readString(idPort);

        Task taskToGet = new Task();
        taskToGet.setUser(user);
        taskToGet.setId(id);

        try {
            TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);

            List<OutputFile> files = restAPI.getOutputsForTask(taskToGet);
            RecordToken[] tokens = new RecordToken[files.size()];

            for (int i = 0; i < files.size(); i++) {
                StringToken[] file = new StringToken[2];
                file[0] = new StringToken(files.get(i).getUrl());
                file[1] = new StringToken(files.get(i).getName());
                tokens[i] = new RecordToken(new String[]{"url", "name"}, file);
            }

            output.broadcast(new ArrayToken(tokens));
        } catch (FutureGatewayException | URISyntaxException e) {
            throw new IllegalActionException(this, e, "Failed to get output list");
        }
    }
}
