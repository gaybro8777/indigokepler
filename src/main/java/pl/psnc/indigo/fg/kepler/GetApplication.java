package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.ApplicationsAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Application;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.net.URI;

/**
 * Actor which queries Future Gateway for application details. See:
 * {@link ApplicationsAPI#getApplication(String)}
 */
@SuppressWarnings({"WeakerAccess", "PublicField",
                   "ThisEscapedInObjectConstruction",
                   "ResultOfObjectAllocationIgnored", "unused"})
public class GetApplication extends FutureGatewayActor {
    /**
     * Receives application's id to be queried in the Future Gateway.
     */
    @AllowedPublicField
    public TypedIOPort idPort;

    public GetApplication(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false);
        new SingletonAttribute(idPort, "_showName");
        idPort.setTypeEquals(BaseType.STRING);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readStringMandatory(idPort);

        try {
            ApplicationsAPI api = new ApplicationsAPI(
                    URI.create(futureGatewayUri.stringValue()));
            Application application = api.getApplication(id);
            RecordToken recordToken = BeanTokenizer.convert(application);
            output.broadcast(recordToken);
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to list all "
                                                      + "applications");
        }
    }
}
