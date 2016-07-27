package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Actor which reports all tasks belonging to a user. See
 * {@link TasksAPI#getAllTasks(String)}.
 */
@SuppressWarnings({"WeakerAccess", "PublicField",
                   "ThisEscapedInObjectConstruction",
                   "ResultOfObjectAllocationIgnored", "unused"})
public class GetAllTasks extends FutureGatewayActor {
    /**
     * User id (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort userPort;

    public GetAllTasks(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.STRING);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readStringMandatory(userPort);

        try {
            TasksAPI restAPI = new TasksAPI(
                    URI.create(futureGatewayUri.stringValue()));
            List<Task> tasks = restAPI.getAllTasks(user);
            int size = tasks.size();
            List<RecordToken> tokens = new ArrayList<>(size);

            for (Task task : tasks) {
                RecordToken recordToken = BeanTokenizer.convert(task);
                tokens.add(recordToken);
            }

            Token[] array = tokens.toArray(new Token[size]);
            output.broadcast(new ArrayToken(array));
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e,
                                             "Failed to get all tasks");
        }
    }
}
