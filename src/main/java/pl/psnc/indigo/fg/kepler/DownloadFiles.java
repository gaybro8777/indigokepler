package pl.psnc.indigo.fg.kepler;

import java.io.File;
import java.util.ArrayList;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.SingletonAttribute;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.jaxb.InputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.api.restful.jaxb.Upload;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;

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

    ArrayList<OutputFile> outputFilesArray = new ArrayList<OutputFile>();
    String localFolderString = null;
    
    if (outputFilesPort.getWidth() > 0) {
      ArrayToken outputFilesToken = (ArrayToken) outputFilesPort.get(0);
      for (int i = 0; i < outputFilesToken.length(); i++) {
        RecordToken arrayElement = (RecordToken) outputFilesToken.getElement(i);
        OutputFile tmpFile = new OutputFile();
        tmpFile.setName(((StringToken)arrayElement.get("name")).stringValue());
        tmpFile.setUrl(((StringToken)arrayElement.get("url")).stringValue());
        
        outputFilesArray.add(tmpFile);
      }
    }

    if (localFolderPort.getWidth() > 0) {
      StringToken inputToken = (StringToken) localFolderPort.get(0);
      localFolderString = inputToken.stringValue();
    }

    TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);

    try {
      for (int i = 0; i < outputFilesArray.size(); i++) {
        
        boolean flag = restAPI.downloadOutputFile(outputFilesArray.get(i), localFolderString);
        if(flag == false) {
          output.send(0, new BooleanToken(false));
          return;
        }
      }
    } catch (Exception ex) {
      throw new IllegalActionException("There was an issue while uploading file.");
    }
    output.send(0, new BooleanToken(true));
  }
}
