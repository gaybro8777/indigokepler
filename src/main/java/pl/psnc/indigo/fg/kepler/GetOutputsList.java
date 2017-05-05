package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;
import java.util.List;

/**
 * Actor which retrieves task's output files and sends them in a
 * {@link RecordToken}.
 */
public class GetOutputsList extends FutureGatewayActor {
    /**
     * Task id (mandatory).
     */
    private final TypedIOPort idPort;

    public GetOutputsList(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false); //NON-NLS
        idPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(
                new ArrayType(BeanTokenizer.getRecordType(OutputFile.class)));

        PortHelper.makePortNameVisible(idPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        final String id = PortHelper.readStringMandatory(idPort);

        try {
            final String uri = getFutureGatewayUri();
            final String token = getAuthorizationToken();
            final TasksAPI restAPI = new TasksAPI(URI.create(uri), token);

            final List<OutputFile> files = restAPI.getTask(id).getOutputFiles();
            final RecordToken[] tokens = new RecordToken[files.size()];

            for (int i = 0; i < files.size(); i++) {
                final String name = files.get(i).getName();
                final String url = files.get(i).getUrl().toString();

                final StringToken[] file = new StringToken[2];
                file[0] = new StringToken(url);
                file[1] = new StringToken(name);
                tokens[i] = new RecordToken(new String[]{"url", "name"}, file);
            }

            output.broadcast(new ArrayToken(tokens));
        } catch (final FutureGatewayException e) {
            final String message =
                    Messages.format("failed.to.get.output.files.for.task.0",
                                    id);
            throw new IllegalActionException(this, e, message);
        }
    }
}
