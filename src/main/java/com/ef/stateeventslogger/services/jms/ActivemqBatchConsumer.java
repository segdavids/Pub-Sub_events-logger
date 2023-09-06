package com.ef.stateeventslogger.services.jms;

import com.ef.cim.objectmodel.Enums;
import com.ef.stateeventslogger.commons.Constants;
import com.ef.stateeventslogger.services.jms.events.JmsEvent;
import com.ef.stateeventslogger.services.jms.events.JmsEventFactory;
import java.util.ArrayList;
import java.util.List;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An implementation of the JmsCommunicator interface. Handles message communication
 * for a particular customer between other CIM microservices and this microservice over
 * an activemq broker.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ActivemqBatchConsumer implements JmsConsumer {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(ActivemqBatchConsumer.class);

    /**
     * The Connection.
     */
    private final Connection connection;
    /**
     * The Subscriber session.
     */
    private Session subscriberSession;
    /**
     * The Subscriber.
     */
    private MessageConsumer subscriber;
    /**
     * The Jms event factory.
     */
    private final JmsEventFactory jmsEventFactory;
    /**
     * The Jms event.
     */
    private JmsEvent jmsEvent;
    /**
     * The Topic name.
     */
    @Value("${topic-name}")
    private String topicName;
    /**
     * The constant SUBSCRIBER_NAME.
     */
    private String subscriberName;
    /**
     * The Message selector.
     */
    private String messageSelector;
    /**
     * The Max batch size.
     */
    @Value("${batch-size}")
    private int batchSize;
    /**
     * The Batch counter.
     */
    private int batchCounter;
    /**
     * The Batch.
     */
    private final List<String> batch = new ArrayList<>();

    /**
     * Constructor
     *
     * <p>sets the activemq properties taken from the application-prod.properties
     *
     * <p>Sets the broker_url for the amq connection using the activemq's
     * 'failover' transport
     * //* @param amqProperties carries all the system defined activemq properties.
     *
     * @param connection the connection
     * @throws JMSException the jms exception
     */
    @Autowired
    public ActivemqBatchConsumer(@NotNull Connection connection,
                                 @NotNull JmsEventFactory jmsEventFactory) throws JMSException {
        this.connection = connection;
        this.connection.setExceptionListener(this);
        this.jmsEventFactory = jmsEventFactory;
    }

    /**
     * Initialize.
     *
     * @param eventName the jms event
     * @throws JMSException the jms exception
     */
    @Override
    public void initializeFor(@NotNull Enums.JmsEventName eventName) throws JMSException {
        this.jmsEvent = jmsEventFactory.getEventHandler(Enums.JmsEventName.AGENT_STATE_CHANGED);

        if (jmsEvent == null) {
            throw new IllegalArgumentException("Could not find event instance for " + eventName);
        }

        if (jmsEvent.isConsumerInitialised()) {
            throw new IllegalArgumentException("A consumer is already initialised for event: " + eventName);
        }

        this.messageSelector = "JMSType = '" + eventName.name() + "'";
        this.subscriberName = eventName.name() + "_EVENT_LOGGER_SUBSCRIBER";

        this.initialiseSubscriber();
    }

    private void initialiseSubscriber() throws JMSException {
        try {
            this.setSubscriber();
            this.jmsEvent.setConsumerInitialisedTrue();
            logger.info("Connection, subscriber successfully initialized for topic '{}'", this.topicName);
        } catch (JMSException jmsException) {
            logger.error("JMSException, trying to close connection and sessions...");
            this.stop();
            throw jmsException;
        }
    }

    /**
     * Stop.
     *
     * @throws JMSException the jms exception
     */
    private void stop() throws JMSException {
        logger.debug(Constants.METHOD_STARTED);
        if (subscriber != null) {
            subscriber.close();
            this.subscriber = null;
            logger.debug("Subscriber closed");
        }
        if (subscriberSession != null) {
            this.subscriberSession.unsubscribe(subscriberName);
            subscriberSession.close();
            this.subscriberSession = null;
            logger.debug("SubscriberSession closed");
        }

        logger.info("Communication stopped successfully on Topic: '{}'", this.topicName);
        logger.debug(Constants.METHOD_ENDED);
    }

    @Override
    public void onMessage(Message message) {
        logger.debug(Constants.METHOD_STARTED);
        try {
            logger.info("Jms event: '{}' received on topic: '{}'", message.getJMSType(), topicName);

            String payload = ((TextMessage) message).getText();

            if (message.getJMSRedelivered() && this.jmsEvent.existsInDb(payload)) {
                logger.debug("Redelivered message exists in DB .. ignoring message");
                batchCounter++;
                return;
            }

            this.addToBatch(payload);

            if (batchCounter >= batchSize) {
                this.jmsEvent.saveBatch(batch);
                subscriberSession.commit();
                this.clearBatch();
            }

            logger.info("Jms event: '{}' handled gracefully on topic: '{}'", message.getJMSType(), topicName);
        } catch (Exception e) {
            logger.error("Exception while handling JMS Message: ", e);
            this.clearBatch();
            this.rollBackSession();
        }
        logger.debug(Constants.METHOD_ENDED);
    }

    /**
     * Add to batch.
     *
     * @param payload the payload
     */
    private void addToBatch(String payload) {
        this.batch.add(payload);
        batchCounter++;
    }

    /**
     * Clear batch.
     */
    private void clearBatch() {
        this.batch.clear();
        this.batchCounter = 0;
    }

    /**
     * Roll back session.
     */
    private void rollBackSession() {
        try {
            this.subscriberSession.rollback();
        } catch (JMSException e) {
            logger.error("Exception while roll-backing the session: ", e);
        }
    }

    @Override
    public synchronized void onException(JMSException ex) {
        logger.debug(Constants.METHOD_STARTED);
        logger.error("JMSException: ", ex);
        logger.debug(Constants.METHOD_ENDED);
    }

    /**
     * Used by the init() method to create a new topic subscriber on the connection.
     * Creates a 'durable subscriber'. Uses MessageListener's onMessage method to receive messages.
     *
     * @throws JMSException exception
     */
    private void setSubscriber() throws JMSException {
        logger.debug("Method started for Topic: '{}'", this.topicName);

        this.subscriberSession = this.connection.createSession(true, Session.SESSION_TRANSACTED);
        logger.debug("Session created successfully");

        Topic destination = this.subscriberSession.createTopic(this.topicName);
        logger.debug("Topic Destination created successfully");

        this.subscriber = subscriberSession.createDurableSubscriber(destination, subscriberName,
                messageSelector, true);
        logger.debug("Durable subscriber created successfully");

        this.subscriber.setMessageListener(this);
        logger.debug("Method ended for Topic: '{}'", this.topicName);
    }
}