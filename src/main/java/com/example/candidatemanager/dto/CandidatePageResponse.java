package com.example.candidatemanager.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record CandidatePageResponse(
        List<CandidateResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static CandidatePageResponse from(Page<CandidateResponse> candidates) {
        return new CandidatePageResponse(
                candidates.getContent(), candidates.getNumber(), candidates.getSize(), candidates.getTotalElements(),
                candidates.getTotalPages(), candidates.isFirst(), candidates.isLast());
    }
}
