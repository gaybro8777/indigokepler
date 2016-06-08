package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.ApplicationsAPI;
import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Application;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class GetApplication extends LimitedFiringSource {
    public TypedIOPort idPort;

    public GetApplication(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false);
        idPort.setTypeEquals(BaseType.STRING);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        String id = PortHelper.readString(idPort);

        Application application = new Application();
        application.setId(id);

        try {
            ApplicationsAPI api = new ApplicationsAPI(BaseAPI.LOCALHOST_ADDRESS);
            api.getApplication(application);
            output.broadcast(BeanTokenizer.convert(application));
        } catch (FutureGatewayException | URISyntaxException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalActionException(this, e, "Failed to list all applications");
        }
    }
}
