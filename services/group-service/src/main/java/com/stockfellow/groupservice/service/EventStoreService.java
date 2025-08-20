package com.stockfellow.groupservice.service;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventStoreService {
    private static final Logger logger = LoggerFactory.getLogger(EventStoreService.class);
    
    private final EventRepository eventRepository;

    public EventStoreService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Save an event for a specific group and return the event ID
     * 
     * @param groupId The ID of the group this event belongs to
     * @param event The event to save
     * @return The ID of the saved event
     */
    public String saveEvent(String groupId, Event event) {
        // Add the groupId to the event data (matches your existing data structure)
        if (event.getData() == null) {
            event.setData(new HashMap<>());
        }
        
        // Ensure the group ID is stored in the event data for querying
        event.getData().put("groupId", groupId);
        
        Event savedEvent = eventRepository.save(event);
        
        logger.debug("Saved event {} for group {}: {}", 
                    savedEvent.getId(), groupId, savedEvent.getEventType());
        
        return savedEvent.getId();
    }

    /**
     * Create and save an event with the given type and data
     * 
     * @param groupId The ID of the group this event belongs to
     * @param eventType The type of event
     * @param data The event data
     * @return The ID of the saved event
     */
    public String appendEvent(String groupId, String eventType, Map<String, Object> data) {
        Event event = new Event(eventType, data);
        return saveEvent(groupId, event);
    }

    /**
     * Get all events for a specific group ordered by timestamp
     * 
     * @param groupId The ID of the group
     * @return List of events ordered by timestamp
     */
    public List<Event> getEvents(String groupId) {
        return eventRepository.findByGroupIdOrderByTimestampAsc(groupId);
    }

    /**
     * Get all events of a specific type for a group
     * 
     * @param groupId The ID of the group
     * @param eventType The type of events to retrieve
     * @return List of events of the specified type
     */
    public List<Event> getEventsByType(String groupId, String eventType) {
        return eventRepository.findByGroupIdAndEventTypeOrderByTimestampAsc(groupId, eventType);
    }

    /**
     * Get all events across all groups (useful for admin/monitoring)
     * 
     * @return List of all events ordered by timestamp
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAllByOrderByTimestampAsc();
    }

    /**
     * Get events for multiple groups (useful for user dashboards)
     * 
     * @param groupIds List of group IDs
     * @return List of events for all specified groups
     */
    public List<Event> getEventsForGroups(List<String> groupIds) {
        return eventRepository.findByGroupIdInOrderByTimestampAsc(groupIds);
    }

    /**
     * Count total events for a group
     * 
     * @param groupId The ID of the group
     * @return Number of events for the group
     */
    public long countEvents(String groupId) {
        return eventRepository.countByGroupId(groupId);
    }
}