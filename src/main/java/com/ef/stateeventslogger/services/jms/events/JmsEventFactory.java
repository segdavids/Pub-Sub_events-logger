package com.ef.stateeventslogger.services.jms.events;

import com.ef.cim.objectmodel.Enums;
import com.ef.stateeventslogger.repositories.AgentStateChangeEventsRepository;
import java.util.IdentityHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Jms event factory.
 */
@Service
public class JmsEventFactory {
    private final Map<Enums.JmsEventName, JmsEvent> eventHandlerMap = new IdentityHashMap<>();

    @Autowired
    public JmsEventFactory(AgentStateChangeEventsRepository repository) {
        this.eventHandlerMap.put(Enums.JmsEventName.AGENT_STATE_CHANGED, new AgentStateChangeEvent(repository));
    }

    /**
     * Gets jms event.
     *
     * @param eventName the event name
     * @return the jms event
     */
    public JmsEvent getEventHandler(Enums.JmsEventName eventName) {
        if (!this.eventHandlerMap.containsKey(eventName)) {
            throw new IllegalArgumentException("No event handler found for event name: " + eventName);
        }
        return this.eventHandlerMap.get(eventName);
    }
}
