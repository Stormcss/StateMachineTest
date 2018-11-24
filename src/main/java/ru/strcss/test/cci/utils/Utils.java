package ru.strcss.test.cci.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Stormcss
 * Date: 25.11.2018
 */
public class Utils {
    public static void provideLatencyInMs(Long latency) {
        try {
            TimeUnit.MILLISECONDS.sleep(latency);
        } catch (InterruptedException ignored) {
        }
    }
}
