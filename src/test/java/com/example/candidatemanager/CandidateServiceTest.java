package com.example.candidatemanager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.candidatemanager.dto.CandidateRequest;
import com.example.candidatemanager.dto.CandidateResponse;
import com.example.candidatemanager.entity.Candidate;
import com.example.candidatemanager.entity.CandidateStatus;
import com.example.candidatemanager.exception.DuplicateResourceException;
import com.example.candidatemanager.repository.CandidateRepository;
import com.example.candidatemanager.service.CandidateService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private CandidateService candidateService;

    @Test
    void createDefaultsMissingStatusAndNormalizesEmail() {
        CandidateRequest request = new CandidateRequest(
                "Ada Lovelace", "Software Engineer", "ADA@EXAMPLE.COM", "+91 9876543210", "Java, SQL", 5, "Remote", null);
        when(candidateRepository.findByEmailIgnoreCase("ada@example.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> {
            Candidate candidate = invocation.getArgument(0);
            candidate.setId(1L);
            return candidate;
        });

        CandidateResponse response = candidateService.create(request);

        ArgumentCaptor<Candidate> captor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ada@example.com");
        assertThat(captor.getValue().getStatus()).isEqualTo(CandidateStatus.APPLIED);
        assertThat(response.status()).isEqualTo(CandidateStatus.APPLIED);
    }

    @Test
    void createRejectsAnExistingEmail() {
        Candidate existingCandidate = new Candidate();
        existingCandidate.setId(1L);
        when(candidateRepository.findByEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(existingCandidate));

        CandidateRequest request = new CandidateRequest(
                "Ada Lovelace", "Software Engineer", "ada@example.com", null, "Java", 5, "Remote", CandidateStatus.APPLIED);

        assertThatThrownBy(() -> candidateService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ada@example.com");
    }
}
