package com.example.candidatemanager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.candidatemanager.entity.Candidate;
import com.example.candidatemanager.entity.CandidateStatus;
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
    void createReturnsCreatedAndDefaultsStatus() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidateJson("ada@example.com", "Java, SQL", 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createRejectsDuplicateEmailsIgnoringCase() throws Exception {
        createCandidate("ada@example.com", "Java", 5, "Remote", CandidateStatus.APPLIED);

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidateJson("ADA@example.com", "Java", 5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void createRejectsInvalidFields() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":" ","email":"not-an-email","phone":"bad","experienceYears":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.experienceYears").exists());
    }

    @Test
    void createRejectsMissingExperience() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Ada Lovelace","email":"ada@example.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.experienceYears").exists());
    }

    @Test
    void createRejectsFieldsThatExceedDatabaseLimits() throws Exception {
        String oversizedName = "a".repeat(256);

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"%s","currentRole":"Software Engineer","email":"ada@example.com","experienceYears":5}
                                """.formatted(oversizedName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists());
    }

    @Test
    void createRejectsMissingRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is required"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void createRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body contains invalid JSON"));
    }

    @Test
    void getReturnsCandidateAndUnknownIdIsNotFound() throws Exception {
        long id = createCandidate("ada@example.com", "Java", 5, "Remote", CandidateStatus.INTERVIEW);

        mockMvc.perform(get("/api/v1/candidates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("INTERVIEW"));

        mockMvc.perform(get("/api/v1/candidates/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateChangesCandidateAndRejectsDuplicateEmail() throws Exception {
        long adaId = createCandidate("ada@example.com", "Java", 5, "Remote", CandidateStatus.APPLIED);
        createCandidate("grace@example.com", "COBOL", 10, "New York", CandidateStatus.SCREENING);

        mockMvc.perform(put("/api/v1/candidates/{id}", adaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidateJson("ada@example.com", "Java, Spring", 6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills").value("Java, Spring"))
                .andExpect(jsonPath("$.experienceYears").value(6))
                .andExpect(jsonPath("$.status").value("APPLIED"));

        mockMvc.perform(put("/api/v1/candidates/{id}", adaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidateJson("grace@example.com", "Java", 5)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateReturnsNotFoundAndValidatesBody() throws Exception {
        mockMvc.perform(put("/api/v1/candidates/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidateJson("ada@example.com", "Java", 5)))
                .andExpect(status().isNotFound());

        long id = createCandidate("ada@example.com", "Java", 5, "Remote", CandidateStatus.APPLIED);
        mockMvc.perform(put("/api/v1/candidates/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists());
    }

    @Test
    void deleteReturnsNoContentAndThenNotFound() throws Exception {
        long id = createCandidate("ada@example.com", "Java", 5, "Remote", CandidateStatus.APPLIED);

        mockMvc.perform(delete("/api/v1/candidates/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/candidates/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchMatchesAnySelectedSkill() throws Exception {
        createCandidate("ada@example.com", "Java, SQL", 5, "Remote", CandidateStatus.APPLIED);
        createCandidate("grace@example.com", "Figma", 10, "New York", CandidateStatus.HIRED);
        createCandidate("linus@example.com", "C", 15, "Helsinki", CandidateStatus.REJECTED);

        mockMvc.perform(get("/api/v1/candidates").param("skill", "java", "figma"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void searchCombinesKeywordSkillExperienceLocationAndStatus() throws Exception {
        createCandidate("ada@example.com", "Java, Spring Boot", 5, "Remote", CandidateStatus.INTERVIEW);
        createCandidate("grace@example.com", "Java, Spring Boot", 5, "Remote", CandidateStatus.APPLIED);
        createCandidate("linus@example.com", "Java, Spring Boot", 5, "Helsinki", CandidateStatus.INTERVIEW);
        createCandidate("margaret@example.com", "Java, Spring Boot", 12, "Remote", CandidateStatus.INTERVIEW);

        mockMvc.perform(get("/api/v1/candidates")
                        .param("q", "ada")
                        .param("skill", "java")
                        .param("minExperience", "3")
                        .param("maxExperience", "7")
                        .param("location", "remote")
                        .param("status", "INTERVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("ada@example.com"));
    }

    @Test
    void searchSupportsPaginationAndSorting() throws Exception {
        createCandidate("charlie@example.com", "Java", 5, "Remote", CandidateStatus.APPLIED);
        createCandidate("ada@example.com", "Java", 5, "Remote", CandidateStatus.APPLIED);
        createCandidate("grace@example.com", "Java", 5, "Remote", CandidateStatus.APPLIED);

        mockMvc.perform(get("/api/v1/candidates")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "email,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].email").value("ada@example.com"));
    }

    @Test
    void searchRejectsInvalidRangesAndParameters() throws Exception {
        mockMvc.perform(get("/api/v1/candidates")
                        .param("minExperience", "8")
                        .param("maxExperience", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("minExperience")));

        mockMvc.perform(get("/api/v1/candidates").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/candidates").param("sort", "unknownField,asc"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/candidates").param("page", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/candidates").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size must be between 1 and 100"));
    }

    private long createCandidate(String email, String skills, int years, String location, CandidateStatus status)
            throws Exception {
        String body = """
                {
                  "fullName": "%s",
                  "currentRole": "Software Engineer",
                  "email": "%s",
                  "phone": "+1 415 555 0101",
                  "skills": "%s",
                  "experienceYears": %d,
                  "location": "%s",
                  "status": "%s"
                }
                """.formatted(nameFor(email), email, skills, years, location, status);
        String response = mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = response.replaceAll("(?s).*?\"id\":(\\d+).*", "$1");
        return Long.parseLong(id);
    }

    private String nameFor(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    private String candidateJson(String email, String skills, int experienceYears) {
        return """
                {
                  "fullName": "%s",
                  "currentRole": "Software Engineer",
                  "email": "%s",
                  "phone": "+1 415 555 0101",
                  "skills": "%s",
                  "experienceYears": %d,
                  "location": "Remote"
                }
                """.formatted(nameFor(email), email, skills, experienceYears);
    }
}
