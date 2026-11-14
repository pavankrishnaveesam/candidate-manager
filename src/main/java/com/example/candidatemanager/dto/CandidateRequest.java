package com.example.candidatemanager.dto;

import com.example.candidatemanager.entity.CandidateStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CandidateRequest(
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Size(max = 255) String currentRole,
        @NotBlank @Email @Size(max = 255) String email,
        @Pattern(
                regexp = "^\\+?[0-9][0-9 ()-]{6,19}$",
                message = "phone must contain 7 to 20 digits and may include spaces, parentheses, or hyphens"
        )
        String phone,
        @Size(max = 1000) String skills,
        @NotNull @Min(0) Integer experienceYears,
        @Size(max = 255) String location,
        CandidateStatus status
) {
}
