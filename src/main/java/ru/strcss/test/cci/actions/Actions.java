package ru.strcss.test.cci.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.persist.StateMachinePersister;
import ru.strcss.test.cci.dto.Request;
import ru.strcss.test.cci.rqEvents;
import ru.strcss.test.cci.rqStates;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static ru.strcss.test.cci.config.Constants.REQUEST;
import static ru.strcss.test.cci.rqEvents.*;
import static ru.strcss.test.cci.utils.Utils.persist;
import static ru.strcss.test.cci.utils.Utils.provideLatencyInMs;

/**
 * Created by Stormcss
 * Date: 25.11.2018
 */
@Slf4j
@Configuration
public class Actions {

    @Autowired
    private StateMachinePersister<rqStates, rqEvents, Long> persister;

    @Bean
    Action<rqStates, rqEvents> closeRequestAction() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            //some logic
            provideLatencyInMs(500L);

            request.setStatusId(7L);
            request.setCloseReason("OK");

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.warn("request is closed...: {}", context.getEvent());
        };
    }

    @Bean
    Action<rqStates, rqEvents> logValidatedAction() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(2L);

            persist(persister, context.getStateMachine(), request.getId());

            request.setApproveEndDate(LocalDateTime.now().plus(15, ChronoUnit.SECONDS));
            log.warn("request '{}' is validated.: event - {}, state - {}", request, context.getEvent(),
                    context.getStateMachine().getState().getId());
        };
    }

    @Bean
    Action<rqStates, rqEvents> logIgnoredAction() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(9L);

            log.warn("request '{}' is Ignored! event - {}, state - {}", request, context.getEvent(),
                    context.getStateMachine().getState().getId());
        };
    }

    @Bean
    Action<rqStates, rqEvents> approveRequestAction() {
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

            log.info("Request is approved... Received event - {}", context.getEvent());

            context.getStateMachine().sendEvent(eProccess);
        };
    }

    @Bean
    Action<rqStates, rqEvents> unApproveRequestAction() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(4L);

            //some logic
            provideLatencyInMs(500L);

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.info("Request is unapproved... Received event - {}", context.getEvent());

            context.getStateMachine().sendEvent(eCancel);
        };
    }

    @Bean
    Action<rqStates, rqEvents> processRequestAction() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(5L);

            //some logic
            provideLatencyInMs(500L);

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.info("Request is being processed... Received event - {}", context.getEvent());

            context.getStateMachine().sendEvent(eClose);
        };
    }

    @Bean
    Action<rqStates, rqEvents> canceledAction() {
        return context -> {
            Request request = (Request) context.getExtendedState()
                    .getVariables()
                    .get(REQUEST);

            request.setStatusId(6L);

            //some logic
            provideLatencyInMs(500L);

            context.getExtendedState()
                    .getVariables()
                    .put(REQUEST, request);

            log.info("Request is canceled... Received event - {}", context.getEvent());

            context.getStateMachine().sendEvent(eFinish);
        };
    }
}
