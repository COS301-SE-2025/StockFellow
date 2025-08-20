package com.stockfellow.groupservice.repository;

import com.stockfellow.groupservice.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    
    // Query by groupId in the event data (works with your existing data structure)
    @Query(value = "{ 'data.groupId': ?0 }", sort = "{ 'timestamp': 1 }")
    List<Event> findByGroupIdOrderByTimestampAsc(String groupId);
    
    @Query(value = "{ 'data.groupId': ?0, 'eventType': ?1 }", sort = "{ 'timestamp': 1 }")
    List<Event> findByGroupIdAndEventTypeOrderByTimestampAsc(String groupId, String eventType);
    
    @Query(value = "{ 'data.groupId': { $in: ?0 } }", sort = "{ 'timestamp': 1 }")
    List<Event> findByGroupIdInOrderByTimestampAsc(List<String> groupIds);
    
    @Query(value = "{ 'data.groupId': ?0 }", count = true)
    long countByGroupId(String groupId);
    
    @Query(value = "{}", sort = "{ 'timestamp': 1 }")
    List<Event> findAllByOrderByTimestampAsc();
    
    // Legacy method for backward compatibility (if you still need it)
    @Query(value = "{ $or: [ { 'data.groupId': ?0 }, { 'data.userId': ?0 } ] }", sort = "{ 'timestamp': 1 }")
    List<Event> findByUserIdOrGroupIdOrderByTimestampAsc(String id);
}