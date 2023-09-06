package com.ef.stateeventslogger.services.jms.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ef.cim.objectmodel.Enums;
import com.ef.stateeventslogger.repositories.AgentStateChangeEventsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JmsEventFactoryTest {
    @Mock
    private AgentStateChangeEventsRepository repository;
    private JmsEventFactory factory;

    @BeforeEach
    void setUp() {
        this.factory = new JmsEventFactory(repository);
    }

    @Test
    void test_getEventHandler_returnsJmsEvent_when_eventNameIsAgentStateChanged() {
        JmsEvent event = this.factory.getEventHandler(Enums.JmsEventName.AGENT_STATE_CHANGED);
        assertEquals(AgentStateChangeEvent.class, event.getClass());
    }

    @Test
    void test_getEventHandler_throwsIllegalArgumentException_when_eventNameIsTaskStateChanged() {
        assertThrows(IllegalArgumentException.class,
                () -> this.factory.getEventHandler(Enums.JmsEventName.TASK_STATE_CHANGED));
    }
}