package ru.strcss.test.cci;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {


//    @Autowired
//    private StateMachine<rqStates, rqEvents> stateMachine;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... args) throws Exception {
        log.info("New CCI has started");
        //        stateMachine.sendEvent(rqEvents.eReceive);
//        stateMachine.sendEvent(Events.E2);
    }
}
