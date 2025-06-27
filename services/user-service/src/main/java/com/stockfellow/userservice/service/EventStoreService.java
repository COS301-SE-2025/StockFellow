package com.stockfellow.userservice.service;

import com.stockfellow.userservice.model.Event;
import com.stockfellow.userservice.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventStoreService {
    @Autowired
    private EventRepository eventRepository;

    public Event appendEvent(String eventType, Map<String, Object> data) {
        Event event = new Event(eventType, data);
        return eventRepository.save(event);
    }

    public List<Event> getEvents(String userId) {
        return eventRepository.findByDataUserIdOrderByTimestampAsc(userId);
    }
}