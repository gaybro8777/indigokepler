package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.ApplicationsAPI;
import pl.psnc.indigo.fg.api.restful.RootAPI;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Actor which lists all applications available in the Future Gateway
 * database. See {@link ApplicationsAPI#getAllApplications()}.
 */
@SuppressWarnings("unused")
public class GetAllApplications extends LimitedFiringSource {
    public GetAllApplications(final CompositeEntity container,
                              final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        try {
            ApplicationsAPI api = new ApplicationsAPI(
                    RootAPI.LOCALHOST_ADDRESS);
            List<Application> applications = api.getAllApplications();
            int size = applications.size();
            List<RecordToken> tokens = new ArrayList<>(size);

            for (Application application : applications) {
                RecordToken recordToken = BeanTokenizer.convert(application);
                tokens.add(recordToken);
            }

            Token[] array = tokens.toArray(new Token[size]);
            output.broadcast(new ArrayToken(array));
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e,
                                             "Failed to list all applications");
        }
    }
}
