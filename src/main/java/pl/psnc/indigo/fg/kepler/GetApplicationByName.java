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
import java.text.MessageFormat;
import java.util.Objects;

/**
 * Actor which queries Future Gateway to get application details knowing its
 * name. See: {@link ApplicationsAPI#getAllApplications()}
 */
public class GetApplicationByName extends FutureGatewayActor {
    /**
     * Receives application's id to be queried in the Future Gateway.
     */
    private final TypedIOPort namePort;

    public GetApplicationByName(
            final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        namePort = new TypedIOPort(this, "name", true, false); //NON-NLS
        namePort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BeanTokenizer.getRecordType(Application.class));

        PortHelper.makePortNameVisible(namePort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String name = PortHelper.readStringMandatory(namePort);

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            ApplicationsAPI api = new ApplicationsAPI(URI.create(uri), token);

            for (final Application application : api.getAllApplications()) {
                if (Objects.equals(name, application.getName())) {
                    RecordToken recordToken =
                            BeanTokenizer.convert(application);
                    output.broadcast(recordToken);
                    return;
                }
            }

            String message = Messages.getString(
                    "failed.to.get.details.for.application.0");
            message = MessageFormat.format(message, name);
            throw new IllegalActionException(this, message);
        } catch (final FutureGatewayException e) {
            String message = Messages.getString(
                    "failed.to.get.details.for.application.0");
            message = MessageFormat.format(message, name);
            throw new IllegalActionException(this, e, message);
        }
    }
}
