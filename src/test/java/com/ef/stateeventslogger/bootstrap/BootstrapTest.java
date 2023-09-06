package com.ef.stateeventslogger.bootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Enums;
import com.ef.stateeventslogger.services.jms.JmsConsumer;
import javax.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

@ExtendWith(MockitoExtension.class)
class BootstrapTest {
    @Mock
    private ObjectFactory<JmsConsumer> jmsConsumerFactory;
    private Bootstrap bootstrap;

    @BeforeEach
    void setUp() {
        this.bootstrap = new Bootstrap(jmsConsumerFactory);
    }

    @Test
    void test_initialiseEventConsumers() throws JMSException {
        JmsConsumer jmsConsumer = mock(JmsConsumer.class);
        when(jmsConsumerFactory.getObject()).thenReturn(jmsConsumer);
        this.bootstrap.initialiseEventConsumers();
        verify(jmsConsumer, times(1)).initializeFor(Enums.JmsEventName.AGENT_STATE_CHANGED);
        verifyNoMoreInteractions(jmsConsumer);
    }
}