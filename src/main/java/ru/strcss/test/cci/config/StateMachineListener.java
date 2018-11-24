package ru.strcss.test.cci.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

/**
 * Created by Stormcss
 * Date: 24.11.2018
 */
@Slf4j
public class StateMachineListener<S, E> extends StateMachineListenerAdapter<S, E> {

    @Override
    public void stateChanged(State from, State to) {
//        log.info(String.format("State changed from %s to %s%n", from == null ? "none" : from.getId(), to.getId()));
    }
}
