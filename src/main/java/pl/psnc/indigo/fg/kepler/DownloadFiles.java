package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
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
import ptolemy.kernel.util.SingletonAttribute;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Download task's output files into a local directory. See
 * {@link TasksAPI#downloadOutputFile(OutputFile, File)}.
 */
@SuppressWarnings({"WeakerAccess", "PublicField",
                   "ThisEscapedInObjectConstruction",
                   "ResultOfObjectAllocationIgnored", "unused"})
public class DownloadFiles extends FutureGatewayActor {
    /**
     * A list of {@link RecordToken} with "name" and "url" describing the
     * files to be downloaded (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort outputFilesPort;
    /**
     * A local directory where files will be downloaded (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort localFolderPort;

    public DownloadFiles(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        outputFilesPort = new TypedIOPort(this, "outputFiles", true, false);
        new SingletonAttribute(outputFilesPort, "_showName");
        outputFilesPort
                .setTypeEquals(new ArrayType(GetOutputsList.OUTPUT_FILE_TYPE));

        localFolderPort = new TypedIOPort(this, "localFolder", true, false);
        new SingletonAttribute(localFolderPort, "_showName");
        localFolderPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String localFolderPath = PortHelper
                .readStringMandatory(localFolderPort);
        File localFolder = new File(localFolderPath);

        if (outputFilesPort.getWidth() > 0) {
            ArrayToken outputFiles = (ArrayToken) outputFilesPort.get(0);
            int length = outputFiles.length();

            try {
                TasksAPI api = new TasksAPI(
                        URI.create(futureGatewayUri.stringValue()));

                for (int i = 0; i < length; i++) {
                    RecordToken token = (RecordToken) outputFiles.getElement(i);
                    StringToken nameToken = (StringToken) token.get("name");
                    StringToken urlToken = (StringToken) token.get("url");

                    String name = nameToken.stringValue();
                    String url = urlToken.stringValue();
                    URI uri = UriBuilder.fromUri(url).build();

                    OutputFile outputFile = new OutputFile();
                    outputFile.setName(name);
                    outputFile.setUrl(uri);

                    api.downloadOutputFile(outputFile, localFolder);
                }
            } catch (FutureGatewayException | IOException e) {
                throw new IllegalActionException(this, e, "Failed to download "
                                                          + "files");
            }

        }

        output.send(0, new BooleanToken(true));
    }
}
