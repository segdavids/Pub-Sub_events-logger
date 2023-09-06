package com.ef.stateeventslogger.model;

import com.ef.cim.objectmodel.Enums;
import java.sql.Timestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The type State change event.
 */
@Document(value = "agentStateChangeEvents")
public class AgentStateChangeEntity {
    /**
     * The ID.
     */
    @Id
    private String id;
    /**
     * The Name.
     */
    private Enums.JmsEventName name;
    /**
     * The Data.
     */
    private AgentStateChangeData data;
    /**
     * The Timestamp.
     */
    @Indexed
    private Timestamp timestamp;
    /**
     * The Topic id.
     */
    private String topicId;

    public AgentStateChangeEntity() {

    }

    /**
     * Instantiates a new State change event.
     */
    public AgentStateChangeEntity(Enums.JmsEventName name, AgentStateChangeData data,
                                  Timestamp timestamp, String topicId) {
        this.name = name;
        this.data = data;
        this.timestamp = timestamp;
        this.topicId = topicId;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public Enums.JmsEventName getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(Enums.JmsEventName name) {
        this.name = name;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public AgentStateChangeData getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(AgentStateChangeData data) {
        this.data = data;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets topic id.
     *
     * @return the topic id
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Sets topic id.
     *
     * @param topicId the topic id
     */
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    @Override
    public String toString() {
        return "AgentStateChangeEntity{"
                + "id='" + id + '\''
                + ", name=" + name
                + ", data=" + data
                + ", timestamp=" + timestamp
                + ", topicId='" + topicId + '\''
                + '}';
    }
}
