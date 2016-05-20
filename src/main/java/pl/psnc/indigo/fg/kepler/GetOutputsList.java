package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
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

public class GetOutputsList extends LimitedFiringSource {

    private final static Logger LOGGER = Logger.getLogger(GetOutputsList.class.getName());

    public TypedIOPort userPort;
    public TypedIOPort idPort;
    public TypedIOPort outputsPort;

    public GetOutputsList(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        idPort = new TypedIOPort(this, "id", true, false);
        new SingletonAttribute(idPort, "_showName");
        idPort.setTypeEquals(BaseType.STRING);

        outputsPort = new TypedIOPort(this, "outputs", false, true);
        new SingletonAttribute(outputsPort, "_showName");
        outputsPort.setTypeEquals(BaseType.GENERAL);

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

        List<OutputFile> files = null;
        try {
            TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);
            files = restAPI.getOutputsForTask(taskToGet);
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to get output list");
        }

        RecordToken[] tokens = new RecordToken[files.size()];
        String[] labels = {"url", "name"};

        for (int i = 0; i < files.size(); i++) {
            StringToken[] file = new StringToken[2];
            file[0] = new StringToken(files.get(i).getUrl());
            file[1] = new StringToken(files.get(i).getName());
            tokens[i] = new RecordToken(labels, file);
        }

        ArrayToken token = new ArrayToken(tokens);
        output.send(0, new StringToken(idString));
        outputsPort.send(0, token);
    }
}
