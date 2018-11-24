package ru.strcss.test.cci.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import ru.strcss.test.cci.dto.Request;
import ru.strcss.test.cci.rqEvents;
import ru.strcss.test.cci.rqStates;

import java.util.concurrent.ThreadLocalRandom;

import static ru.strcss.test.cci.config.Constants.REQUEST;
import static ru.strcss.test.cci.rqEvents.*;
import static ru.strcss.test.cci.rqStates.*;
import static ru.strcss.test.cci.utils.Utils.provideLatencyInMs;

/**
 * Created by Stormcss
 * Date: 24.11.2018
 */
@Slf4j
@Configuration
@EnableStateMachineFactory
public class SimpleStateMachineConfiguration
        extends EnumStateMachineConfigurerAdapter<rqStates, rqEvents> {


    @Bean
    StateMachineListener<rqStates, rqEvents> stateMachineListener() {
        return new StateMachineListener<rqStates, rqEvents>();
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<rqStates, rqEvents> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(stateMachineListener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<rqStates, rqEvents> states)
            throws Exception {

        states
                .withStates()
                .initial(sReceived)
                .state(sSaved, saveRequest())
                .state(sChecked, performChecks())
                .state(sApproved, approveRequest())
                .state(sClosed, closeRequest())
                .end(sClosed);
//                .states(
//                        new HashSet<>(Arrays.asList(sSaved, sChecked, sApproved, sApproved))
//                );

    }

    @Override
    public void configure(
            StateMachineTransitionConfigurer<rqStates, rqEvents> transitions)
            throws Exception {

        transitions.withExternal()
                .source(sReceived).target(sSaved).event(eReceive).and()
                .withExternal()
                .source(sSaved).target(sChecked).event(eCheck).and()
                .withExternal()
                .source(sChecked).target(sApproved).event(eApprove).and()
                .withExternal()
                .source(sApproved).target(sClosed).event(eClose);
    }

    private Action<rqStates, rqEvents> saveRequest() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(1L);

            //some logic
            provideLatencyInMs(500L);

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.info("saved to DB with 1 status: {}", context.getEvent());

            context.getStateMachine().sendEvent(eCheck);
        };
    }

    private Action<rqStates, rqEvents> performChecks() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setId(ThreadLocalRandom.current().nextLong(0, 100000L));
            request.setStatusId(2L);

            //some logic
            provideLatencyInMs(500L);

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.warn("checks are done...: {}", context.getEvent());

            context.getStateMachine().sendEvent(eApprove);
        };
    }

    private Action<rqStates, rqEvents> approveRequest() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(3L);

            //some logic
            provideLatencyInMs(500L);

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.info("Request is approved: {}", context.getEvent());

            context.getStateMachine().sendEvent(eClose);
        };
    }

    private Action<rqStates, rqEvents> closeRequest() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);


            //some logic
            provideLatencyInMs(500L);

            request.setStatusId(4L);
            request.setCloseReason("OK");

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.warn("request is closed...: {}", context.getEvent());

//            context.getStateMachine().sendEvent(eApprove);
        };
    }
}
