package com.ef.stateeventslogger.services.jms;

import com.ef.cim.objectmodel.Enums;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;

/**
 * This interface describes a message communication service to communicate over a JMS broker.
 *
 * <p>It provides an init() method to initialize a connection with an JMS broker,
 * a single topic subscriber and a single topic publisher on that connection. This way
 * messages on a particular topic can be received and published.
 *
 * <p>It extends the MessageListener interface to listen to messages using the onMessage()
 * method and provides publish() method to send messages on topic.
 *
 * <p>It also provides a stop() method to stop the connection gracefully.
 */
public interface JmsConsumer extends MessageListener, ExceptionListener {
    void initializeFor(@NotNull Enums.JmsEventName eventName) throws JMSException;
}