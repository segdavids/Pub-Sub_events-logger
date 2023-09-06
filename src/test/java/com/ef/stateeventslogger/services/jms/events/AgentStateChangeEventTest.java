package com.ef.stateeventslogger.services.jms.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.stateeventslogger.commons.CommonUtils;
import com.ef.stateeventslogger.dto.StateChangeEventDto;
import com.ef.stateeventslogger.model.AgentStateChangeData;
import com.ef.stateeventslogger.model.AgentStateChangeEntity;
import com.ef.stateeventslogger.repositories.AgentStateChangeEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentStateChangeEventTest {
    @Mock
    private AgentStateChangeEventsRepository repository;
    private AgentStateChangeEvent agentStateChangeEvent;

    @BeforeEach
    void setUp() {
        this.agentStateChangeEvent = new AgentStateChangeEvent(repository);
    }

    @Test
    void test_getAgentStateChangedEntityFrom() throws JsonProcessingException {
        String payload = getPayload(new AgentState(Enums.AgentStateName.READY, null));
        AgentStateChangeEntity entity = this.agentStateChangeEvent.getAgentStateChangedEntityFrom(payload);

        assertEquals(Enums.JmsEventName.AGENT_STATE_CHANGED, entity.getName());
        assertEquals("STATE_CHANNEL", entity.getTopicId());
        assertNotEquals(null, entity.getData());
    }

    @Test
    void test_convertBatchToEntityList() throws JsonProcessingException {
        List<String> batch = new ArrayList<>();
        batch.add(getPayload(new AgentState(Enums.AgentStateName.READY, null)));
        batch.add(getPayload(new AgentState(Enums.AgentStateName.NOT_READY, null)));

        AgentStateChangeEvent spy = spy(this.agentStateChangeEvent);
        for (String payload : batch) {
            when(spy.getAgentStateChangedEntityFrom(payload)).thenReturn(mock(AgentStateChangeEntity.class));
        }

        List<AgentStateChangeEntity> entityList = spy.convertBatchToEntityList(batch);
        assertEquals(batch.size(), entityList.size());
    }


    @Test
    void test_saveBatch_doesNothing_when_batchIsEmpty() throws JsonProcessingException {
        List<String> batch = new ArrayList<>();
        this.agentStateChangeEvent.saveBatch(batch);

        verifyNoInteractions(repository);
    }

    @Test
    void test_saveBatch_when_batchSaved_successfully() throws JsonProcessingException {
        List<String> batch = new ArrayList<>();
        batch.add(getPayload(new AgentState(Enums.AgentStateName.READY, null)));
        batch.add(getPayload(new AgentState(Enums.AgentStateName.NOT_READY, null)));

        List<AgentStateChangeEntity> entityList = this.agentStateChangeEvent.convertBatchToEntityList(batch);
        when(this.repository.saveAll(any())).thenReturn(entityList);

        this.agentStateChangeEvent.saveBatch(batch);

        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void test_saveBatch_throwsIllegalStateException_when_failToSaveBatchInDb() throws JsonProcessingException {
        List<String> batch = new ArrayList<>();
        batch.add(getPayload(new AgentState(Enums.AgentStateName.READY, null)));
        batch.add(getPayload(new AgentState(Enums.AgentStateName.NOT_READY, null)));

        List<AgentStateChangeEntity> entityList = new ArrayList<>();

        when(this.repository.saveAll(any())).thenReturn(entityList);

        assertThrows(IllegalStateException.class, () -> this.agentStateChangeEvent.saveBatch(batch));
    }

    @Test
    void test_getName_returns_correctEventName() {
        Enums.JmsEventName eventName = this.agentStateChangeEvent.getName();
        assertEquals(Enums.JmsEventName.AGENT_STATE_CHANGED, eventName);
    }

    @Test
    void test_existsInDb_returns_trueIfEventExistsInDB() throws JsonProcessingException {
        String payload = this.getPayload(new AgentState(Enums.AgentStateName.READY, null));
        when(this.repository.existsByTimestamp(any())).thenReturn(true);

        boolean exists = this.agentStateChangeEvent.existsInDb(payload);

        ArgumentCaptor<Timestamp> captor = ArgumentCaptor.forClass(Timestamp.class);
        verify(this.repository, times(1)).existsByTimestamp(captor.capture());
        verifyNoMoreInteractions(this.repository);

        Timestamp capturedTimestamp = captor.getValue();

        JsonNode timestampJson = CommonUtils.getObjectMapper().readTree(payload).get("timeStamp");
        Timestamp timestamp = CommonUtils.getObjectMapper().treeToValue(timestampJson, Timestamp.class);

        assertEquals(timestamp, capturedTimestamp);
        assertTrue(exists);
    }

    private String getPayload(AgentState agentState) throws JsonProcessingException {
        Enums.JmsEventName name = Enums.JmsEventName.AGENT_STATE_CHANGED;
        AgentStateChangeData data = new AgentStateChangeData(getAgentPresence(agentState), true, null);
        String topicId = "STATE_CHANNEL";

        StateChangeEventDto payload = new StateChangeEventDto(name, data, topicId);
        return CommonUtils.getObjectMapper().writeValueAsString(payload);
    }

    private AgentPresence getAgentPresence(AgentState agentState) {
        return new AgentPresence(new CCUser(), agentState, new ArrayList<>());
    }
}