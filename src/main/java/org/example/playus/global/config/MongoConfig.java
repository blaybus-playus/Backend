package org.example.playus.global.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.TimeZone;

@Configuration
@EnableMongoRepositories(basePackages = {
        "org.example.playus.domain.employee",
        "org.example.playus.domain.quest",
        "org.example.playus.domain.board",
        "org.example.playus.domain.project",
        "org.example.playus.domain.evaluation",
        "org.example.playus.domain.employeeExp",
        "org.example.playus.domain.level",
        "org.example.playus.domain.admin"
})
@EnableMongoAuditing
public class MongoConfig {

    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

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

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
