package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

/**
 * An abstract actor which reads desired Future Gateway URI in the beginning
 * of its operation.
 */
public abstract class FutureGatewayActor extends LimitedFiringSource {
    /**
     * Port for URI of a Future Gateway instance.
     */
    @AllowedPublicField
    public TypedIOPort futureGatewayUriPort;
    /**
     * Parameter for URI of a Future Gateway instance.
     */
    private StringParameter futureGatewayUri;

    public FutureGatewayActor(final CompositeEntity container,
                              final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        futureGatewayUriPort = new TypedIOPort(this, "futureGatewayUri", true,
                                               false);
        futureGatewayUriPort.setTypeEquals(BaseType.STRING);
        new SingletonAttribute(futureGatewayUriPort, "_showName");

        futureGatewayUri = new StringParameter(this, "futureGatewayUri");
        futureGatewayUri.setToken(RootAPI.LOCALHOST_ADDRESS.toString());
    }

    protected final String getFutureGatewayUri() throws IllegalActionException {
        if (futureGatewayUriPort.getWidth() > 0 && futureGatewayUriPort
                .hasToken(0)) {
            Token uriToken = futureGatewayUriPort.get(0);
            futureGatewayUri.setToken(uriToken);
        }

        return futureGatewayUri.stringValue();
    }
}
