package com.ef.stateeventslogger.repositories;

import com.ef.stateeventslogger.model.AgentStateChangeEntity;
import java.sql.Timestamp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface State change event repository.
 */
@Repository
public interface AgentStateChangeEventsRepository extends MongoRepository<AgentStateChangeEntity, String> {
    boolean existsByTimestamp(Timestamp timestamp);
}
