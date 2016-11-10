package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Actor which reports all tasks belonging to a user. See
 * {@link TasksAPI#getAllTasks(String)}.
 */
public class GetAllTasks extends FutureGatewayActor {
    /**
     * User id (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort userPort;

    public GetAllTasks(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false); //NON-NLS
        userPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.STRING);

        PortHelper.makePortNameVisible(userPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readStringMandatory(userPort);

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            TasksAPI api = new TasksAPI(URI.create(uri), token);

            List<Task> tasks = api.getAllTasks(user);
            int size = tasks.size();

            List<RecordToken> tokens = new ArrayList<>(size);
            for (final Task task : tasks) {
                RecordToken recordToken = BeanTokenizer.convert(task);
                tokens.add(recordToken);
            }

            Token[] array = tokens.toArray(new Token[size]);
            output.broadcast(new ArrayToken(array));
        } catch (final FutureGatewayException e) {
            throw new IllegalActionException(this, e, Messages.getString(
                    "failed.to.get.all.tasks"));
        }
    }
}
