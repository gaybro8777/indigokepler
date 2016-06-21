package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.api.restful.jaxb.Upload;
import pl.psnc.indigo.fg.kepler.helper.BeanTokenizer;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "WeakerAccess", "PublicField",
                    "ThisEscapedInObjectConstruction",
                    "ResultOfObjectAllocationIgnored", "unused" })
public class UploadFiles extends LimitedFiringSource {
    public TypedIOPort userPort;
    public TypedIOPort idPort;
    public TypedIOPort inputFilesPort;
    public TypedIOPort uploadURL;

    public UploadFiles(CompositeEntity container, String name)
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
        inputFilesPort.setTypeEquals(BaseType.GENERAL);

        uploadURL = new TypedIOPort(this, "uploadURL", true, false);
        new SingletonAttribute(uploadURL, "_showName");
        uploadURL.setTypeEquals(BaseType.STRING);

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
            TasksAPI restAPI = new TasksAPI(RootAPI.LOCALHOST_ADDRESS);
            List<Token> tokens = new ArrayList<>(size);

            for (String inputFile : inputFiles) {
                Upload result = restAPI
                        .uploadFileForTask(task, new File(inputFile));
                RecordToken recordToken = BeanTokenizer.convert(result);
                tokens.add(recordToken);
            }

            Token[] array = tokens.toArray(new Token[size]);
            output.broadcast(new ArrayToken(array));
        } catch (FutureGatewayException | NoSuchMethodException |
                InvocationTargetException | IllegalAccessException e) {
            throw new IllegalActionException(this, e, "Failed to upload files");
        }
    }
}
