package com.ef.stateeventslogger;

import com.ef.stateeventslogger.bootstrap.Bootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * The type State Events Logger application.
 */
@SpringBootApplication
public class StateEventsLoggerApplication {
    /**
     * application's starting point.
     *
     * @param args list of command line arguments
     */
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(StateEventsLoggerApplication.class, args);

        Bootstrap bootstrap = applicationContext.getBean(Bootstrap.class);
        bootstrap.initialiseEventConsumers();
    }
}
