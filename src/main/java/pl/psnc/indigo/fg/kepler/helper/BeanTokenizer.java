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
import java.util.Map.Entry;

public final class BeanTokenizer {
    public static RecordToken convert(final Object beanObject)
            throws IllegalAccessException, NoSuchMethodException,
                   InvocationTargetException, IllegalActionException {
        Map<String, Object> objectMap = PropertyUtils.describe(beanObject);
        Map<String, Token> tokenMap = new HashMap<>();

        for (Entry<String, Object> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Token token = BeanTokenizer.asToken(value);
            tokenMap.put(key, token);
        }

        return new RecordToken(tokenMap);
    }

    private static Token asToken(final Object object)
            throws InvocationTargetException, NoSuchMethodException,
                   IllegalActionException, IllegalAccessException {
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
    }
}
