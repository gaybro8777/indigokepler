package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.ApplicationsAPI;
import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Application;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class GetAllApplications extends LimitedFiringSource {
    public GetAllApplications(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        try {
            ApplicationsAPI api = new ApplicationsAPI(BaseAPI.LOCALHOST_ADDRESS);
            List<RecordToken> tokens = new ArrayList<>();

            for (Application application : api.getAllApplications()) {
                tokens.add(BeanTokenizer.convert(application));
            }

            output.broadcast(new ArrayToken(tokens.toArray(new Token[tokens.size()])));
        } catch (FutureGatewayException | URISyntaxException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalActionException(this, e, "Failed to list all applications");
        }
    }
}
