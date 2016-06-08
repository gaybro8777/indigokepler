package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DownloadFiles extends LimitedFiringSource {
    public TypedIOPort outputFilesPort;
    public TypedIOPort localFolderPort;

    public DownloadFiles(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        outputFilesPort = new TypedIOPort(this, "outputFiles", true, false);
        new SingletonAttribute(outputFilesPort, "_showName");
        outputFilesPort.setTypeEquals(BaseType.GENERAL);

        localFolderPort = new TypedIOPort(this, "local_folder", true, false);
        new SingletonAttribute(localFolderPort, "_showName");
        localFolderPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        File localFolderString = new File(PortHelper.readString(localFolderPort));
        List<OutputFile> outputFilesArray = new ArrayList<>();

        if (outputFilesPort.getWidth() > 0) {
            ArrayToken outputFilesToken = (ArrayToken) outputFilesPort.get(0);
            
            for (int i = 0; i < outputFilesToken.length(); i++) {
                RecordToken arrayElement = (RecordToken) outputFilesToken.getElement(i);
                OutputFile tmpFile = new OutputFile();
                tmpFile.setName(((StringToken) arrayElement.get("name")).stringValue());
                tmpFile.setUrl(((StringToken) arrayElement.get("url")).stringValue());
                outputFilesArray.add(tmpFile);
            }
        }

        try {
            TasksAPI api = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);

            for (int i = 0; i < outputFilesArray.size(); i++) {
                api.downloadOutputFile(outputFilesArray.get(i), localFolderString);
            }

            output.send(0, new BooleanToken(true));
        } catch (FutureGatewayException | URISyntaxException e) {
            throw new IllegalActionException(this, e, "Failed to download files");
        }
    }
}
