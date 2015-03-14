package at.rseiler.spbee.demo;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Logs the stored procedure execution durations.
 * <p>
 * If the stored procedure call exceed 200 ms (default value) then the call is logged as WARNING like:
 * <code>n ms sp_name[sp_arguments]#sp_call_count</code>.
 * Example: <code>17 ms sp_get_user[3]#1</code>
 * <p>
 * The default value can be changed with the <code>interceptor.splogger.slow.query.duration</code> property in <code>/spbee.properties</code>.
 * <p>
 * The logger name is: <code>spLogger</code>
 * If TRACE is enabled then every stored procedure call is logged.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public final class SpLogger {

    private static final int SLOW_QUERY_DURATION;
    private static final int SLOW_QUERY_DURATION_DEFAULT = 200;
    private static final AtomicLong ID = new AtomicLong(1);
    private static final String CONFIG = "/spbee.properties";
    private static final Logger LOG = Logger.getLogger("spLogger");

    private static final Map<Object, Long> idTimestampMap = new ConcurrentHashMap<>();

    static {
        int slowQueryDuration = SLOW_QUERY_DURATION_DEFAULT;
        InputStream spLoggerInputStream = null;

        try {
            spLoggerInputStream = SpLogger.class.getResourceAsStream(CONFIG);
            Properties config = new Properties();

            if (spLoggerInputStream != null) {
                config.load(spLoggerInputStream);
                String property = config.getProperty("interceptor.splogger.slow.query.duration");

                if (property.matches("^\\d+$")) {
                    slowQueryDuration = Integer.parseInt(property);
                } else {
                    LOG.error("interceptor.splogger.slow.query.duration in spbee.properties is not an integer.");
                }
            } else {
                LOG.info("spbee.properties doesn't exist. Using default value.");
            }
        } catch (IOException e) {
            LOG.error("Failed to read spbee.properties. Using default value.");
        } finally {
            closeStream(spLoggerInputStream);
        }

        SLOW_QUERY_DURATION = slowQueryDuration;
    }

    /**
     * Logs the invocation of the stored procedure call.
     * This method will be called before the invocation of the stored procedure.
     *
     * @param spName    the name of the stored procedure
     * @param arguments the arguments of the stored procedure
     * @return the ID object to identify the call in the {@link #after} method
     */
    public static Object before(String spName, Object... arguments) {
        long id = ID.getAndIncrement();
        idTimestampMap.put(id, System.currentTimeMillis());

        if (LOG.isTraceEnabled()) {
            LOG.trace(getSpInfo(id, spName, arguments));
        }

        return id;
    }

    /**
     * Logs the execution duration of the stored procedure call.
     * This method will be called after the invocation of the stored procedure.
     *
     * @param id        the ID object
     * @param spName    the name of the stored procedure
     * @param arguments the arguments of the stored procedure
     */
    public static void after(Object id, String spName, Object... arguments) {
        long executionTime = System.currentTimeMillis() - idTimestampMap.get(id);

        if (LOG.isTraceEnabled()) {
            LOG.trace(getSpInfoWithExecutionTime(id, spName, executionTime, arguments));
        }

        if (executionTime > SLOW_QUERY_DURATION) {
            LOG.warn(getSpInfoWithExecutionTime(id, spName, executionTime, arguments));
        }

        idTimestampMap.remove(id);
    }

    private static String getSpInfoWithExecutionTime(Object id, String spName, long executionTime, Object[] arguments) {
        return executionTime + " ms " + getSpInfo(id, spName, arguments);
    }

    private static String getSpInfo(Object id, String spName, Object[] arguments) {
        return spName + Arrays.toString(arguments) + "#" + id;
    }

    private static void closeStream(InputStream spLoggerInputStream) {
        if (spLoggerInputStream != null) {
            try {
                spLoggerInputStream.close();
            } catch (IOException e) {
                LOG.error("Failed to close spLoggerInputStream.", e);
            }
        }
    }

    private SpLogger() {
    }

}
