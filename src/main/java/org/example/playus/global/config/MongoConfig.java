package org.example.playus.global.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {"org.example.playus.domain.employee", "org.example.playus.domain.quest", "org.example.playus.domain.board"})
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String uri;


    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(uri);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "playus-mongo");
    }
}
