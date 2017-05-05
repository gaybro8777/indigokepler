package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.ApplicationsAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Application;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;

/**
 * Actor which queries Future Gateway for application details. See:
 * {@link ApplicationsAPI#getApplication(String)}
 */
public class GetApplication extends FutureGatewayActor {
    /**
     * Receives application's id to be queried in the Future Gateway.
     */
    private TypedIOPort idPort;

    public GetApplication(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false); //NON-NLS
        idPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BeanTokenizer.getRecordType(Application.class));

        PortHelper.makePortNameVisible(idPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        final String id = PortHelper.readStringMandatory(idPort);

        try {
            final String uri = getFutureGatewayUri();
            final String token = getAuthorizationToken();
            final ApplicationsAPI api =
                    new ApplicationsAPI(URI.create(uri), token);

            final Application application = api.getApplication(id);
            final RecordToken recordToken = BeanTokenizer.convert(application);
            output.broadcast(recordToken);
        } catch (final FutureGatewayException e) {
            final String message =
                    Messages.format("failed.to.get.details.for.application.0",
                                    id);
            throw new IllegalActionException(this, e, message);
        }
    }
}
