package pl.psnc.indigo.fg.kepler.helper;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PortHelper {
    public static String readStringMandatory(TypedIOPort port)
            throws IllegalActionException {
        if (port.getWidth() > 0) {
            return ((StringToken) port.get(0)).stringValue();
        }
        throw new IllegalActionException(port, "Missing data on port " + port);
    }

    public static String readStringOptional(TypedIOPort port)
            throws IllegalActionException {
        if (port.getWidth() > 0) {
            return ((StringToken) port.get(0)).stringValue();
        }
        return "";
    }

    public static List<String> readStringArrayMandatory(TypedIOPort port)
            throws IllegalActionException {
        if (port.getWidth() > 0) {
            return PortHelper.readStringArray(port);
        }
        throw new IllegalActionException(port, "Missing data on port " + port);
    }

    public static List<String> readStringArrayOptional(TypedIOPort port)
            throws IllegalActionException {
        if (port.getWidth() > 0) {
            return PortHelper.readStringArray(port);
        }

        return Collections.emptyList();
    }

    private static List<String> readStringArray(TypedIOPort port)
            throws IllegalActionException {
        List<String> stringList = new ArrayList<>(10);
        ArrayToken tokenArray = (ArrayToken) port.get(0);

        for (int i = 0; i < tokenArray.length(); i++) {
            String arrayElement = ((StringToken) tokenArray.getElement(i))
                    .stringValue();
            stringList.add(arrayElement);
        }

        return stringList;
    }

    private PortHelper() {
    }
}
