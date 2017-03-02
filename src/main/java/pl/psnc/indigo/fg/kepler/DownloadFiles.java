package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;

/**
 * Download task's output files into a local directory. See {@link
 * TasksAPI#downloadOutputFile(OutputFile, File)}.
 */
public class DownloadFiles extends FutureGatewayActor {
    /**
     * A list of {@link RecordToken} with "name" and "url" describing the files
     * to be downloaded (mandatory).
     */
    private final TypedIOPort outputFilesPort;

    /** A local directory where files will be downloaded (mandatory). */
    private final TypedIOPort localFolderPort;

    public DownloadFiles(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        outputFilesPort =
                new TypedIOPort(this, "outputFiles", true, false); //NON-NLS
        outputFilesPort.setTypeEquals(
                new ArrayType(BeanTokenizer.getRecordType(OutputFile.class)));

        localFolderPort =
                new TypedIOPort(this, "localFolder", true, false); //NON-NLS
        localFolderPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);

        PortHelper
                .makePortNameVisible(outputFilesPort, localFolderPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String localFolderPath =
                PortHelper.readStringMandatory(localFolderPort);

        if (outputFilesPort.getWidth() > 0) {
            ArrayToken outputFiles = (ArrayToken) outputFilesPort.get(0);
            int length = outputFiles.length();

            try {
                String uri = getFutureGatewayUri();
                String token = getAuthorizationToken();
                TasksAPI api = new TasksAPI(URI.create(uri), token);

                File localFolder = new File(localFolderPath);
                for (int i = 0; i < length; i++) {
                    RecordToken record =
                            (RecordToken) outputFiles.getElement(i);
                    StringToken nameToken = (StringToken) record.get("name");
                    StringToken urlToken = (StringToken) record.get("url");

                    String name = nameToken.stringValue();
                    String url = urlToken.stringValue();
                    URI outputUri = UriBuilder.fromUri(url).build();

                    OutputFile outputFile = new OutputFile();
                    outputFile.setName(name);
                    outputFile.setUrl(outputUri);

                    api.downloadOutputFile(outputFile, localFolder);
                }
            } catch (final FutureGatewayException e) {
                String message = Messages.getString("failed.to.download.files");
                throw new IllegalActionException(this, e, message);
            }
        }

        output.send(0, new BooleanToken(true));
    }
}
