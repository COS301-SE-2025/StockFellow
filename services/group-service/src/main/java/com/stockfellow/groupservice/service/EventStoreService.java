   package com.stockfellow.groupservice.service;
   
   import com.stockfellow.groupservice.model.Event;
   import com.stockfellow.groupservice.repository.EventRepository;
   import org.springframework.stereotype.Service;
   
   import java.util.HashMap;
   import java.util.List;
   import java.util.Map;
   
   @Service
   public class EventStoreService {
       private final EventRepository eventRepository;
   
       public EventStoreService(EventRepository eventRepository) {
           this.eventRepository = eventRepository;
       }
   
       public Event appendEvent(String eventType, Map<String, Object> data) {
           Event event = new Event(eventType, data);
           return eventRepository.save(event);
       }
   
       public List<Event> getEvents(String id) {
           return eventRepository.findByUserIdOrGroupIdOrderByTimestampAsc(id);
       }
   }
