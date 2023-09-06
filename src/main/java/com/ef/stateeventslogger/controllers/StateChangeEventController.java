package com.ef.stateeventslogger.controllers;

import com.ef.stateeventslogger.repositories.AgentStateChangeEventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type State change event controller.
 */
@RestController
public class StateChangeEventController {
    private final AgentStateChangeEventsRepository repository;

    /**
     * Instantiates a new State change event controller.
     *
     * @param repository the repository
     */
    @Autowired
    public StateChangeEventController(AgentStateChangeEventsRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieve all response entity.
     *
     * @return the response entity
     */
    @GetMapping("/agent-state-change-events")
    public ResponseEntity<Object> retrieveAll() {
        return ResponseEntity.ok().body(this.repository.findAll());
    }
}
