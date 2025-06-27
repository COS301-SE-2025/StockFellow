package com.stockfellow.groupservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.stockfellow.groupservice.model.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig {
    
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient, MongoMappingContext context) {
        MongoTemplate template = new MongoTemplate(mongoClient, "groups_stokvel_db");
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(context);
        IndexOperations indexOps = template.indexOps(Event.class);
        resolver.resolveIndexFor(Event.class).forEach(indexOps::ensureIndex);
        return template;
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
}