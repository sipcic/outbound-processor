package com.example.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JmsConfig {

    @Bean
    public ConnectionFactory jmsConnectionFactory() {
        return new ActiveMQConnectionFactory("tcp://localhost:61616", "admin", "admin");
    }

    @Bean
    public PlatformTransactionManager transactionManager(ConnectionFactory jmsConnectionFactory) {
        return new JmsTransactionManager(jmsConnectionFactory);
    }
}