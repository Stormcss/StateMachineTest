package ru.strcss.test.cci.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.transaction.annotation.Transactional;
import ru.strcss.test.cci.dto.Request;
import ru.strcss.test.cci.rqEvents;
import ru.strcss.test.cci.rqStates;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.strcss.test.cci.config.Constants.REQUEST;

/**
 * Created by Stormcss
 * Date: 25.11.2018
 */
@Slf4j
@Transactional
public class UnapprovedRqJobObserver extends QuartzJobBean {

    @Autowired
    Map<Long, StateMachineContext<rqStates, rqEvents>> inMemoryStorage;
    @Autowired
    private StateMachinePersister<rqStates, rqEvents, Long> persister;
    @Autowired
    private StateMachineFactory<rqStates, rqEvents> stateMachineFactory;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("cron executed");
        log.debug("storage: " + inMemoryStorage.values());

        //doing simple filter
        List<Request> endedApprovalRequests = inMemoryStorage.values().stream()
                .map(stateMachine -> stateMachine.getExtendedState().getVariables().get(REQUEST))
                .map(Request.class::cast)
                .filter(request -> request.getStatusId().equals(2L))
                .filter(request -> LocalDateTime.now().isAfter(request.getApproveEndDate()))
                .collect(Collectors.toList());

        for (Request endedApprovalRequest : endedApprovalRequests) {
            StateMachineContext<rqStates, rqEvents> stateMachineContext = inMemoryStorage.get(endedApprovalRequest.getId());
            Long id = ((Request) stateMachineContext.getExtendedState().getVariables().get(REQUEST)).getId();

            log.info("Id {} is found to be unapproved", id);

            try {
                StateMachine<rqStates, rqEvents> stateMachine = persister.restore(stateMachineFactory.getStateMachine(), id);
                stateMachine.sendEvent(rqEvents.eUnapprove);
            } catch (Exception e) {
                log.error("Can not restore!");
                e.printStackTrace();
            }
        }


    }
}
