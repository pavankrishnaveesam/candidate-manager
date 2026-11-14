package com.example.candidatemanager.service;

import com.example.candidatemanager.dto.CandidateRequest;
import com.example.candidatemanager.dto.CandidateResponse;
import com.example.candidatemanager.entity.Candidate;
import com.example.candidatemanager.entity.CandidateStatus;
import com.example.candidatemanager.exception.DuplicateResourceException;
import com.example.candidatemanager.exception.ResourceNotFoundException;
import com.example.candidatemanager.repository.CandidateRepository;
import com.example.candidatemanager.specification.CandidateSpecification;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateRepository candidateRepository;

    @Transactional
    public CandidateResponse create(CandidateRequest request) {
        String email = normalizeEmail(request.email());
        candidateRepository.findByEmailIgnoreCase(email).ifPresent(candidate -> {
            throw new DuplicateResourceException("A candidate with email " + email + " already exists");
        });

        Candidate candidate = new Candidate();
        populate(candidate, request, CandidateStatus.APPLIED);
        Candidate savedCandidate = candidateRepository.save(candidate);
        log.info("Created candidate with id={}", savedCandidate.getId());
        return toResponse(savedCandidate);
    }

    @Transactional(readOnly = true)
    public CandidateResponse getById(Long id) {
        return toResponse(findCandidate(id));
    }

    @Transactional
    public CandidateResponse update(Long id, CandidateRequest request) {
        Candidate candidate = findCandidate(id);
        String email = normalizeEmail(request.email());
        candidateRepository.findByEmailIgnoreCase(email)
                .filter(existingCandidate -> !existingCandidate.getId().equals(id))
                .ifPresent(existingCandidate -> {
                    throw new DuplicateResourceException("A candidate with email " + email + " already exists");
                });

        populate(candidate, request, candidate.getStatus());
        Candidate savedCandidate = candidateRepository.save(candidate);
        log.info("Updated candidate with id={}", savedCandidate.getId());
        return toResponse(savedCandidate);
    }

    @Transactional
    public void delete(Long id) {
        Candidate candidate = findCandidate(id);
        candidateRepository.delete(candidate);
        log.info("Deleted candidate with id={}", id);
    }

    @Transactional(readOnly = true)
    public Page<CandidateResponse> search(
            String keyword,
            List<String> skills,
            Integer minExperience,
            Integer maxExperience,
            String location,
            CandidateStatus status,
            Pageable pageable
    ) {
        if (minExperience != null && maxExperience != null && minExperience > maxExperience) {
            throw new IllegalArgumentException("minExperience must not be greater than maxExperience");
        }
        return candidateRepository.findAll(
                        CandidateSpecification.filter(keyword, skills, minExperience, maxExperience, location, status), pageable)
                .map(this::toResponse);
    }

    private Candidate findCandidate(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate with id " + id + " was not found"));
    }

    private void populate(Candidate candidate, CandidateRequest request, CandidateStatus defaultStatus) {
        candidate.setFullName(request.fullName().trim());
        candidate.setEmail(normalizeEmail(request.email()));
        candidate.setPhone(request.phone());
        candidate.setSkills(request.skills());
        candidate.setExperienceYears(request.experienceYears());
        candidate.setLocation(request.location());
        candidate.setStatus(request.status() == null ? defaultStatus : request.status());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private CandidateResponse toResponse(Candidate candidate) {
        return new CandidateResponse(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getEmail(),
                candidate.getPhone(),
                candidate.getSkills(),
                candidate.getExperienceYears(),
                candidate.getLocation(),
                candidate.getStatus(),
                candidate.getCreatedAt(),
                candidate.getUpdatedAt()
        );
    }
}
