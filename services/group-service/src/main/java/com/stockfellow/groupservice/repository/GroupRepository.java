   package com.stockfellow.groupservice.repository;
   
   import com.stockfellow.groupservice.model.Group;
   import org.springframework.data.mongodb.repository.MongoRepository;
   
   import java.util.List;
   
   public interface GroupRepository extends MongoRepository<Group, String> {
       List<Group> findByMemberIdsContaining(String userId);
   }
