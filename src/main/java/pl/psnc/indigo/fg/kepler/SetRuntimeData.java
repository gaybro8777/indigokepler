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
import java.util.Collections;
import java.util.List;

/**
 * Actor which sets runtime data for a running task.
 */
public class SetRuntimeData extends FutureGatewayActor {
    private final TypedIOPort idPort;
    private final TypedIOPort namePort;
    private final TypedIOPort valuePort;

    public SetRuntimeData(final CompositeEntity container, final String name)
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

        final String id = PortHelper.readStringMandatory(idPort);
        final String name = PortHelper.readStringMandatory(namePort);
        final String value = PortHelper.readStringMandatory(valuePort);

        final KeyValue keyValue = new KeyValue(name, value);
        final List<KeyValue> runtimeData = Collections.singletonList(keyValue);
        final PatchRuntimeData patchRuntimeData = new PatchRuntimeData();
        patchRuntimeData.setRuntimeData(runtimeData);

        try {
            final String uri = getFutureGatewayUri();
            final String token = getAuthorizationToken();
            final TasksAPI api = new TasksAPI(URI.create(uri), token);

            final boolean flag = api.patchRuntimeData(id, patchRuntimeData);
            output.broadcast(new BooleanToken(flag));
        } catch (final FutureGatewayException e) {
            final String message =
                    Messages.format("failed.to.set.runtime.data.for.task.0",
                                    id);
            throw new IllegalActionException(this, e, message);
        }
    }
}
