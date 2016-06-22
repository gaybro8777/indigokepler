package pl.psnc.indigo.fg.kepler.helper;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A collection of helpful utility methods to read data from Kepler actors'
 * ports.
 */
public final class PortHelper {
    /**
     * Read string from port, throw {@link IllegalActionException} in case it
     * is empty.
     *
     * @param port Actor's port to read data from.
     * @return String value of a {@link StringToken} in a port.
     * @throws IllegalActionException If port lacks any tokens.
     */
    public static String readStringMandatory(final TypedIOPort port)
            throws IllegalActionException {
        if (port.getWidth() > 0) {
            return ((StringToken) port.get(0)).stringValue();
        }
        throw new IllegalActionException(port, "Missing data on port " + port);
    }

    /**
     * Read string from port, return a default "" value in case it is empty.
     *
     * @param port Actor's port to read data from.
     * @return String value of a {@link StringToken} in a port.
     * @throws IllegalActionException If reading from port fails.
     */
    public static String readStringOptional(final TypedIOPort port)
            throws IllegalActionException {
        if (port.getWidth() > 0) {
            return ((StringToken) port.get(0)).stringValue();
        }
        return "";
    }

    /**
     * Read array of strings from port, throw {@link IllegalActionException}
     * if port is empty.
     *
     * @param port Port to read data from.
     * @return A list of strings made from {@link StringToken} inside of
     * actor's port.
     * @throws IllegalActionException If port is empty.
     */
    public static List<String> readStringArrayMandatory(final TypedIOPort port)
            throws IllegalActionException {
        if (port.getWidth() > 0) {
            return PortHelper.readStringArray(port);
        }
        throw new IllegalActionException(port, "Missing data on port " + port);
    }

    /**
     * Read array of strings from port, return {@link Collections#emptyList}
     * if port is empty.
     *
     * @param port Port to read data from.
     * @return A list of strings made from {@link StringToken} inside of
     * actor's port.
     * @throws IllegalActionException If reading from port fails.
     */
    public static List<String> readStringArrayOptional(final TypedIOPort port)
            throws IllegalActionException {
        if (port.getWidth() > 0) {
            return PortHelper.readStringArray(port);
        }

        return Collections.emptyList();
    }

    /**
     * Read array of strings from port. This private method assumes the port
     * contains some data. To decide what to do if that is not true, refer to
     * {@link PortHelper#readStringArrayMandatory(TypedIOPort)} or
     * {@link PortHelper#readStringArrayOptional(TypedIOPort)}.
     *
     * @param port Port to read data from.
     * @return A list of strings made from {@link StringToken} inside of
     * actor's port.
     * @throws IllegalActionException If reading from port fails.
     */
    private static List<String> readStringArray(final TypedIOPort port)
            throws IllegalActionException {
        ArrayToken tokenArray = (ArrayToken) port.get(0);
        int length = tokenArray.length();
        List<String> stringList = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            String arrayElement = ((StringToken) tokenArray.getElement(i))
                    .stringValue();
            stringList.add(arrayElement);
        }

        return stringList;
    }

    private PortHelper() {
    }
}
