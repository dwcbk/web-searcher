package com.wework.websitesearcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple stopwatch service that you can start and stop to get an elapsed time. Uses {@link System#nanoTime()}.
 */
public class Stopwatch {
    private static final Logger LOG = LoggerFactory.getLogger(Stopwatch.class);

    private long startTime;
    private long endTime;

    public void start() {
        startTime = System.nanoTime();
        endTime = -1L;
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    /**
     * Return the elapsed time (in nanoseconds)
     * @return
     */
    public long getElapsedTime() {
        return (endTime >= 0) ? (endTime - startTime) : 0L;
    }

    public void printElapsedTime() {
        System.out.println("Elapsed time: " + toHumanFormat(getElapsedTime()));
        LOG.info("Elapsed time: " + toHumanFormat(getElapsedTime()));
    }

    /**
     * Convert a nanosecond time unit to a human-readable form.
     *
     * @param nanos
     * @return
     */
    public static String toHuman(long nanos) {
        long micros = nanos / 1000;
        String human;
        if (micros > 1000000) {
            final long millis = micros / 1000;
            human = Long.toString(millis / 1000) + "." + String.format("%03d", (millis % 1000)) + "s";
        } else if (micros > 1000) {
            human = Long.toString(micros / 1000) + "." + String.format("%03d", (micros % 1000)) + "ms";
        } else {
            human = Long.toString(micros) + "us";
        }
        return human;
    }

    private String toHumanFormat(long nanos) {
        return Stopwatch.toHuman(nanos);
    }
}
