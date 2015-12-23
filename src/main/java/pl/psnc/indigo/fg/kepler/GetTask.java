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

public class GetTask extends LimitedFiringSource {

	public TypedIOPort userPort;
    	public TypedIOPort idPort;
	public TypedIOPort statusPort;

	public GetTask(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		userPort = new TypedIOPort(this,"user",true,false);
  		new SingletonAttribute(userPort, "_showName");
  		userPort.setTypeEquals(BaseType.STRING);

		idPort = new TypedIOPort(this,"id",true,false);
  		new SingletonAttribute(idPort, "_showName");
  		idPort.setTypeEquals(BaseType.STRING);

		statusPort = new TypedIOPort(this, "status", false, true);
		new SingletonAttribute(statusPort, "_showName");
		statusPort.setTypeEquals(BaseType.STRING);

		output.setTypeEquals(BaseType.STRING);
	}

	@Override
	public void fire() throws IllegalActionException {
		super.fire();

		String userString = null;
		String idString = null;

		if ( userPort.getWidth() > 0 ) {
      			StringToken userToken = (StringToken) userPort.get(0);
      			userString = userToken.stringValue();
  		}

		if ( idPort.getWidth() > 0 ) {
      			StringToken idToken = (StringToken) idPort.get(0);
      			idString = idToken.stringValue();
  		}

		TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);

		try {	
		    Task result = restAPI.getTask( userString, idString );
                    output.send(0, new StringToken( result.getId() ));
		    statusPort.send(0, new StringToken( result.getStatus() ));
                } catch(Exception ex) {
                    throw new IllegalActionException("There was an issue during task submission");
                }
	}
}
