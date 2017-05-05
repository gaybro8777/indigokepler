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

    public static String format(final String s, final Object... objects) {
        return MessageFormat.format(Messages.getString(s), objects);
    }

    private Messages() {
        super();
    }
}
