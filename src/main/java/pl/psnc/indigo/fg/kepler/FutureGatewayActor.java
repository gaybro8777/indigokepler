package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * An actor which reads desired Future Gateway URI in the beginning
 * of its operation.
 */
public class FutureGatewayActor extends LimitedFiringSource {
    /**
     * Port for URI of a Future Gateway instance.
     */
    private final TypedIOPort futureGatewayUriPort;
    /**
     * Port for authorization token from the user.
     */
    private final TypedIOPort authorizationTokenPort;
    /**
     * Parameter for URI of a Future Gateway instance.
     */
    private final StringParameter futureGatewayUri;
    /**
     * Parameter for authorization token from the user.
     */
    private final StringParameter authorizationToken;

    public FutureGatewayActor(
            final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        futureGatewayUriPort = new TypedIOPort(this, "futureGatewayUri", true,
                                               false); //NON-NLS
        futureGatewayUriPort.setTypeEquals(BaseType.STRING);

        authorizationTokenPort =
                new TypedIOPort(this, "authorizationToken", true,
                                false); //NON-NLS
        authorizationTokenPort.setTypeEquals(BaseType.STRING);

        futureGatewayUri =
                new StringParameter(this, "futureGatewayUri"); //NON-NLS
        futureGatewayUri.setToken(RootAPI.LOCALHOST_ADDRESS.toString());

        authorizationToken =
                new StringParameter(this, "authorizationToken"); //NON-NLS
        authorizationToken.setToken("");

        PortHelper.makePortNameVisible(futureGatewayUriPort,
                                       authorizationTokenPort);
    }

    protected final String getFutureGatewayUri() throws IllegalActionException {
        if ((futureGatewayUriPort.getWidth() > 0) && futureGatewayUriPort
                .hasToken(0)) {
            Token token = futureGatewayUriPort.get(0);
            futureGatewayUri.setToken(token);
        }
        return futureGatewayUri.stringValue();
    }

    protected final String getAuthorizationToken()
            throws IllegalActionException {
        if ((authorizationTokenPort.getWidth() > 0) && authorizationTokenPort
                .hasToken(0)) {
            Token token = authorizationTokenPort.get(0);
            authorizationToken.setToken(token);
        }
        return authorizationToken.stringValue();
    }
}
