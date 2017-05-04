package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Actor which reports all tasks belonging to a user. See
 * {@link TasksAPI#getAllTasks()}.
 */
public class GetAllTasks extends FutureGatewayActor {
    public GetAllTasks(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(
                new ArrayType(BeanTokenizer.getRecordType(Task.class)));
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            TasksAPI api = new TasksAPI(URI.create(uri), token);

            List<Task> tasks = api.getAllTasks();
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
