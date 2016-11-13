package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;
import java.text.MessageFormat;

/**
 * Actor which gets status of a task. See {@link TasksAPI#getTask(String)}.
 */
public class GetTask extends FutureGatewayActor {
    /**
     * Task id (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort idPort;

    /**
     * Output port which will receive task's status.
     */
    @AllowedPublicField
    public TypedIOPort statusPort;

    public GetTask(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false); //NON-NLS
        idPort.setTypeEquals(BaseType.STRING);

        statusPort = new TypedIOPort(this, "status", false, true); //NON-NLS
        statusPort.setTypeEquals(BaseType.STRING);

        output.setName("idOut"); //NON-NLS
        output.setTypeEquals(BaseType.STRING);

        PortHelper.makePortNameVisible(idPort, statusPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            TasksAPI api = new TasksAPI(URI.create(uri), token);

            Task task = api.getTask(id);
            String status = task.getStatus().name();

            output.send(0, new StringToken(id));
            statusPort.send(0, new StringToken(status));
        } catch (final FutureGatewayException e) {
            String message =
                    Messages.getString("failed.to.get.details.for.task.0");
            message = MessageFormat.format(message, id);
            throw new IllegalActionException(this, e, message);
        }
    }
}
