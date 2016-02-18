package pl.psnc.indigo.fg.kepler;

import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class GetRoot extends LimitedFiringSource {

  public GetRoot(CompositeEntity container, String name)
    throws NameDuplicationException, IllegalActionException {
    super(container, name);
    output.setTypeEquals(BaseType.STRING);
  }

  @Override
  public void fire() throws IllegalActionException {
    super.fire();

    String result = "Output";

    output.send(0, new StringToken(result));
  }
}
