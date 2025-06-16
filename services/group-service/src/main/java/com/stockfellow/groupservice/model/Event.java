   package com.stockfellow.groupservice.model;
   
   import lombok.Data;
   import org.springframework.data.annotation.Id;
   import org.springframework.data.mongodb.core.index.Indexed;
   import org.springframework.data.mongodb.core.mapping.Document;
   
   import java.util.Date;
   import java.util.Map;
   
   @Document(collection = "events")
   @Data
   public class Event {
       @Id
       private String id;
       private String eventType;
       private Map<String, Object> data;
       private Date timestamp;
   
       public Event() {
           this.timestamp = new Date();
       }
   
       public Event(String eventType, Map<String, Object> data) {
           this.eventType = eventType;
           this.data = data;
           this.timestamp = new Date();
       }
   }

