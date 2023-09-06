package com.ef.stateeventslogger.services.jms.events;


import com.ef.cim.objectmodel.Enums;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;

/**
 * The interface Jms event.
 */
public abstract class JmsEvent {
    private boolean consumerInitialised = false;

    public abstract void saveBatch(List<String> batch) throws JsonProcessingException;

    /**
     * Exists in db.
     *
     * @param payload the payload
     */
    public abstract boolean existsInDb(String payload) throws JsonProcessingException;

    public abstract Enums.JmsEventName getName();

    public final boolean isConsumerInitialised() {
        return consumerInitialised;
    }

    public final void setConsumerInitialisedTrue() {
        this.consumerInitialised = true;
    }
}
