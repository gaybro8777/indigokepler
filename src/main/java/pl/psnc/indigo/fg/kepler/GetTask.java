package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;

/**
 * Actor which gets status of a task. See {@link TasksAPI#getTask(String)}.
 */
public class GetTask extends FutureGatewayActor {
    /**
     * Task id (mandatory).
     */
    private final TypedIOPort idPort;

    public GetTask(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false); //NON-NLS
        idPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BeanTokenizer.getRecordType(Task.class));

        PortHelper.makePortNameVisible(idPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        final String id = PortHelper.readStringMandatory(idPort);

        try {
            final String uri = getFutureGatewayUri();
            final String token = getAuthorizationToken();
            final TasksAPI api = new TasksAPI(URI.create(uri), token);

            final Task task = api.getTask(id);
            output.broadcast(BeanTokenizer.convert(task));
        } catch (final FutureGatewayException e) {
            final String message =
                    Messages.format("failed.to.get.details.for.task.0", id);
            throw new IllegalActionException(this, e, message);
        }
    }
}
