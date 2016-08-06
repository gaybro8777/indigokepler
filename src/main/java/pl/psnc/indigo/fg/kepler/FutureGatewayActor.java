package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
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
     * Port for authorization token from the user.
     */
    @AllowedPublicField
    public TypedIOPort authorizationTokenPort;
    /**
     * Parameter for URI of a Future Gateway instance.
     */
    private StringParameter futureGatewayUri;
    /**
     * Parameter for authorization token from the user.
     */
    private StringParameter authorizationToken;

    public FutureGatewayActor(final CompositeEntity container,
                              final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        futureGatewayUriPort = new TypedIOPort(this, "futureGatewayUri", true,
                                               false);
        futureGatewayUriPort.setTypeEquals(BaseType.STRING);
        new SingletonAttribute(futureGatewayUriPort, "_showName");

        authorizationTokenPort = new TypedIOPort(this, "authorizationToken",
                                                 true, false);
        authorizationTokenPort.setTypeEquals(BaseType.STRING);
        new SingletonAttribute(authorizationTokenPort, "_showName");

        futureGatewayUri = new StringParameter(this, "futureGatewayUri");
        futureGatewayUri.setToken(RootAPI.LOCALHOST_ADDRESS.toString());

        authorizationToken = new StringParameter(this, "authorizationToken");
        authorizationToken.setToken("");
    }

    protected final String getFutureGatewayUri() throws IllegalActionException {
        if (futureGatewayUriPort.getWidth() > 0 && futureGatewayUriPort
                .hasToken(0)) {
            futureGatewayUri.setToken(futureGatewayUriPort.get(0));
        }
        return futureGatewayUri.stringValue();
    }

    protected final String getAuthorizationToken()
            throws IllegalActionException {
        if (authorizationTokenPort.getWidth() > 0 && authorizationTokenPort
                .hasToken(0)) {
            authorizationToken.setToken(authorizationTokenPort.get(0));
        }
        return authorizationToken.stringValue();
    }
}
