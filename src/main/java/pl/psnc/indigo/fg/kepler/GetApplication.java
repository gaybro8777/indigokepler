package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.ApplicationsAPI;
import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Application;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.RecordToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings({ "WeakerAccess", "PublicField",
                    "ThisEscapedInObjectConstruction",
                    "ResultOfObjectAllocationIgnored", "unused" })
public class GetApplication extends LimitedFiringSource {
    public TypedIOPort idPort;

    public GetApplication(CompositeEntity container, String name)
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
                    RootAPI.LOCALHOST_ADDRESS);
            Application application = api.getApplication(id);
            RecordToken recordToken = BeanTokenizer.convert(application);
            output.broadcast(recordToken);
        } catch (FutureGatewayException | IllegalAccessException |
                NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalActionException(this, e, "Failed to list all "
                                                      + "applications");
        }
    }
}
