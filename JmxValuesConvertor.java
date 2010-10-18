
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Convert from jmx strings into java objects
 */
public class JmxValuesConvertor {
    // contains all date and date time format instances
    // for the current locale
    private static final DateFormat[] allFormats = new DateFormat[]{
            DateFormat.getDateInstance(),
            DateFormat.getTimeInstance(),
            DateFormat.getDateTimeInstance(),
            // first pure date format
            DateFormat.getDateInstance(DateFormat.SHORT),
            DateFormat.getDateInstance(DateFormat.MEDIUM),
            DateFormat.getDateInstance(DateFormat.LONG),
            DateFormat.getDateInstance(DateFormat.FULL),
            // pure time format
            DateFormat.getTimeInstance(DateFormat.SHORT),
            DateFormat.getTimeInstance(DateFormat.MEDIUM),
            DateFormat.getTimeInstance(DateFormat.LONG),
            DateFormat.getTimeInstance(DateFormat.FULL),
            // combinations
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM),
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG),
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL),

            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM),
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG),
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL),

            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM),
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG),
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL),

            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM),
            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG),
            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
    };

    /**
     * Creates a parameter object of the given type containing a given value.
     * If the type is unknown null is returned
     *
     * @param parameterType  Indicates the type of the parameter, for instance java.lang.String
     * @param parameterValue The value of the parameter as a String
     * @return an object of parameterType type and value parameterValue or null if the type is unknown
     * @throws Thrown in case there is a data conversion error
     */
    public static Object createParameterValue(String parameterType, String parameterValue)
            throws Exception {
        if ((parameterValue == null || parameterValue.length() == 0) && !parameterType.equals("java.lang.String")) {
            return null;
        }
        if (parameterType.equals("java.lang.String")) {
            return parameterValue;
        } else if (parameterType.equals("java.lang.Integer") || parameterType.equals("int")) {
            return Integer.valueOf(parameterValue);
        } else if (parameterType.equals("java.lang.Long") || parameterType.equals("long")) {
            return Long.valueOf(parameterValue);
        } else if (parameterType.equals("java.lang.Short") || parameterType.equals("short")) {
            return Short.valueOf(parameterValue);
        } else if (parameterType.equals("java.lang.Byte") || parameterType.equals("byte")) {
            return Byte.valueOf(parameterValue);
        } else if (parameterType.equals("java.lang.Float") || parameterType.equals("float")) {
            return Float.valueOf(parameterValue);
        }
        // changed java.lang.dobule to java.lang.double bronwen
        else if (parameterType.equals("java.lang.Double") || parameterType.equals("double")) {
            return Double.valueOf(parameterValue);
        } else if (parameterType.equals("java.lang.Boolean") || parameterType.equals("boolean")) {
            return Boolean.valueOf(parameterValue);
        } else if (parameterType.equals("java.lang.Void")) {
            return Void.TYPE;
        } else if (parameterType.equals("java.util.Date")) {
            // this is tricky since Date can be written in many formats
            // will use the Date format with current locale and several
            // different formats
            Date value = null;
            for (int i = 0; i < allFormats.length; i++) {
                synchronized (allFormats[i]) {
                    try {
                        System.out.println(parameterValue + " " + allFormats[i]);
                        value = allFormats[i].parse(parameterValue);
                        // if succeful then break
                        break;
                    }
                    catch (ParseException e) {
                        // ignore, the format wasn't appropriate
                    }
                }
            }
            if (value == null) {
                throw new ParseException("Not possible to parse", 0);
            }
            return value;
        } else if (parameterType.equals("java.lang.Number")) {
            Number value = null;
            // try first as a long
            try {
                value = Long.valueOf(parameterValue);
            }
            catch (NumberFormatException e) {
            }
            // if not try as a double
            if (value == null) {
                try {
                    value = Double.valueOf(parameterValue);
                }
                catch (NumberFormatException e) {
                }
            }
            if (value == null) {
                throw new NumberFormatException("Not possible to parse");
            }
            return value;
        }
        if (parameterType.equals("java.lang.Character") || parameterType.equals("char")) {
            if (parameterValue.length() > 0) {
                return new Character(parameterValue.charAt(0));
            } else {
                throw new NumberFormatException("Can not initialize Character from empty String");
            }
        }
        // tests whether the classes have a single string parameter value
        // constructor. That covers the classes
        // javax.management.ObjectName
        // java.math.BigInteger
        // java.math.BigDecimal

        Class cls = null;
        java.lang.reflect.Constructor ctor = null;
        try {
            cls = Class.forName(parameterType);
            ctor = cls.getConstructor(new Class[]{String.class});
            return ctor.newInstance(new Object[]{parameterValue});
        }
        catch (ClassNotFoundException cnfe) {
            // Can not find class. Not in our ClassLoader?
            /** @todo Ask the MBeanServer to instantiate this class??? */
            throw new IllegalArgumentException("Invalid parameter type: " + parameterType);
        }
        catch (NoSuchMethodException nsme) {
            // No public String constructor.
            throw new IllegalArgumentException("Invalid parameter type: " + parameterType);
        }
        catch (Exception ex) {
            // Constructor might have thrown an exception?
            // Security Exception ?
            // IllegalAccessException? .... etc.
            // Just rethrow. We can do little here <shrug>
            /** @todo Log the exception */
            throw ex;
        }
    }

}
