package pl.psnc.indigo.fg.kepler.helper;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import pl.psnc.indigo.fg.api.restful.jaxb.FutureGatewayBean;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        } catch (final ReflectiveOperationException e) {
            throw new IllegalActionException(null, e, Messages.getString(
                    "failed.to.convert.a.bean.to.a.record.token"));
        }
    }

    /**
     * Convert a single object into a {@link Token}. If object's class is
     * annotated with {@link FutureGatewayBean}, then it will be converted
     * recursively into a {@link RecordToken}. For primitive data types,
     * currently supported tokens are: {@link Token#NIL}, {@link StringToken},
     * {@link BooleanToken} and {@link DateToken}.
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
        } else if (object instanceof Boolean) {
            return new BooleanToken((Boolean) object);
        } else if (object instanceof LocalDateTime) {
            long time = ((LocalDateTime) object).atZone(ZoneId.systemDefault())
                                                .toInstant().toEpochMilli();
            return new DateToken(time);
        } else if (object instanceof Enum) {
            String name = ((Enum<?>) object).name();
            return new StringToken(name);
        } else if (object.getClass()
                         .isAnnotationPresent(FutureGatewayBean.class)) {
            return BeanTokenizer.convert(object);
        } else if (object instanceof Collection<?>) {
            if (((Collection<?>) object).isEmpty()) {
                return new ArrayToken(BaseType.UNKNOWN);
            }

            List<Token> tokens = new ArrayList<>();
            for (final Object item : (Iterable<?>) object) {
                tokens.add(BeanTokenizer.asToken(item));
            }
            return new ArrayToken(tokens.toArray(new Token[tokens.size()]));
        } else {
            String value = object.toString();
            return new StringToken(value);
        }
    }

    public static RecordType getRecordType(final Class<?> beanClass) {
        Field[] fields = FieldUtils.getAllFields(beanClass);
        List<String> labels = new ArrayList<>(fields.length);
        List<Type> types = new ArrayList<>(fields.length);

        for (final Field field : fields) {
            labels.add(field.getName());

            if (Iterable.class.isAssignableFrom(field.getType())) {
                ParameterizedType genericType =
                        (ParameterizedType) field.getGenericType();
                Class<?> clazz =
                        (Class<?>) genericType.getActualTypeArguments()[0];
                types.add(new ArrayType(BeanTokenizer.asType(clazz)));
            } else {
                types.add(BeanTokenizer.asType(field.getType()));
            }

        }

        String[] labelsArray = labels.toArray(new String[labels.size()]);
        Type[] typesArray = types.toArray(new Type[types.size()]);
        return new RecordType(labelsArray, typesArray);
    }

    /**
     * Return Ptolemy {@link Type} corresponding to a given {@link Class}. For
     * example BaseType.BOOLEAN corresponds to boolean.class.
     *
     * @param clazz A given class to get Ptolemy type of.
     * @return Ptolemy {@link Type} of a token.
     */
    private static Type asType(final Class<?> clazz) {
        if (Objects.equals(clazz, boolean.class)) {
            return BaseType.BOOLEAN;
        } else if (Objects.equals(clazz, String.class)) {
            return BaseType.STRING;
        } else if (Objects.equals(clazz, LocalDateTime.class)) {
            return BaseType.DATE;
        } else if (Objects.equals(clazz, Enum.class)) {
            return BaseType.STRING;
        } else if (clazz.isAnnotationPresent(FutureGatewayBean.class)) {
            return BeanTokenizer.getRecordType(clazz);
        } else {
            return BaseType.STRING;
        }
    }

    /**
     * A private constructor to make this a utility class.
     */
    private BeanTokenizer() {
        super();
    }
}
