package ru.strcss.test.cci;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stormcss
 * Date: 25.11.2018
 */
@Configuration
public class Storage {

    private Map<Long, StateMachineContext<rqStates, rqEvents>> storage = new HashMap<>(32);

    @Bean
    Map<Long, StateMachineContext<rqStates, rqEvents>> inMemoryStorage() {
        return storage;
    }
}
