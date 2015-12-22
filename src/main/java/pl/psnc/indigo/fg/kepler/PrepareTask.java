package pl.psnc.indigo.fg.kepler;

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
import pl.psnc.indigo.fg.api.restful.jaxb.Task;

public class PrepareTask extends LimitedFiringSource {

	public TypedIOPort userPort;
    	public TypedIOPort applicationPort;
    	public TypedIOPort descriptionPort;

	public PrepareTask(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		userPort = new TypedIOPort(this,"user",true,false);
  		new SingletonAttribute(userPort, "_showName");
  		userPort.setTypeEquals(BaseType.STRING);

		applicationPort = new TypedIOPort(this,"application",true,false);
  		new SingletonAttribute(userPort, "_showName");
  		applicationPort.setTypeEquals(BaseType.STRING);

		descriptionPort = new TypedIOPort(this,"description",true,false);
  		new SingletonAttribute(userPort, "_showName");
  		descriptionPort.setTypeEquals(BaseType.STRING);

		output.setTypeEquals(BaseType.STRING);
	}

	@Override
	public void fire() throws IllegalActionException {
		super.fire();

		String userString = null;
		String applicationString = null;
		String descriptionString = null;

		if ( userPort.getWidth() > 0 ) {
      			StringToken userToken = (StringToken) userPort.get(0);
      			userString = userToken.stringValue();
  		}

		if ( applicationPort.getWidth() > 0 ) {
      			StringToken applicationToken = (StringToken) applicationPort.get(0);
      			applicationString = applicationToken.stringValue();
  		}

		if ( descriptionPort.getWidth() > 0 ) {
      			StringToken descriptionToken = (StringToken) descriptionPort.get(0);
      			descriptionString = descriptionToken.stringValue();
  		}

		TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);
	
		Task result = restAPI.prepareTask( userString, applicationString, descriptionString );
		
                try {
                    output.send(0, new StringToken( result.getId() ));
                } catch(Exception ex) {
                    throw new IllegalActionException("There was an issue while parsing JSON output - there is no 'id' field or it is incorrec");
                }
	}
}
