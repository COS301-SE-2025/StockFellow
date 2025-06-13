   package com.stockfellow.groupservice.service;
   
   import com.stockfellow.groupservice.model.Event;
   import com.stockfellow.groupservice.model.Group;
   import com.stockfellow.groupservice.repository.GroupRepository;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.stereotype.Service;
   
   import java.util.*;
   
   @Service
   public class ReadModelService {
       private static final Logger logger = LoggerFactory.getLogger(ReadModelService.class);
       private final EventStoreService eventStoreService;
       private final GroupRepository groupRepository;
   
       public ReadModelService(EventStoreService eventStoreService, GroupRepository groupRepository) {
           this.eventStoreService = eventStoreService;
           this.groupRepository = groupRepository;
       }
   
       public void rebuildState(String groupId) {
           List<Event> events = eventStoreService.getEvents(groupId);
           Group groupData = null;
   
           for (Event event : events) {
               Map<String, Object> data = event.getData();
               if ("GroupCreated".equals(event.getEventType())) {
                   groupData = new Group();
                   groupData.setGroupId((String) data.get("groupId"));
                   groupData.setAdminId((String) data.get("adminId"));
                   groupData.setName((String) data.get("name"));
                   groupData.setMinContribution(((Number) data.get("minContribution")).doubleValue());
                   groupData.setMaxMembers((Integer) data.get("maxMembers"));
                   groupData.setDescription((String) data.get("description"));
                   groupData.setProfileImage((String) data.get("profileImage"));
                   groupData.setVisibility((String) data.get("visibility"));
                   groupData.setContributionFrequency((String) data.get("contributionFrequency"));
                   String contributionDateStr = (String) data.get("contributionDate");
                   groupData.setContributionDate(contributionDateStr != null ? new Date(contributionDateStr) : null);
                   groupData.setPayoutFrequency((String) data.get("payoutFrequency"));
                   String payoutDateStr = (String) data.get("payoutDate");
                   groupData.setPayoutDate(payoutDateStr != null ? new Date(payoutDateStr) : null);
                   groupData.setMemberIds((List<String>) data.getOrDefault("memberIds", new ArrayList<>()));
               } else if ("MemberAdded".equals(event.getEventType())) {
                   if (groupData != null) {
                       List<String> memberIds = new ArrayList<>(groupData.getMemberIds());
                       memberIds.add((String) data.get("userId"));
                       groupData.setMemberIds(new ArrayList<>(new LinkedHashSet<>(memberIds))); // Remove duplicates
                       groupData.setMaxMembers(Math.max(memberIds.size(), groupData.getMaxMembers()));
                   }
               }
           }
   
           if (groupData != null) {
               groupRepository.save(groupData);
               logger.info("Rebuilt state for group {}", groupId);
           }
       }
   
       public Optional<Group> getGroup(String groupId) {
           return groupRepository.findById(groupId);
       }
   
       public List<Group> getAllGroups() {
           return groupRepository.findAll();
       }
   
       public List<Group> getUserGroups(String userId) {
           return groupRepository.findByMemberIdsContaining(userId);
       }
   }

