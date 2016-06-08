package pl.psnc.indigo.fg.kepler.helper;

import org.apache.commons.beanutils.PropertyUtils;
import pl.psnc.indigo.fg.api.restful.jaxb.FutureGatewayBean;
import ptolemy.data.*;
import ptolemy.kernel.util.IllegalActionException;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class BeanTokenizer {
    public static RecordToken convert(Object beanObject) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, IllegalActionException {
        Map<String, Object> objectMap = PropertyUtils.describe(beanObject);
        Map<String, Token> tokenMap = new HashMap();

        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            tokenMap.put(key, BeanTokenizer.asToken(value));
        }

        return new RecordToken(tokenMap);
    }

    private static Token asToken(Object object) throws InvocationTargetException, NoSuchMethodException, IllegalActionException, IllegalAccessException {
        if (object instanceof String) {
            return new StringToken((String) object);
        } else if (object instanceof Integer) {
            return new IntToken((Integer) object);
        } else if (object instanceof Boolean) {
            return new BooleanToken((Boolean) object);
        } else if (object instanceof Date) {
            return new DateToken(((Date) object).getTime());
        } else if (object instanceof Enum) {
            return new StringToken(((Enum) object).name());
        } else if (object.getClass().isAnnotationPresent(FutureGatewayBean.class)) {
            return BeanTokenizer.convert(object);
        } else {
            return new StringToken(object.toString());
        }
    }

    private BeanTokenizer() {
    }
}
