package com.example.candidatemanager.dto;

import com.example.candidatemanager.entity.CandidateStatus;
import java.time.LocalDateTime;

public record CandidateResponse(
        Long id,
        String fullName,
        String currentRole,
        String email,
        String phone,
        String skills,
        Integer experienceYears,
        String location,
        CandidateStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
