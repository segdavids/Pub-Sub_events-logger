package com.ef.stateeventslogger.services.jms.events;

import com.ef.cim.objectmodel.Enums;
import com.ef.stateeventslogger.commons.CommonUtils;
import com.ef.stateeventslogger.commons.Constants;
import com.ef.stateeventslogger.model.AgentStateChangeData;
import com.ef.stateeventslogger.model.AgentStateChangeEntity;
import com.ef.stateeventslogger.repositories.AgentStateChangeEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Agent state changed event.
 */
public class AgentStateChangeEvent extends JmsEvent {
    private static final Logger logger = LoggerFactory.getLogger(AgentStateChangeEvent.class);
    private final AgentStateChangeEventsRepository repository;

    public AgentStateChangeEvent(AgentStateChangeEventsRepository repository) {
        this.repository = repository;
    }


    @Override
    public void saveBatch(List<String> batch) throws JsonProcessingException {
        logger.debug(Constants.METHOD_STARTED);
        if (batch.isEmpty()) {
            logger.debug("Batch is empty, returning..");
            return;
        }

        List<AgentStateChangeEntity> entityList = convertBatchToEntityList(batch);
        logger.debug("Batch converted to entity list");
        List<AgentStateChangeEntity> savedList = this.repository.saveAll(entityList);
        if (savedList.size() != entityList.size()) {
            throw new IllegalStateException("Failed to save batch in DB");
        }
        logger.info("Batch saved in DB");
        logger.debug(Constants.METHOD_ENDED);
    }

    @Override
    public Enums.JmsEventName getName() {
        return Enums.JmsEventName.AGENT_STATE_CHANGED;
    }

    @Override
    public boolean existsInDb(String payload) throws JsonProcessingException {
        JsonNode timestampJson = CommonUtils.getObjectMapper().readTree(payload).get("timeStamp");
        Timestamp timestamp = CommonUtils.getObjectMapper().treeToValue(timestampJson, Timestamp.class);
        return this.repository.existsByTimestamp(timestamp);
    }

    List<AgentStateChangeEntity> convertBatchToEntityList(List<String> batch) throws JsonProcessingException {
        List<AgentStateChangeEntity> entityList = new ArrayList<>();
        for (String payload : batch) {
            entityList.add(getAgentStateChangedEntityFrom(payload));
        }
        return entityList;
    }

    AgentStateChangeEntity getAgentStateChangedEntityFrom(String payload) throws JsonProcessingException {
        JsonNode payloadJson = CommonUtils.getObjectMapper().readTree(payload);

        Enums.JmsEventName name = getNameFrom(payloadJson);
        AgentStateChangeData data = getAgentStateChangedDataFrom(payloadJson);
        Timestamp timestamp = getTimestampFrom(payloadJson);
        String topicId = getTopicIdFrom(payloadJson);

        return new AgentStateChangeEntity(name, data, timestamp, topicId);
    }

    private AgentStateChangeData getAgentStateChangedDataFrom(JsonNode payloadJson) throws JsonProcessingException {
        String dataJsonString = payloadJson.get("data").toString();
        return CommonUtils.getObjectMapper().readValue(dataJsonString, AgentStateChangeData.class);
    }

    private Enums.JmsEventName getNameFrom(JsonNode payloadJson) {
        return Enums.JmsEventName.valueOf(payloadJson.get("name").asText());
    }

    private Timestamp getTimestampFrom(JsonNode payloadJson) throws JsonProcessingException {
        return CommonUtils.getObjectMapper().readValue(payloadJson.get("timestamp").toString(), Timestamp.class);
    }

    private String getTopicIdFrom(JsonNode payloadJson) {
        return payloadJson.get("topicId").asText();
    }
}
