package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;

/**
 * Actor which deletes a task from the Future Gateway database. See
 * {@link TasksAPI#removeTask(String)}.
 */
public class DeleteTask extends FutureGatewayActor {
    /**
     * Task id (mandatory).
     */
    private final TypedIOPort idPort;

    public DeleteTask(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false); //NON-NLS
        idPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);

        PortHelper.makePortNameVisible(idPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            TasksAPI api = new TasksAPI(URI.create(uri), token);

            boolean isSuccess = api.removeTask(id);
            output.broadcast(new BooleanToken(isSuccess));
        } catch (final FutureGatewayException e) {
            throw new IllegalActionException(this, e, Messages.getString(
                    "failed.to.delete.task"));
        }
    }
}
