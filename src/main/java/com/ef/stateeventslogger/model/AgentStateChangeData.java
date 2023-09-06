package com.ef.stateeventslogger.model;

import com.ef.cim.objectmodel.AgentPresence;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * An AgentStateChangeData object is used as a DTO to consume the payload from topic
 * when changes in the Agent / Agent-MRD states.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AgentStateChangeData implements Serializable {
    /**
     * Contains the information of the Agent and its states.
     */
    private AgentPresence agentPresence;
    /**
     * True if and only if the Agent-State is changed.
     */
    private boolean agentStateChanged = false;
    /**
     * The Mrd state changes.
     */
    private List<String> mrdStateChanges = new ArrayList<>();
}