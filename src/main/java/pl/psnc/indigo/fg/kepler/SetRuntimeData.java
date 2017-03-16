package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.KeyValue;
import pl.psnc.indigo.fg.api.restful.jaxb.PatchRuntimeData;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Actor which sets runtime data for a running task.
 */
public class SetRuntimeData extends FutureGatewayActor {
    private final TypedIOPort idPort;
    private final TypedIOPort namePort;
    private final TypedIOPort valuePort;

    public SetRuntimeData(
            final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false);
        idPort.setTypeEquals(BaseType.STRING);

        namePort = new TypedIOPort(this, "name", true, false);
        namePort.setTypeEquals(BaseType.STRING);

        valuePort = new TypedIOPort(this, "value", true, false);
        valuePort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);
        PortHelper.makePortNameVisible(idPort, namePort, valuePort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);
        String name = PortHelper.readStringMandatory(namePort);
        String value = PortHelper.readStringMandatory(valuePort);

        KeyValue keyValue = new KeyValue(name, value);
        List<KeyValue> runtimeData = Collections.singletonList(keyValue);
        PatchRuntimeData patchRuntimeData = new PatchRuntimeData();
        patchRuntimeData.setRuntimeData(runtimeData);

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            TasksAPI api = new TasksAPI(URI.create(uri), token);

            boolean flag = api.patchRuntimeData(id, patchRuntimeData);
            output.broadcast(new BooleanToken(flag));
        } catch (final FutureGatewayException e) {
            String message =
                    Messages.getString("failed.to.set.runtime.data.for.task.0");
            message = MessageFormat.format(message, id);
            throw new IllegalActionException(this, e, message);
        }
    }
}
