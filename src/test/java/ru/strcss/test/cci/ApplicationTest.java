package ru.strcss.test.cci;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.strcss.test.cci.dto.Request;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.strcss.test.cci.config.Constants.REQUEST;
import static ru.strcss.test.cci.rqEvents.eReceive;
import static ru.strcss.test.cci.rqStates.*;
import static ru.strcss.test.cci.utils.Utils.provideLatencyInMs;

/**
 * Created by Stormcss
 * Date: 25.11.2018
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ApplicationTest {

    private StateMachine<rqStates, rqEvents> stateMachine;

    @Autowired
    private StateMachineFactory<rqStates, rqEvents> stateMachineFactory;

    @BeforeEach
    void setUp() {
        stateMachine = stateMachineFactory.getStateMachine();
    }

    /**
     * Checking that stateMachine is correctly injected
     */
    @Test
    void initTest() {
        assertThat(stateMachine.getState().getId(), equalTo(sReceived));

        assertNotNull(stateMachine);
    }

    /**
     * Checking that stateMachine comes to terminate status
     */
    @Test
    void shouldChangeStatuses() {
        Request request = new Request();

        stateMachine.getExtendedState()
                .getVariables()
                .put(REQUEST, request);
        stateMachine.sendEvent(eReceive);

        provideLatencyInMs(3000L);

        List<rqStates> collect = stateMachine.getStates().stream().map(State::getId).collect(Collectors.toList());
        Request finishedRequest = (Request) stateMachine.getExtendedState().getVariables().get(REQUEST);


        System.out.println("finishedRequest = " + finishedRequest);
        assertThat(stateMachine.getState().getId(), equalTo(sClosed));
        assertThat(collect, containsInAnyOrder(sReceived, sSaved, sChecked, sApproved, sClosed));
        assertNotNull(finishedRequest.getId());
        assertThat(finishedRequest.getStatusId(), equalTo(4L));
        assertThat(finishedRequest.getCloseReason(), equalTo("OK"));
    }

//    /**
//     * Checking that stateMachine won't perform incorrect transition
//     */
//    @Test
//    public void testWrongWay() {
//        // Arrange
//        // Act
//        stateMachine.sendEvent(rqEvents.eReceive);
//        stateMachine.sendEvent(rqEvents.eClose);
//        // Asserts
//        Assertions.assertThat(stateMachine.getState().getId())
//                .isEqualTo(States.IN_PROGRESS);
//    }
}