package ru.strcss.test.cci.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.concurrent.TimeUnit;

/**
 * Created by Stormcss
 * Date: 25.11.2018
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static void provideLatencyInMs(Long latency) {
        try {
            TimeUnit.MILLISECONDS.sleep(latency);
        } catch (InterruptedException ignored) {
        }
    }

    public static <S, E, T> void persist(StateMachinePersister<S, E, T> persister,
                                         StateMachine<S, E> stateMachine, T contextObj) {
        try {
            persister.persist(stateMachine, contextObj);
        } catch (Exception e) {
            log.error("Can not persist!");
            e.printStackTrace();
        }
    }

}
