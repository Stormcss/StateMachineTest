package ru.strcss.test.cci.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.web.bind.annotation.*;
import ru.strcss.test.cci.dto.Request;
import ru.strcss.test.cci.dto.RequestIncome;
import ru.strcss.test.cci.rqEvents;
import ru.strcss.test.cci.rqStates;

import java.util.Map;

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

    private Map<Long, StateMachineContext<rqStates, rqEvents>> inMemoryStorage;
    private StateMachineFactory<rqStates, rqEvents> stateMachineFactory;
    private StateMachinePersister<rqStates, rqEvents, Long> persister;

    public RequestController(Map<Long, StateMachineContext<rqStates, rqEvents>> inMemoryStorage,
                             StateMachineFactory<rqStates, rqEvents> stateMachineFactory,
                             StateMachinePersister<rqStates, rqEvents, Long> persister) {
        this.inMemoryStorage = inMemoryStorage;
        this.stateMachineFactory = stateMachineFactory;
        this.persister = persister;
    }

    @PostMapping(value = "/register")
    public void registerRequest(@RequestBody RequestIncome incomeRequest) {

        log.info("Received: {}", incomeRequest);
        log.debug("Some validations are done...");

        Request request = new Request();
        request.setUserId(incomeRequest.getUserId());
        request.setPrice(incomeRequest.getPrice());


        StateMachine<rqStates, rqEvents> stateMachine = stateMachineFactory.getStateMachine();
        log.debug("Created new machine");

        stateMachine.getExtendedState()
                .getVariables()
                .put(REQUEST, request);
        stateMachine.sendEvent(eReceive);
        log.debug("sent event.");
    }

    @GetMapping(value = "/approve/{requestId}")
    public String approveRequest(@PathVariable long requestId) throws Exception {
        StateMachineContext<rqStates, rqEvents> machineContext = inMemoryStorage.get(requestId);

        if (machineContext == null)
            return "Not found";

        log.info("Approve for request {} is received...", requestId);

        Long id = ((Request) machineContext.getExtendedState().getVariables().get(REQUEST)).getId();

        StateMachine<rqStates, rqEvents> stateMachine = persister.restore(stateMachineFactory.getStateMachine(), id);

        stateMachine.sendEvent(rqEvents.eApprove);

        log.info("Approve is applied");
        return "Approved!";
    }
}
