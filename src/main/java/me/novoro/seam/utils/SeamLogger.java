package me.novoro.seam.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seam's Logger. It's not recommended to use this externally.
 */
public class SeamLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("Seam");

    /**
     * Sends an info log to console.
     * @param s The string to log.
     */
    public static void info(String s) {
        SeamLogger.LOGGER.info("{}{}", "[Seam]: ", s);
    }

    /**
     * Sends a warn log to console.
     * @param s The string to log.
     */
    public static void warn(String s) {
        SeamLogger.LOGGER.warn("{}{}", "[Seam]: ", s);
    }

    /**
     * Sends an error log to console.
     * @param s The string to log.
     */
    public static void error(String s) {
        SeamLogger.LOGGER.error("{}{}", "[Seam]: ", s);
    }

    /**
     * Prints a stacktrace using Seam's Logger.
     * @param throwable The exception to print.
     */
    public static void printStackTrace(Throwable throwable) {
        SeamLogger.error(throwable.toString());
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement traceElement : trace) SeamLogger.error("\tat " + traceElement);
    }
}
