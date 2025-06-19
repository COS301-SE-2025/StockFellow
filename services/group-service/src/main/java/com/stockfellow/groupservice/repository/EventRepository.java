   package com.stockfellow.groupservice.repository;
   
   import com.stockfellow.groupservice.model.Event;
   import org.springframework.data.mongodb.repository.MongoRepository;
   import org.springframework.data.mongodb.repository.Query;
   
   import java.util.List;
   
   public interface EventRepository extends MongoRepository<Event, String> {
       @Query("{$or: [{'data.userId': ?0}, {'data.groupId': ?0}]}")
       List<Event> findByUserIdOrGroupIdOrderByTimestampAsc(String id);
   }
