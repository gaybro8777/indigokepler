package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.api.restful.jaxb.Upload;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
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
import ptolemy.kernel.util.SingletonAttribute;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Actor which uploads input files. See
 * {@link TasksAPI#uploadFileForTask(Task, File)}.
 */
@SuppressWarnings({"WeakerAccess", "PublicField",
                   "ThisEscapedInObjectConstruction",
                   "ResultOfObjectAllocationIgnored", "unused"})
public class UploadFiles extends FutureGatewayActor {
    /**
     * User id (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort userPort;
    /**
     * Task id (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort idPort;
    /**
     * List of input files (mandatory).
     */
    @AllowedPublicField
    public TypedIOPort inputFilesPort;
    /**
     * Output port repeating task id (useful in workflow design).
     */
    @AllowedPublicField
    public TypedIOPort idOutPort;

    public UploadFiles(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        idPort = new TypedIOPort(this, "id", true, false);
        new SingletonAttribute(idPort, "_showName");
        idPort.setTypeEquals(BaseType.STRING);

        inputFilesPort = new TypedIOPort(this, "inputFiles", true, false);
        new SingletonAttribute(inputFilesPort, "_showName");
        inputFilesPort.setTypeEquals(new ArrayType(BaseType.STRING));

        idOutPort = new TypedIOPort(this, "idOut", false, true);
        new SingletonAttribute(idOutPort, "_showName");
        idOutPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.GENERAL);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readStringMandatory(userPort);
        String id = PortHelper.readStringMandatory(idPort);
        List<String> inputFiles = PortHelper
                .readStringArrayMandatory(inputFilesPort);
        int size = inputFiles.size();

        Task task = new Task();
        task.setUser(user);
        task.setId(id);

        try {
            TasksAPI restAPI = new TasksAPI(URI.create(getFutureGatewayUri()));
            List<Token> tokens = new ArrayList<>(size);

            for (String inputFile : inputFiles) {
                File file = new File(inputFile);

                if (!file.canRead()) {
                    throw new IllegalActionException(this, "Cannot read file: "
                                                           + file);
                }

                Upload result = restAPI.uploadFileForTask(task, file);
                RecordToken recordToken = BeanTokenizer.convert(result);
                tokens.add(recordToken);
            }

            Token[] array = tokens.toArray(new Token[size]);
            output.broadcast(new ArrayToken(array));
            idOutPort.broadcast(new StringToken(id));
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to upload files");
        }
    }
}
