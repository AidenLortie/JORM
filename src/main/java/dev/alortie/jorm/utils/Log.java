package dev.alortie.jorm.utils;

import java.sql.Time;

public class Log {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";

    private static Log instance;
    private static LogLevel logLevel = LogLevel.NONE;
    private Log() {
    }

    public static Log setInstance(LogLevel level) {
        if (instance != null) {
            throw new IllegalStateException("Log is already initialized.");
        }

        instance = new Log();
        logLevel = level;

        return instance;
    }

    public static Log o() {
        if (instance == null) {
            throw new IllegalStateException("Log is not initialized. Call setInstance() first.");
        }
        return instance;
    }



    public void info(String tag, String message) {
        if (logLevel.ordinal() >= LogLevel.INFO.ordinal()){
            System.out.println(GREEN + "[INFO]" + RESET + " [" + new Time(System.currentTimeMillis()) + "] [" + tag + "] " + message);
        }
    }

    public void error(String tag, String message) {
        if (logLevel.ordinal() >= LogLevel.ERROR.ordinal()){
            System.err.println( RED + "[ERROR]" + RESET +" [" + new Time(System.currentTimeMillis()) + "][" + tag + "] " + message);
        }
    }

    public void debug(String tag, String message) {
        if (logLevel.ordinal() >= LogLevel.DEBUG.ordinal()){
            System.out.println(CYAN + "[DEBUG]" + RESET + " [" + new Time(System.currentTimeMillis()) + "][" + tag + "] " + message);
        }
    }

    public static void i(String tag, String msg) { o().info(tag, msg); }
    public static void e(String tag, String msg) { o().error(tag, msg); }
    public static void d(String tag, String msg) { o().debug(tag, msg); }
}
