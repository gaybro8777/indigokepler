package pl.psnc.indigo.fg.kepler.helper;

import org.apache.commons.beanutils.PropertyUtils;
import pl.psnc.indigo.fg.api.restful.jaxb.FutureGatewayBean;
import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper, utility class to convert from bean to Kepler's record tokens.
 */
public final class BeanTokenizer {
    /**
     * Convert a bean object into Kepler's record token.
     *
     * @param beanObject A bean object with getters and setters for every
     *                   property.
     * @return A record token with map of key-value pairs representing bean's
     * properties.
     * @throws IllegalActionException If conversion process fails.
     */
    public static RecordToken convert(final Object beanObject)
            throws IllegalActionException {
        try {
            Map<String, Object> objectMap = PropertyUtils.describe(beanObject);
            int size = objectMap.size();
            Map<String, Token> tokenMap = new HashMap<>(size);

            for (final Map.Entry<String, Object> entry : objectMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Token token = BeanTokenizer.asToken(value);
                tokenMap.put(key, token);
            }

            return new RecordToken(tokenMap);
        } catch (
            // @formatter:off
                final NoSuchMethodException
                        | IllegalAccessException
                        | InvocationTargetException e) { // @formatter:on
            throw new IllegalActionException(null, e, Messages.getString(
                    "failed.to.convert.a.bean.to.a.record.token"));
        }
    }

    /**
     * Convert a single object into a {@link Token}. If object's class is
     * annotated with {@link FutureGatewayBean}, then it will be converted
     * recursively into a {@link RecordToken}. For primitive data types,
     * currently supported tokens are: {@link Token#NIL}, {@link StringToken},
     * {@link IntToken}, {@link BooleanToken} and {@link DateToken}.
     *
     * @param object An object to be converted.
     * @return A {@link Token} made out of the object.
     * @throws IllegalActionException If the object is a bean and its recursive
     *                                processing ends in error.
     */
    private static Token asToken(final Object object)
            throws IllegalActionException {
        if (object == null) {
            return Token.NIL;
        } else if (object instanceof String) {
            return new StringToken((String) object);
        } else if (object instanceof Integer) {
            return new IntToken((Integer) object);
        } else if (object instanceof Boolean) {
            return new BooleanToken((Boolean) object);
        } else if (object instanceof Date) {
            long time = ((Date) object).getTime();
            return new DateToken(time);
        } else if (object instanceof Enum) {
            String name = ((Enum<?>) object).name();
            return new StringToken(name);
        } else if (object.getClass()
                         .isAnnotationPresent(FutureGatewayBean.class)) {
            return BeanTokenizer.convert(object);
        } else {
            String value = object.toString();
            return new StringToken(value);
        }
    }

    private BeanTokenizer() {
        super();
    }
}
