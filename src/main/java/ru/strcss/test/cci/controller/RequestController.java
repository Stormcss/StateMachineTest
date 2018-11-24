package ru.strcss.test.cci.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.strcss.test.cci.dto.Request;
import ru.strcss.test.cci.dto.RequestIncome;
import ru.strcss.test.cci.rqEvents;
import ru.strcss.test.cci.rqStates;

import static ru.strcss.test.cci.config.Constants.REQUEST;
import static ru.strcss.test.cci.rqEvents.eReceive;

/**
 * Created by Stormcss
 * Date: 24.11.2018
 */
@Slf4j
@RestController
@RequestMapping("/api/")
public class RequestController {

    private StateMachineFactory<rqStates, rqEvents> stateMachineFactory;

    public RequestController(StateMachineFactory<rqStates, rqEvents> stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }

    @PostMapping(value = "/register")
    public void registerRequest(@RequestBody RequestIncome incomeRequest) {

        log.info("Received: {}", incomeRequest);
        log.info("Some validations are done...");

        Request request = new Request();
        request.setUserId(incomeRequest.getUserId());
        request.setPrice(incomeRequest.getPrice());


        StateMachine<rqStates, rqEvents> stateMachine = stateMachineFactory.getStateMachine();

        stateMachine.getExtendedState()
                .getVariables()
                .put(REQUEST, request);
        stateMachine.sendEvent(eReceive);
    }
}
