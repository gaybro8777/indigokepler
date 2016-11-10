package pl.psnc.indigo.fg.kepler.helper;

import java.util.ResourceBundle;

/**
 * A helper to get internationalized messages.
 */
public final class Messages {
    private static final ResourceBundle RESOURCE_BUNDLE =
            ResourceBundle.getBundle("messages"); //NON-NLS

    public static String getString(final String s) {
        return Messages.RESOURCE_BUNDLE.getString(s);
    }

    private Messages() {
        super();
    }
}
