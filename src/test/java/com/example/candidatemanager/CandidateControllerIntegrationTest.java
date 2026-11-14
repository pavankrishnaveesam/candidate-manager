package com.example.candidatemanager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.candidatemanager.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CandidateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateRepository candidateRepository;

    @BeforeEach
    void clearCandidates() {
        candidateRepository.deleteAll();
    }

    @Test
    void createDefaultsStatusAndReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidateJson("ada@example.com", "Java, SQL", 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void createRejectsDuplicateEmailsIgnoringCase() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(candidateJson("ada@example.com", "Java", 5)));

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidateJson("ADA@example.com", "Java", 5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void createRejectsMissingExperienceYears() throws Exception {
        String request = """
                {
                  "fullName": "Ada Lovelace",
                  "email": "ada@example.com",
                  "skills": "Java"
                }
                """;

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.experienceYears").exists());
    }

    @Test
    void searchFiltersBySkill() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(candidateJson("ada@example.com", "Java, SQL", 5)));
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(candidateJson("grace@example.com", "COBOL", 10)));

        mockMvc.perform(get("/api/v1/candidates").param("skill", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("ada@example.com"));
    }

    @Test
    void searchMatchesAnySelectedSkill() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(candidateJson("ada@example.com", "Java, SQL", 5)));
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(candidateJson("grace@example.com", "Figma", 10)));
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(candidateJson("linus@example.com", "C", 15)));

        mockMvc.perform(get("/api/v1/candidates")
                        .param("skill", "java", "figma"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    private String candidateJson(String email, String skills, int experienceYears) {
        return """
                {
                  "fullName": "Ada Lovelace",
                  "email": "%s",
                  "phone": "+91 9876543210",
                  "skills": "%s",
                  "experienceYears": %d,
                  "location": "Remote"
                }
                """.formatted(email, skills, experienceYears);
    }
}
