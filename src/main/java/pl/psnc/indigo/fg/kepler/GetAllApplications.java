package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.ApplicationsAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Application;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Actor which lists all applications available in the Future Gateway
 * database. See {@link ApplicationsAPI#getAllApplications()}.
 */
public class GetAllApplications extends FutureGatewayActor {
    public GetAllApplications(
            final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(
                new ArrayType(BeanTokenizer.getRecordType(Application.class)));
        PortHelper.makePortNameVisible(output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            ApplicationsAPI api = new ApplicationsAPI(URI.create(uri), token);

            List<Application> applications = api.getAllApplications();
            int size = applications.size();

            List<RecordToken> tokens = new ArrayList<>(size);
            for (final Application application : applications) {
                RecordToken recordToken = BeanTokenizer.convert(application);
                tokens.add(recordToken);
            }

            Token[] array = tokens.toArray(new Token[size]);
            output.broadcast(new ArrayToken(array));
        } catch (final FutureGatewayException e) {
            throw new IllegalActionException(this, e, Messages.getString(
                    "failed.to.list.all.applications"));
        }
    }
}
