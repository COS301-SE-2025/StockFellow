   package com.stockfellow.groupservice.model;
   
   import lombok.Data;
   import org.springframework.data.annotation.Id;
   import org.springframework.data.mongodb.core.mapping.Document;
   
   import java.util.Date;
   import java.util.List;
   
   @Document(collection = "groups")
   @Data
   public class Group {
       @Id
       private String groupId;
       private String adminId;
       private String name;
       private Double minContribution;
       private Integer maxMembers;
       private String description;
       private String profileImage;
       private String visibility;
       private String contributionFrequency;
       private Date contributionDate;
       private String payoutFrequency;
       private Date payoutDate;
       private List<String> memberIds;
   }

