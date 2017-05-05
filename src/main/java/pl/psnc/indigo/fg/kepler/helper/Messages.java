package pl.psnc.indigo.fg.kepler.helper;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * A helper to get internationalized messages.
 */
public final class Messages {
    private static final ResourceBundle RESOURCE_BUNDLE =
            ResourceBundle.getBundle("kepler-messages"); //NON-NLS

    public static String getString(final String s) {
        return Messages.RESOURCE_BUNDLE.getString(s);
    }

    /**
     * A delegate method for {@link MessageFormat#format(String, Object...)}
     * where first argument is taken from resource bundle.
     *
     * @param s       Name of the resource in the resource bundle.
     * @param objects Paramters for
     *                {@link MessageFormat#format(String, Object...)} method.
     * @return A formatted string.
     */
    public static String format(final String s, final Object... objects) {
        return MessageFormat.format(Messages.getString(s), objects);
    }

    private Messages() {
        super();
    }
}
