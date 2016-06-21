package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.RootAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.InputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Link;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "WeakerAccess", "PublicField",
                    "ThisEscapedInObjectConstruction",
                    "ResultOfObjectAllocationIgnored" })
public class CreateTask extends LimitedFiringSource {
    public TypedIOPort userPort;            // user name in FG database
    public TypedIOPort applicationPort;     // application id in FG database
    public TypedIOPort descriptionPort;     // description passed to app
    public TypedIOPort argumentsPort;       // array of arguments passed to app
    public TypedIOPort inputFilesPort;      // array of input files
    public TypedIOPort outputFilesPort;     // array of output files
    public TypedIOPort inputLoctaion;

    public CreateTask(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        applicationPort = new TypedIOPort(this, "application", true, false);
        new SingletonAttribute(applicationPort, "_showName");
        applicationPort.setTypeEquals(BaseType.STRING);

        descriptionPort = new TypedIOPort(this, "description", true, false);
        new SingletonAttribute(descriptionPort, "_showName");
        descriptionPort.setTypeEquals(BaseType.STRING);

        argumentsPort = new TypedIOPort(this, "arguments", true, false);
        new SingletonAttribute(argumentsPort, "_showName");
        argumentsPort.setTypeEquals(BaseType.GENERAL);

        inputFilesPort = new TypedIOPort(this, "input_files", true, false);
        new SingletonAttribute(inputFilesPort, "_showName");
        inputFilesPort.setTypeEquals(BaseType.GENERAL);

        outputFilesPort = new TypedIOPort(this, "output_files", true, false);
        new SingletonAttribute(outputFilesPort, "_showName");
        outputFilesPort.setTypeEquals(BaseType.GENERAL);

        inputLoctaion = new TypedIOPort(this, "inputs", false, true);
        new SingletonAttribute(inputLoctaion, "_showName");
        inputLoctaion.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.STRING);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readStringMandatory(userPort);
        String application = PortHelper.readStringMandatory(applicationPort);
        String description = PortHelper.readStringOptional(descriptionPort);
        List<String> arguments = PortHelper
                .readStringArrayOptional(argumentsPort);
        List<String> inputFileNames = PortHelper
                .readStringArrayOptional(inputFilesPort);
        List<String> outputFileNames = PortHelper
                .readStringArrayOptional(outputFilesPort);

        int inputSize = inputFileNames.size();
        int outputSize = outputFileNames.size();
        List<InputFile> inputFiles = new ArrayList<>(inputSize);
        List<OutputFile> outputFiles = new ArrayList<>(outputSize);

        for (String fileName : inputFileNames) {
            InputFile inputFile = new InputFile();
            inputFile.setName(fileName);
            inputFiles.add(inputFile);
        }
        for (String fileName : outputFileNames) {
            OutputFile outputFile = new OutputFile();
            outputFile.setName(fileName);
            outputFiles.add(outputFile);
        }

        Task task = new Task();
        task.setUser(user);
        task.setDescription(description);
        task.setApplication(application);
        task.setArguments(arguments);
        task.setInputFiles(inputFiles);
        task.setOutputFiles(outputFiles);

        try {
            TasksAPI api = new TasksAPI(RootAPI.LOCALHOST_ADDRESS);
            task = api.createTask(task);
            String id = task.getId();

            // We have to make sure there are inputs required by the application
            String linksURL = "";
            for (Link link : task.getLinks()) {
                String rel = link.getRel();
                if ("input".equals(rel)) {
                    linksURL = link.getHref();
                }
            }

            inputLoctaion.send(0, new StringToken(linksURL));
            output.send(0, new StringToken(id));
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to create task");
        }
    }
}
