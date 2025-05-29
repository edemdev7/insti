package com.payiskoul.institution.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.InetSocketAddress;
import java.net.Socket;

@Configuration
@Slf4j
@Profile({"default","dev","local"})
public class NetworkDiagnostic {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public CommandLineRunner checkNetworkConnectivity() {
        return args -> {
            log.info("Checking network connectivity to Redis at {}:{}", redisHost, redisPort);
            try {
                // Test direct TCP connection
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(redisHost, redisPort), 5000);
                log.info("Successfully connected to Redis via TCP");
                socket.close();
            } catch (Exception e) {
                log.error("Failed to connect to Redis: {}", e.getMessage(), e);
                // Tentative de diagnostic DNS
                try {
                    log.info("Attempting to resolve Redis hostname...");
                    InetSocketAddress address = new InetSocketAddress(redisHost, redisPort);
                    log.info("Redis hostname resolved to: {}", address.getAddress().getHostAddress());
                } catch (Exception e2) {
                    log.error("Failed to resolve Redis hostname: {}", e2.getMessage());
                }
            }
        };
    }
}