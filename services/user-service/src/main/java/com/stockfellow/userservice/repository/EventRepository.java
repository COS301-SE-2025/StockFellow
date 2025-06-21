package com.stockfellow.userservice.repository;

import com.stockfellow.userservice.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    @Query("{'data.userId': ?0}")
    List<Event> findByDataUserIdOrderByTimestampAsc(String userId);
}