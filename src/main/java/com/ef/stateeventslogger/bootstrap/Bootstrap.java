package com.ef.stateeventslogger.bootstrap;

import com.ef.cim.objectmodel.Enums;
import com.ef.stateeventslogger.services.jms.JmsConsumer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Bootstrap.
 */
@Service
public class Bootstrap {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    private final ObjectFactory<JmsConsumer> jmsConsumerFactory;

    @Autowired
    public Bootstrap(ObjectFactory<JmsConsumer> jmsConsumerFactory) {
        this.jmsConsumerFactory = jmsConsumerFactory;
    }

    /**
     * Subscribes to state change Events JMS Topic to communicate state change with Agent-Manager.
     */
    public void initialiseEventConsumers() {
        try {
            this.getJmsConsumerInstance().initializeFor(Enums.JmsEventName.AGENT_STATE_CHANGED);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private JmsConsumer getJmsConsumerInstance() {
        return this.jmsConsumerFactory.getObject();
    }
}
