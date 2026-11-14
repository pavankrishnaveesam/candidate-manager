package com.example.candidatemanager.config;

import com.example.candidatemanager.dto.CandidateRequest;
import com.example.candidatemanager.entity.CandidateStatus;
import com.example.candidatemanager.repository.CandidateRepository;
import com.example.candidatemanager.service.CandidateService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevelopmentDataInitializer {

    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Bean
    CommandLineRunner seedDevelopmentCandidates() {
        return args -> {
            if (!seedEnabled || candidateRepository.count() > 0) {
                return;
            }

            List<CandidateRequest> candidates = List.of(
                    new CandidateRequest("Sophia Chen", "Senior Product Designer", "sophia.chen@example.com", "+1 415 555 0101",
                            "UI/UX, Figma, User Research, Product Strategy", 7, "San Francisco",
                            CandidateStatus.INTERVIEW),
                    new CandidateRequest("Marcus Johnson", "Senior Backend Engineer", "marcus.johnson@example.com", "+1 212 555 0102",
                            "Java, Spring Boot, PostgreSQL, Docker", 5, "New York", CandidateStatus.SCREENING),
                    new CandidateRequest("Aisha Patel", "Frontend Engineer", "aisha.patel@example.com", "+91 98765 43210",
                            "React, TypeScript, UI/UX, Figma", 4, "Hyderabad", CandidateStatus.APPLIED),
                    new CandidateRequest("Daniel Kim", "Platform Engineer", "daniel.kim@example.com", "+1 206 555 0104",
                            "Python, AWS, Kubernetes, PostgreSQL", 10, "Seattle", CandidateStatus.OFFERED),
                    new CandidateRequest("Olivia Garcia", "Product Designer", "olivia.garcia@example.com", "+1 512 555 0105",
                            "Product Design, Figma, Prototyping", 2, "Austin", CandidateStatus.HIRED),
                    new CandidateRequest("Noah Williams", "Staff Software Engineer", "noah.williams@example.com", "+44 20 7946 0106",
                            "Java, Microservices, Kafka, AWS", 8, "London", CandidateStatus.REJECTED),
                    new CandidateRequest("Priya Nair", "UI Engineer", "priya.nair@example.com", "+91 98765 43217",
                            "React, JavaScript, Accessibility, CSS", 3, "Bengaluru", CandidateStatus.INTERVIEW),
                    new CandidateRequest("Ethan Brown", "Data Engineer", "ethan.brown@example.com", "+1 617 555 0108",
                            "Data Engineering, SQL, Spark, Python", 6, "Boston", CandidateStatus.SCREENING)
            );

            candidates.forEach(candidateService::create);
        };
    }
}
