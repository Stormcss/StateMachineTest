package ru.strcss.test.cci.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import ru.strcss.test.cci.dto.Request;
import ru.strcss.test.cci.rqEvents;
import ru.strcss.test.cci.rqStates;

import java.util.concurrent.Executors;
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

    private Action<rqStates, rqEvents> logIgnoredAction;
    private Action<rqStates, rqEvents> logValidatedAction;
    private Action<rqStates, rqEvents> closeRequestAction;
    private Action<rqStates, rqEvents> approveRequestAction;
    private Action<rqStates, rqEvents> processRequestAction;
    private Action<rqStates, rqEvents> unApproveRequestAction;
    private Action<rqStates, rqEvents> canceledAction;

    SimpleStateMachineConfiguration(Action<rqStates, rqEvents> logIgnoredAction, Action<rqStates, rqEvents> logValidatedAction,
                                    Action<rqStates, rqEvents> closeRequestAction, Action<rqStates, rqEvents> approveRequestAction,
                                    Action<rqStates, rqEvents> processRequestAction, Action<rqStates, rqEvents> unApproveRequestAction,
                                    Action<rqStates, rqEvents> canceledAction) {
        this.logIgnoredAction = logIgnoredAction;
        this.logValidatedAction = logValidatedAction;
        this.closeRequestAction = closeRequestAction;
        this.approveRequestAction = approveRequestAction;
        this.unApproveRequestAction = unApproveRequestAction;
        this.processRequestAction = processRequestAction;
        this.canceledAction = canceledAction;
    }

    @Bean
    StateMachineListener<rqStates, rqEvents> stateMachineListener() {
        return new StateMachineListener<>();
    }

    @Bean
    public StateMachinePersist<rqStates, rqEvents, Long> inMemoryPersist() {
        return new InMemoryPersist();
    }

    @Bean
    public StateMachinePersister<rqStates, rqEvents, Long> persister(
            StateMachinePersist<rqStates, rqEvents, Long> defaultPersist) {

        return new DefaultStateMachinePersister<>(defaultPersist);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<rqStates, rqEvents> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .taskExecutor(new ConcurrentTaskExecutor(Executors.newFixedThreadPool(5)))
                .listener(stateMachineListener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<rqStates, rqEvents> states)
            throws Exception {

        states
                .withStates()
                .initial(sInitial)
                .junction(sReceived)
//                .states(
//                        new HashSet<>(Arrays.asList(sValidated, sApproved, sUnapproved, sProccessing, sCanceled, sClosed, sIgnored))
//                )
//                .states(
//                        new HashSet<>(Arrays.asList(rqStates.values()))
//                )
                .state(sValidated, logValidatedAction)
                .state(sApproved, approveRequestAction)
                .state(sUnapproved, unApproveRequestAction)
                .state(sProccessing, processRequestAction)
                .state(sCanceled, canceledAction)
                .state(sClosed, closeRequestAction)
                .state(sIgnored, logIgnoredAction)
                .end(sFinished);
    }

    @Override
    public void configure(
            StateMachineTransitionConfigurer<rqStates, rqEvents> transitions)
            throws Exception {

        transitions
                .withExternal()
                .source(sInitial).target(sReceived).event(eReceive).action(saveRequest()).and() // 0 -> 1
                .withJunction()
                .source(sReceived)
                .first(sValidated, performValidation()) // 1 -> 2
                .then(sIgnored, ignoreRequest())  // 1 -> 9
                // fixme we could remove method 'ignoreRequest' invocation and process ignore logic as state is changed (methid logIgnore)
                .and()
                .withExternal()
                .source(sValidated).target(sApproved).event(eApprove)/*.action(approveRequestAction)*/.and() // 2 -> 3
                .withExternal()
                .source(sValidated).target(sUnapproved).event(eUnapprove).and() // 2 -> 4
                .withExternal()
                .source(sUnapproved).target(sCanceled).event(eCancel).and() // 4 -> 6
                .withExternal()
                .source(sApproved).target(sProccessing).event(eProccess).and() // 3 -> 5
                .withExternal()
                .source(sProccessing).target(sClosed).event(eClose).and() // 5 -> 7
                .withExternal()
                .source(sClosed).target(sFinished).event(eFinish).and() // finish 7
                .withExternal()
                .source(sCanceled).target(sFinished).event(eFinish).and() // finish 6
                .withExternal()
                .source(sIgnored).target(sFinished).event(eFinish); // finish 9

    }

    private Action<rqStates, rqEvents> saveRequest() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(1L);
            request.setId(ThreadLocalRandom.current().nextLong(0, 10000));

            //some logic
            provideLatencyInMs(1000L);

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.info("saved to DB with 1 status: {}", context.getEvent());
        };
    }


    @Bean
    public Guard<rqStates, rqEvents> performValidation() {
        return context -> {
            // request is got from context for being further validated
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            //some logic
            provideLatencyInMs(1000L);

            log.warn("checks are done...: current event - {}", context.getEvent());

            boolean b = ThreadLocalRandom.current().nextBoolean();
            System.out.println("random validated = " + b);
            return b;
        };
    }

    @Bean
    public Guard<rqStates, rqEvents> ignoreRequest() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(9L);
            request.setCloseReason("NOT VALIDATED!!!");

            //some logic
            provideLatencyInMs(500L);

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.warn("Request is ignored! event - {}", context.getEvent());

            return true; // FIXME: 25.11.2018 Actually this is not the way it was designed for
        };
    }


}
