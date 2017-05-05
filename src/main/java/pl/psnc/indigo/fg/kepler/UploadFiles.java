package pl.psnc.indigo.fg.kepler;

import org.apache.commons.io.FileUtils;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.api.restful.jaxb.Upload;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Actor which uploads input files. See
 * {@link TasksAPI#uploadFileForTask(Task, File...)}.
 */
public class UploadFiles extends FutureGatewayActor {
    /**
     * Task id (mandatory).
     */
    private final TypedIOPort idPort;

    /**
     * List of input files (mandatory).
     */
    private final TypedIOPort inputFilesPort;

    /**
     * List of input files' names (optional). Use this if you want to send
     * file X under name Y.
     */
    private final TypedIOPort inputFilesNamesPort;

    /**
     * Output port repeating task id (useful in workflow design).
     */
    private final TypedIOPort idOutPort;

    public UploadFiles(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        idPort = new TypedIOPort(this, "id", true, false);
        idPort.setTypeEquals(BaseType.STRING);

        inputFilesPort = new TypedIOPort(this, "inputFiles", true, false);
        inputFilesPort.setTypeEquals(new ArrayType(BaseType.STRING));

        inputFilesNamesPort =
                new TypedIOPort(this, "inputFilesNames", true, false);
        inputFilesNamesPort.setTypeEquals(new ArrayType(BaseType.STRING));

        idOutPort = new TypedIOPort(this, "idOut", false, true);
        idOutPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BeanTokenizer.getRecordType(Upload.class));

        PortHelper.makePortNameVisible(idPort, inputFilesPort,
                                       inputFilesNamesPort, idOutPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        final String id = PortHelper.readStringMandatory(idPort);
        final List<String> inputFiles =
                PortHelper.readStringArrayMandatory(inputFilesPort);
        final List<String> inputFilesNames =
                PortHelper.readStringArrayOptional(inputFilesNamesPort);
        final int size = inputFiles.size();

        final Task task = new Task();
        task.setId(id);

        final File tempDirectory = createTempDirectory();

        try {
            final String uri = getFutureGatewayUri();
            final String token = getAuthorizationToken();
            final TasksAPI restAPI = new TasksAPI(URI.create(uri), token);

            final List<File> tempFiles = new ArrayList<>(inputFiles.size());

            for (int i = 0; i < inputFiles.size(); i++) {
                /* establish the valid name of file */
                final String inputFile = inputFiles.get(i);
                final String filename;
                if (inputFilesNames.size() == inputFiles.size()) {
                    filename = inputFilesNames.get(i);
                } else {
                    filename = new File(inputFile).getName();
                }

                /* check if file is readable */
                final File file = new File(inputFile);
                if (!file.canRead()) {
                    String message = Messages.getString("cannot.read.file.0");
                    message = MessageFormat.format(message, file);
                    throw new IllegalActionException(this, message);
                }

                /* create a copy under the valid name */
                final File tempFile = new File(tempDirectory, filename);
                FileUtils.copyFile(file, tempFile);
                tempFiles.add(tempFile);
            }

            final File[] tempFilesArray =
                    tempFiles.toArray(new File[tempFiles.size()]);
            final Upload result =
                    restAPI.uploadFileForTask(task, tempFilesArray);
            final RecordToken recordToken = BeanTokenizer.convert(result);

            output.broadcast(recordToken);
            idOutPort.broadcast(new StringToken(id));
        } catch (final FutureGatewayException | IOException e) {
            throw new IllegalActionException(this, e, Messages.getString(
                    "failed.to.upload.files"));
        } finally {
            FileUtils.deleteQuietly(tempDirectory);
        }
    }

    /**
     * Create a temporary directory with unique name.
     *
     * @return A {@link File} object representing a created directory.
     * @throws IllegalActionException If the creation was not possible.
     */
    private File createTempDirectory() throws IllegalActionException {
        final File tempDirectory = new File(FileUtils.getTempDirectory(),
                                            UUID.randomUUID().toString());
        if (!tempDirectory.mkdirs()) {
            final String message =
                    Messages.format("cannot.create.directory.0", tempDirectory);
            throw new IllegalActionException(this, message);
        }
        return tempDirectory;
    }
}
