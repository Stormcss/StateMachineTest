package ru.strcss.test.cci.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import ru.strcss.test.cci.rqEvents;
import ru.strcss.test.cci.rqStates;

import java.util.Map;

/**
 * Created by Stormcss
 * Date: 25.11.2018
 */
public class InMemoryPersist implements StateMachinePersist<rqStates, rqEvents, Long> {

    @Autowired
    Map<Long, StateMachineContext<rqStates, rqEvents>> inMemoryStorage;

    @Override
    public void write(StateMachineContext<rqStates, rqEvents> context,
                      Long contextObj) {
        inMemoryStorage.put(contextObj, context);
    }

    @Override
    public StateMachineContext<rqStates, rqEvents> read(Long contextObj) {
        return inMemoryStorage.get(contextObj);
    }
}