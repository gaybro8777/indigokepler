package pl.psnc.indigo.fg.kepler.helper;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;

import java.util.ArrayList;
import java.util.List;

public final class PortHelper {
    public static String readString(TypedIOPort port) throws IllegalActionException {
        if (port.getWidth() > 0) {
            return ((StringToken) port.get(0)).stringValue();
        } else {
            throw new IllegalActionException(port, "Failed to delete task: missing data on port " + port);
        }
    }

    public static List<String> readStringArray(TypedIOPort port) throws IllegalActionException {
        if (port.getWidth() > 0) {
            List<String> stringList = new ArrayList<>();
            ArrayToken tokenArray = (ArrayToken) port.get(0);

            for (int i = 0; i < tokenArray.length(); i++) {
                StringToken arrayElement = (StringToken) tokenArray.getElement(i);
                stringList.add(arrayElement.stringValue());
            }

            return stringList;
        } else {
            throw new IllegalActionException(port, "Failed to delete task: missing data on port " + port);
        }


    }

    private PortHelper() {
    }
}
