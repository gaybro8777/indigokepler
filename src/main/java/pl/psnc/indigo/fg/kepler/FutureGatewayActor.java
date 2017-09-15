package pl.psnc.indigo.fg.kepler;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.api.restful.TokenHelper;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * An actor which reads desired Future Gateway URI in the beginning
 * of its operation.
 */
public class FutureGatewayActor extends LimitedFiringSource {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FutureGatewayActor.class);
    private static final String DEFAULT_TOKEN_SERVICE_URI =
            "https://fgw01.ncg.ingrid.pt/api/jsonws/iam.token/get-token";
    /**
     * 3 minutes is taken from indigo-dc/LiferayPlugins.
     * This is the duration, below which it uses refresh token.
     */
    private static final int MINUTES_BEFORE_EXPIRATION = 3;

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
    /**
     * Parameter for token webservice URI, part of indigo-dc/LiferayPlugins.
     */
    private final StringParameter tokenServiceUri;
    /**
     * Parameter for token webservice username, part of
     * indigo-dc/LiferayPlugins.
     */
    private final StringParameter tokenServiceUser;
    /**
     * Parameter for token webservice password, part of
     * indigo-dc/LiferayPlugins.
     */
    private final StringParameter tokenServicePassword;

    public FutureGatewayActor(final CompositeEntity container,
                              final String name)
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

        tokenServiceUri = new StringParameter(this, "tokenServiceUri");
        tokenServiceUri.setToken(FutureGatewayActor.DEFAULT_TOKEN_SERVICE_URI);

        tokenServiceUser = new StringParameter(this, "tokenServiceUser");
        tokenServiceUser.setToken("");

        tokenServicePassword =
                new StringParameter(this, "tokenServicePassword");
        tokenServicePassword.setToken("");

        PortHelper.makePortNameVisible(futureGatewayUriPort,
                                       authorizationTokenPort);
    }

    protected final String getFutureGatewayUri() throws IllegalActionException {
        if ((futureGatewayUriPort.getWidth() > 0) &&
            futureGatewayUriPort.hasToken(0)) {
            final Token token = futureGatewayUriPort.get(0);
            futureGatewayUri.setToken(token);
        }
        return futureGatewayUri.stringValue();
    }

    protected final String getAuthorizationToken()
            throws IllegalActionException {
        try {
            if ((authorizationTokenPort.getWidth() > 0) &&
                authorizationTokenPort.hasToken(0)) {
                final Token token = authorizationTokenPort.get(0);
                authorizationToken.setToken(token);
            }

            final DecodedJWT jwt = JWT.decode(authorizationToken.stringValue());

            if (jwt.getExpiresAt() != null) {
                final LocalDateTime now = LocalDateTime.now();
                final Instant instant =
                        Instant.ofEpochMilli(jwt.getExpiresAt().getTime());
                final LocalDateTime expiresAt = LocalDateTime
                        .ofInstant(instant, ZoneId.systemDefault());
                final Duration duration = Duration.between(now, expiresAt);
                final long minutes = duration.toMinutes();
                FutureGatewayActor.LOGGER
                        .info("Now is {}. IAM token expires at {}. The " +
                              "difference is {} minutes.", now, expiresAt,
                              minutes);

                if (minutes < FutureGatewayActor.MINUTES_BEFORE_EXPIRATION) {
                    final String token = refreshToken();
                    authorizationToken.setToken(token);
                }
            }
        } catch (final JWTDecodeException e) {
            // log error, but do not rethrow
            // in some cases, token can be invalid i.e. undecodeable, but this
            // is fine; for example, if token is not IAM-validated it does
            // not have to be a real JWT
            FutureGatewayActor.LOGGER.warn("Invalid token", e);
        }

        return authorizationToken.stringValue();
    }

    /**
     * Refresh IAM token using webservice from indigo-dc/LiferayPlugins.
     *
     * @return An IAM token which will be valid to use.
     * @throws IllegalActionException If REST communication fails.
     */
    private String refreshToken() throws IllegalActionException {
        try {
            final String token = TokenHelper
                    .getToken(authorizationToken.stringValue(),
                              URI.create(tokenServiceUri.stringValue()),
                              tokenServiceUser.stringValue(),
                              tokenServicePassword.stringValue());
            final DecodedJWT newJwt = JWT.decode(token);
            final LocalDateTime newExpiresAt = LocalDateTime
                    .ofInstant(newJwt.getExpiresAt().toInstant(),
                               ZoneId.systemDefault());
            FutureGatewayActor.LOGGER
                    .info("IAM token refreshed, expires at {}", newExpiresAt);
            return token;
        } catch (final FutureGatewayException | JWTDecodeException e) {
            throw new IllegalActionException(this, e,
                                             "Failed to refresh token");
        }
    }
}
