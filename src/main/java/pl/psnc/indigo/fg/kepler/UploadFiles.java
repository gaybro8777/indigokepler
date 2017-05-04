package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.api.restful.jaxb.Upload;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Actor which uploads input files. See
 * {@link TasksAPI#uploadFileForTask(Task, File...)}.
 */
public class UploadFiles extends FutureGatewayActor {
    /**
     * User id (mandatory).
     */
    private final TypedIOPort userPort;

    /**
     * Task id (mandatory).
     */
    private final TypedIOPort idPort;

    /**
     * List of input files (mandatory).
     */
    private final TypedIOPort inputFilesPort;

    /**
     * Output port repeating task id (useful in workflow design).
     */
    private final TypedIOPort idOutPort;

    public UploadFiles(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        userPort.setTypeEquals(BaseType.STRING);

        idPort = new TypedIOPort(this, "id", true, false);
        idPort.setTypeEquals(BaseType.STRING);

        inputFilesPort = new TypedIOPort(this, "inputFiles", true, false);
        inputFilesPort.setTypeEquals(new ArrayType(BaseType.STRING));

        idOutPort = new TypedIOPort(this, "idOut", false, true);
        idOutPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(
                new ArrayType(BeanTokenizer.getRecordType(Upload.class)));

        PortHelper.makePortNameVisible(userPort, idPort, inputFilesPort,
                                       idOutPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readStringMandatory(userPort);
        String id = PortHelper.readStringMandatory(idPort);
        List<String> inputFiles =
                PortHelper.readStringArrayMandatory(inputFilesPort);
        int size = inputFiles.size();

        Task task = new Task();
        task.setUser(user);
        task.setId(id);

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            TasksAPI restAPI = new TasksAPI(URI.create(uri), token);

            List<Token> tokens = new ArrayList<>(size);
            for (final String inputFile : inputFiles) {
                File file = new File(inputFile);

                if (!file.canRead()) {
                    String message = Messages.getString("cannot.read.file.0");
                    message = MessageFormat.format(message, file);
                    throw new IllegalActionException(this, message);
                }

                Upload result = restAPI.uploadFileForTask(task, file);
                RecordToken recordToken = BeanTokenizer.convert(result);
                tokens.add(recordToken);
            }

            Token[] array = tokens.toArray(new Token[size]);
            output.broadcast(new ArrayToken(array));
            idOutPort.broadcast(new StringToken(id));
        } catch (final FutureGatewayException e) {
            throw new IllegalActionException(this, e, Messages.getString(
                    "failed.to.upload.files"));
        }
    }
}
